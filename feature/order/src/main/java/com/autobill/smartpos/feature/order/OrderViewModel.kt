package com.autobill.smartpos.feature.order

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.autobill.smartpos.domain.usecase.GetActiveOrdersUseCase
import com.autobill.smartpos.domain.usecase.GetAllOrdersUseCase
import com.autobill.smartpos.domain.usecase.GetOrdersByStatusUseCase
import com.autobill.smartpos.domain.usecase.GetPendingOrdersCountUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ObserveConnectionStateUseCase
import com.autobill.smartpos.domain.usecase.ObserveOrderEventsUseCase
import com.autobill.smartpos.domain.usecase.ObserveRolePermissionsUseCase
import com.autobill.smartpos.domain.usecase.SearchOrdersUseCase
import com.autobill.smartpos.feature.order.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Order List screen.
 *
 * Responsibilities:
 *  - Load orders based on the active [OrderFilter] tab
 *  - Fetch pending count concurrently with the list (nav badge)
 *  - Support pull-to-refresh
 *  - Search orders with client-side debounce (400 ms)
 *  - Guard against null restaurantId (super_admin without outlet)
 *  - Expose [OrderUiState.canCancelOrders] for role-gated cancel action
 */
@HiltViewModel
class OrderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAllOrdersUseCase: GetAllOrdersUseCase,
    private val getActiveOrdersUseCase: GetActiveOrdersUseCase,
    private val getOrdersByStatusUseCase: GetOrdersByStatusUseCase,
    private val getPendingOrdersCountUseCase: GetPendingOrdersCountUseCase,
    private val searchOrdersUseCase: SearchOrdersUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val observeRolePermissionsUseCase: ObserveRolePermissionsUseCase,
    private val observeOrderEventsUseCase: ObserveOrderEventsUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    /** Cached restaurantId — sourced from session, never hardcoded. */
    private var restaurantId: Long? = null

    /** Tracks the last active search debounce job so it can be cancelled on new input. */
    private var searchJob: Job? = null

    init {
        observeRolePermissionsUseCase()
            .onEach { perms ->
                _uiState.update { it.copy(canCancelOrders = perms.canCancelOrders) }
            }
            .launchIn(viewModelScope)

        // Phase 9.1: track WebSocket state for the connection banner in OrderListScreen
        observeConnectionStateUseCase()
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = context.getString(R.string.error_no_restaurant_session),
                    )
                }
                return@launch
            }
            loadAll()
            observeRealTimeEvents()
        }
    }

    // ── Phase 9.1: Real-time event handling ──────────────────────────────────

    /**
     * Subscribes to WebSocket order events.
     *  - ORDER_CREATED  → prepend to current list (pending count +1)
     *  - ORDER_UPDATED  → replace matching order in-place; if not in list, reload
     * Search mode is deliberately left stale — live search results refresh on next keystroke.
     */
    private fun observeRealTimeEvents() {
        observeOrderEventsUseCase()
            .onEach { event ->
                if (_uiState.value.isSearchActive) return@onEach  // don't disturb search mode
                when (event) {
                    is RealTimeEvent.OrderCreated -> {
                        _uiState.update { state ->
                            state.copy(
                                orders       = listOf(event.order) + state.orders,
                                pendingCount = state.pendingCount + 1,
                            )
                        }
                    }
                    is RealTimeEvent.OrderUpdated, is RealTimeEvent.OrderItemUpdated -> {
                        val updated = if (event is RealTimeEvent.OrderUpdated) event.order
                                      else (event as RealTimeEvent.OrderItemUpdated).order
                        val exists = _uiState.value.orders.any { it.id == updated.id }
                        if (exists) {
                            _uiState.update { state ->
                                state.copy(orders = state.orders.map { if (it.id == updated.id) updated else it })
                            }
                        } else {
                            // Order not in current filter view — quietly refresh
                            viewModelScope.launch { loadOrders() }
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Filter / Search ──────────────────────────────────────────────────────

    /** Switch filter tab and reload immediately. */
    fun selectFilter(filter: OrderFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                isLoading = true,
                errorMessage = null,
                searchQuery = "",
                isSearchActive = false,
            )
        }
        viewModelScope.launch { loadOrders() }
    }

    /**
     * Toggle search bar visibility.
     * Closing the bar resets the query and reloads the current filter — one atomic state update.
     */
    fun onSearchActiveToggle(active: Boolean) {
        if (!active) {
            _uiState.update { it.copy(isSearchActive = false, searchQuery = "", isLoading = true) }
            viewModelScope.launch { loadOrders() }
        } else {
            _uiState.update { it.copy(isSearchActive = true) }
        }
    }

    /**
     * Called on every keystroke in the search field.
     * Debounced 400 ms — only fires a network call when typing pauses.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(isLoading = true) }
            viewModelScope.launch { loadOrders() }
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            val rid = restaurantId ?: return@launch
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = searchOrdersUseCase(rid, query.trim())) {
                is Result.Success -> _uiState.update {
                    it.copy(orders = result.data, isLoading = false, errorMessage = null)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exception.message ?: context.getString(R.string.error_search_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    /** Pull-to-refresh — shows spinner without clearing the existing list. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch { loadAll(refreshing = true) }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Fetches the order list and pending count concurrently using async/await.
     * Both jobs run in parallel; isRefreshing is reset only after both complete.
     */
    private suspend fun loadAll(refreshing: Boolean = false) {
        val rid = restaurantId ?: return
        val ordersDeferred = viewModelScope.async { loadOrders() }
        val countDeferred  = viewModelScope.async {
            when (val result = getPendingOrdersCountUseCase(rid)) {
                is Result.Success -> _uiState.update { it.copy(pendingCount = result.data) }
                else -> Unit  // badge count failure is non-critical — keep previous value
            }
        }
        ordersDeferred.await()
        countDeferred.await()
        if (refreshing) _uiState.update { it.copy(isRefreshing = false) }
    }

    private suspend fun loadOrders() {
        val rid = restaurantId ?: return
        val result = when (_uiState.value.selectedFilter) {
            OrderFilter.ALL         -> getAllOrdersUseCase(rid)
            OrderFilter.ACTIVE      -> getActiveOrdersUseCase(rid)
            // Inline the status mapping — eliminates the need for a force-unwrap (!!)
            OrderFilter.PENDING     -> getOrdersByStatusUseCase(rid, OrderStatus.PENDING)
            OrderFilter.IN_PROGRESS -> getOrdersByStatusUseCase(rid, OrderStatus.IN_PROGRESS)
            OrderFilter.COMPLETED   -> getOrdersByStatusUseCase(rid, OrderStatus.COMPLETED)
        }
        _uiState.update {
            when (result) {
                is Result.Success -> it.copy(
                    orders = result.data,
                    isLoading = false,
                    errorMessage = null,
                )
                is Result.Failure -> it.copy(
                    isLoading = false,
                    errorMessage = result.exception.message ?: context.getString(R.string.error_load_orders_failed),
                )
                Result.Loading -> it.copy(isLoading = true)
            }
        }
    }
}
