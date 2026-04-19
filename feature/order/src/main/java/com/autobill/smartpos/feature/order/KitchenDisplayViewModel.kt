package com.autobill.smartpos.feature.order

import android.content.Context
import androidx.lifecycle.ViewModel
import com.autobill.smartpos.feature.order.R
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.autobill.smartpos.domain.usecase.GetActiveOrdersUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ObserveConnectionStateUseCase
import com.autobill.smartpos.domain.usecase.ObserveOrderEventsUseCase
import com.autobill.smartpos.domain.usecase.UpdateItemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
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
 * ViewModel for the Kitchen Display Screen.
 *
 * Phase 9.1 upgrade:
 *  - Primary data source: WebSocket ORDER_ITEM_UPDATED / ORDER_UPDATED events.
 *    On each event, the matching order is swapped in-place — no full reload needed.
 *  - Fallback: 60 s polling (doubled from 30 s) used ONLY when WebSocket is
 *    DISCONNECTED or RECONNECTING. Cancelled as soon as the socket reconnects.
 *  - Connection state badge exposed via [KitchenDisplayUiState.connectionState]
 *    so the KDS header can show a ⚡ / ⚠ indicator.
 */
@HiltViewModel
class KitchenDisplayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getActiveOrdersUseCase: GetActiveOrdersUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val observeOrderEventsUseCase: ObserveOrderEventsUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenDisplayUiState())
    val uiState: StateFlow<KitchenDisplayUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null
    private var pollingJob: Job? = null

    companion object {
        /** Fallback polling interval when WebSocket is unavailable — 15 s (contract M-09). */
        private const val POLLING_INTERVAL_MS = 15_000L
    }

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = context.getString(R.string.error_no_restaurant_session),
                    )
                }
                return@launch
            }
            loadOrders()
            observeWebSocketEvents()
            observeConnectionState()
        }
    }

    // ── Lifecycle callbacks (called from Route via LifecycleEventEffect) ──────

    /** Call from the screen's onResume — starts fallback polling if WS is not CONNECTED. */
    fun onResume() {
        val connectionState = _uiState.value.connectionState
        if (connectionState != ConnectionState.CONNECTED) {
            startPollingFallback()
        }
    }

    /** Call from the screen's onPause — stops fallback polling to avoid battery drain. */
    fun onPause() {
        stopPolling()
    }

    // ── Real-time event observation ───────────────────────────────────────────

    private fun observeWebSocketEvents() {
        observeOrderEventsUseCase()
            .onEach { event ->
                when (event) {
                    is RealTimeEvent.OrderItemUpdated,
                    is RealTimeEvent.OrderUpdated -> {
                        val updatedOrder = when (event) {
                            is RealTimeEvent.OrderItemUpdated -> event.order
                            is RealTimeEvent.OrderUpdated     -> event.order
                            else                              -> return@onEach
                        }
                        _uiState.update { state ->
                            val exists = state.orders.any { it.id == updatedOrder.id }
                            val newList = if (exists) {
                                state.orders.map { if (it.id == updatedOrder.id) updatedOrder else it }
                            } else {
                                state.orders + updatedOrder
                            }
                            state.copy(orders = newList.filter { o ->
                                o.status != com.autobill.smartpos.domain.model.OrderStatus.DELIVERED &&
                                o.status != com.autobill.smartpos.domain.model.OrderStatus.CANCELLED
                            })
                        }
                    }
                    is RealTimeEvent.OrderCreated -> {
                        _uiState.update { state ->
                            state.copy(orders = listOf(event.order) + state.orders)
                        }
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        observeConnectionStateUseCase()
            .onEach { state ->
                _uiState.update { it.copy(connectionState = state) }
                when (state) {
                    ConnectionState.CONNECTED    -> stopPolling()   // WS active — no need to poll
                    ConnectionState.DISCONNECTED,
                    ConnectionState.RECONNECTING -> startPollingFallback()
                    ConnectionState.CONNECTING   -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    // ── Polling fallback ──────────────────────────────────────────────────────

    private fun startPollingFallback() {
        if (pollingJob?.isActive == true) return   // already polling
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(POLLING_INTERVAL_MS)
                loadOrders()
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ── Manual refresh ────────────────────────────────────────────────────────

    /** Manual pull-to-refresh — keeps existing orders visible while re-fetching. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            loadOrders()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ── Filter ───────────────────────────────────────────────────────────────

    fun selectFilter(filter: KitchenDisplayFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // ── Item status update ────────────────────────────────────────────────────

    /**
     * PATCH /orders/{orderId}/items/{itemId}/status
     *
     * Shows a per-row spinner while in-flight via [updatingItemIds].
     * On success: replaces the order in [orders] list with the fresh server copy.
     * On failure: shows [errorMessage] snackbar.
     */
    fun updateItemStatus(orderId: Long, itemId: Long, newStatus: ItemStatus) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(updatingItemIds = it.updatingItemIds + itemId) }

        viewModelScope.launch {
            when (val result = updateItemStatusUseCase(rid, orderId, itemId, newStatus)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        orders          = state.orders.map { if (it.id == orderId) result.data else it },
                        updatingItemIds = state.updatingItemIds - itemId,
                        successMessage  = context.getString(R.string.order_status_update_success, newStatus.value.replace("_", " ")),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        updatingItemIds = it.updatingItemIds - itemId,
                        errorMessage    = result.exception.message ?: context.getString(R.string.error_update_item_status),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── One-shot event consumers ──────────────────────────────────────────────

    fun onSuccessMessageConsumed() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun loadOrders() {
        val rid = restaurantId ?: return
        when (val result = getActiveOrdersUseCase(rid)) {
            is Result.Success -> _uiState.update {
                it.copy(orders = result.data, isLoading = false, errorMessage = null)
            }
            is Result.Failure -> _uiState.update {
                it.copy(isLoading = false, errorMessage = result.exception.message ?: context.getString(R.string.error_kitchen_load_failed))
            }
            Result.Loading -> Unit
        }
    }
}
