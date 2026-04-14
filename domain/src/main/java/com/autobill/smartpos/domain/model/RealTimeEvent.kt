package com.autobill.smartpos.domain.model

/**
 * WebSocket connection states — observed by ViewModels to decide whether to
 * fall back to polling when the socket is unavailable.
 */
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
}

/**
 * Sealed hierarchy of all real-time events the app can receive via WebSocket.
 *
 * Event routing:
 *  - [OrderCreated]      → OrderList (prepend) + KDS + Kitchen notification
 *  - [OrderUpdated]      → OrderList (replace) + OrderDetail (refresh) + status notification
 *  - [OrderItemUpdated]  → KDS (replace order in-place) + "items ready" notification
 *  - [TableUpdated]      → TableList (replace table in-place)
 *  - [ConnectionChanged] → KDS / OrderList switch between WS-driven and polling fallback
 */
sealed class RealTimeEvent {

    /** Server emitted ORDER_CREATED — a brand-new order was placed at this restaurant. */
    data class OrderCreated(val order: Order) : RealTimeEvent()

    /** Server emitted ORDER_UPDATED — order-level status changed (e.g. PENDING → IN_PROGRESS). */
    data class OrderUpdated(val order: Order) : RealTimeEvent()

    /** Server emitted ORDER_ITEM_UPDATED — item-level status changed (KDS event). */
    data class OrderItemUpdated(val order: Order) : RealTimeEvent()

    /** Server emitted TABLE_UPDATED — table status changed (e.g. AVAILABLE → OCCUPIED). */
    data class TableUpdated(val table: Table) : RealTimeEvent()

    /** WebSocket connection state changed — drives polling fallback logic. */
    data class ConnectionChanged(val state: ConnectionState) : RealTimeEvent()
}

