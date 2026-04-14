package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.autobill.smartpos.domain.repository.RealTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import javax.inject.Inject

// ── Phase 9.1 — Real-Time Use Cases ──────────────────────────────────────────

/**
 * Opens the WebSocket for the active restaurant.
 * Called from [MainViewModel] once session is confirmed.
 */
class ConnectRealTimeUseCase @Inject constructor(
    private val repository: RealTimeRepository,
) {
    operator fun invoke(restaurantId: Long) = repository.connect(restaurantId)
}

/**
 * Closes the WebSocket gracefully.
 * Called from [MainViewModel] on logout.
 */
class DisconnectRealTimeUseCase @Inject constructor(
    private val repository: RealTimeRepository,
) {
    operator fun invoke() = repository.disconnect()
}

/**
 * Observe order-related real-time events (ORDER_CREATED, ORDER_UPDATED, ORDER_ITEM_UPDATED).
 * Used by OrderListScreen and KitchenDisplayScreen to get instant updates.
 */
class ObserveOrderEventsUseCase @Inject constructor(
    private val repository: RealTimeRepository,
) {
    operator fun invoke(): Flow<RealTimeEvent> = repository.observeEvents().filter { event ->
        event is RealTimeEvent.OrderCreated      ||
        event is RealTimeEvent.OrderUpdated      ||
        event is RealTimeEvent.OrderItemUpdated
    }
}

/**
 * Observe table status changes.
 * Used by TableListScreen to reflect live occupancy without polling.
 */
class ObserveTableEventsUseCase @Inject constructor(
    private val repository: RealTimeRepository,
) {
    operator fun invoke(): Flow<RealTimeEvent.TableUpdated> =
        repository.observeEvents().filterIsInstance<RealTimeEvent.TableUpdated>()
}

/**
 * Observe WebSocket connection state.
 * ViewModels use this to enable/disable polling fallback.
 */
class ObserveConnectionStateUseCase @Inject constructor(
    private val repository: RealTimeRepository,
) {
    operator fun invoke(): Flow<ConnectionState> = repository.observeConnectionState()
}

