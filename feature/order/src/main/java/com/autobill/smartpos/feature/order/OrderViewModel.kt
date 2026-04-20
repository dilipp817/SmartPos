package com.autobill.smartpos.feature.order

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
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
import kotlinx.coroutines.coroutineScope
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

    /** Polling fallback job — 15 s interval (contract M-09). */
    private var pollingJob: Job? = null

    /**
     * Tracks the in-flight order-list fetch job.
     * Cancelled whenever the user switches tabs so a slow previous-tab response
     * cannot overwrite the new tab's data (last-writer-wins race condition fix).
     */
    private var loadOrdersJob: Job? = null

    companion object {
        private const val POLLING_INTERVAL_MS = 15_000L
    }

    init {
        observeRolePermissionsUseCase()
            .onEach { perms -> _uiState.update { it.copy(canCancelOrders = perms.canCancelOrders) } }
            .launchIn(viewModelScope)

        // Phase 9.1: track WebSocket state for the connection banner in OrderListScreen
        observeConnectionStateUseCase()
            .onEach { state -> _uiState.update { it.copy(connectionState = state) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = context.getString(R.string.error_no_restaurant_session))
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
                        // Only prepend if the new order belongs in the current filter tab.
                        // Without this guard, a new PENDING order would appear in every tab.
                        if (orderMatchesFilter(event.order, _uiState.value.selectedFilter)) {
                            _uiState.update { state ->
                                state.copy(
                                    orders       = listOf(event.order) + state.orders,
                                    pendingCount = state.pendingCount + 1,
                                )
                            }
                        } else {
                            _uiState.update { it.copy(pendingCount = it.pendingCount + 1) }
                        }
                    }
                    is RealTimeEvent.OrderUpdated, is RealTimeEvent.OrderItemUpdated -> {
                        val updated = if (event is RealTimeEvent.OrderUpdated) event.order
                                      else (event as RealTimeEvent.OrderItemUpdated).order
                        val currentFilter = _uiState.value.selectedFilter
                        val existsInList  = _uiState.value.orders.any { it.id == updated.id }
                        val matchesFilter = orderMatchesFilter(updated, currentFilter)

                        when {
                            existsInList && matchesFilter -> {
                                // Update in-place — status unchanged relative to filter
                                _uiState.update { state ->
                                    state.copy(orders = state.orders.map { if (it.id == updated.id) updated else it })
                                }
                            }
                            existsInList && !matchesFilter -> {
                                // Order moved to a different status — remove from current tab
                                _uiState.update { state ->
                                    state.copy(orders = state.orders.filter { it.id != updated.id })
                                }
                            }
                            !existsInList && matchesFilter -> {
                                // Order just entered this filter's scope — reload to get full list
                                viewModelScope.launch { loadOrders(currentFilter) }
                            }
                            else -> Unit // not in list, not relevant to current filter
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Returns true if [order] should be visible under [filter].
     * Used to decide whether real-time events should mutate the visible list.
     */
    private fun orderMatchesFilter(order: Order, filter: OrderFilter): Boolean = when (filter) {
        OrderFilter.ALL         -> true
        OrderFilter.ACTIVE      -> order.status !in setOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED)
        OrderFilter.PENDING     -> order.status == OrderStatus.PENDING
        OrderFilter.IN_PROGRESS -> order.status == OrderStatus.IN_PROGRESS
        OrderFilter.COMPLETED   -> order.status == OrderStatus.COMPLETED
        OrderFilter.HOLD        -> order.status == OrderStatus.HOLD
    }

    // ── Filter / Search ──────────────────────────────────────────────────────

    /**
     * Switch filter tab and reload.
     *
     * Key fixes applied here:
     *  1. Cancel [loadOrdersJob] — prevents a slow previous-tab response from
     *     overwriting the new tab's data (last-writer-wins race condition).
     *  2. Clear [OrderUiState.orders] immediately — no stale orders from the
     *     previous tab are visible while the new fetch is in-flight.
     *  3. Pass [filter] directly into [loadOrders] — the filter is captured at
     *     launch time, not read from mutable state during execution.
     */
    fun selectFilter(filter: OrderFilter) {
        if (_uiState.value.selectedFilter == filter) return
        loadOrdersJob?.cancel()
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                orders         = emptyList(), // clear stale data immediately
                isLoading      = true,
                errorMessage   = null,
                searchQuery    = "",
                isSearchActive = false,
            )
        }
        loadOrdersJob = viewModelScope.launch { loadOrders(filter) }
    }

    /**
     * Toggle search bar visibility.
     * Closing the bar resets the query and reloads the current filter — one atomic state update.
     */
    fun onSearchActiveToggle(active: Boolean) {
        if (!active) {
            _uiState.update { it.copy(isSearchActive = false, searchQuery = "", isLoading = true) }
            loadOrdersJob?.cancel()
            loadOrdersJob = viewModelScope.launch { loadOrders(_uiState.value.selectedFilter) }
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
            loadOrdersJob?.cancel()
            loadOrdersJob = viewModelScope.launch { loadOrders(_uiState.value.selectedFilter) }
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
                        isLoading    = false,
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

    // ── Lifecycle callbacks (called from Route via LifecycleEventEffect) ──────

    /** Start 15s polling when screen is resumed (contract M-09). */
    fun onResume() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(POLLING_INTERVAL_MS)
                if (!_uiState.value.isSearchActive) loadAll()
            }
        }
    }

    /** Stop polling when screen is paused to avoid battery drain (contract M-09). */
    fun onPause() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Fetches the order list and pending count concurrently using async/await.
     * Both jobs run in parallel; isRefreshing is reset only after both complete.
     *
     * Uses coroutineScope { async { } } so the parallel fetches are children of the
     * caller's coroutine (pollingJob / refresh job). Cancelling pollingJob via onPause()
     * therefore also cancels any in-flight fetch immediately.
     */
    private suspend fun loadAll(refreshing: Boolean = false) {
        val rid = restaurantId ?: return
        val filter = _uiState.value.selectedFilter   // snapshot before parallel execution
        coroutineScope {
            val ordersDeferred = async { loadOrders(filter) }
            val countDeferred  = async {
                when (val result = getPendingOrdersCountUseCase(rid)) {
                    is Result.Success -> _uiState.update { it.copy(pendingCount = result.data) }
                    else -> Unit  // badge count failure is non-critical — keep previous value
                }
            }
            ordersDeferred.await()
            countDeferred.await()
        }
        if (refreshing) _uiState.update { it.copy(isRefreshing = false) }
    }

    /**
     * Fetches orders for [filter] and writes the result to [_uiState].
     *
     * The [filter] parameter is intentional — it is captured at the call-site
     * (before any async work) so that rapid tab switches cannot cause a stale
     * coroutine to read a newer filter value and clobber the correct result.
     *
     * After the network call completes, the result is discarded if [filter] no
     * longer matches [OrderUiState.selectedFilter] (user switched tabs again
     * while the request was in flight).
     */
    private suspend fun loadOrders(filter: OrderFilter) {
        val rid = restaurantId ?: return
        val result = when (filter) {
            OrderFilter.ALL         -> getAllOrdersUseCase(rid)
            OrderFilter.ACTIVE      -> getActiveOrdersUseCase(rid)
            OrderFilter.PENDING     -> getOrdersByStatusUseCase(rid, OrderStatus.PENDING)
            OrderFilter.IN_PROGRESS -> getOrdersByStatusUseCase(rid, OrderStatus.IN_PROGRESS)
            OrderFilter.COMPLETED   -> getOrdersByStatusUseCase(rid, OrderStatus.COMPLETED)
            OrderFilter.HOLD        -> getOrdersByStatusUseCase(rid, OrderStatus.HOLD)
        }
        _uiState.update { state ->
            // Guard: if the user switched tabs while this fetch was in-flight, discard the result.
            // This is the second line of defence after loadOrdersJob?.cancel() in selectFilter.
            if (state.selectedFilter != filter) return@update state
            when (result) {
                is Result.Success -> state.copy(
                    orders       = result.data,
                    isLoading    = false,
                    errorMessage = null,
                )
                is Result.Failure -> state.copy(
                    isLoading    = false,
                    errorMessage = result.exception.message ?: context.getString(R.string.error_load_orders_failed),
                )
                Result.Loading -> state.copy(isLoading = true)
            }
        }
    }
}
