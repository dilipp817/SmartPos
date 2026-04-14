package com.autobill.smartpos.feature.order

import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus

/**
 * UI state for the Order List screen.
 *
 * [orders]          — filtered list currently shown in the list
 * [selectedFilter]  — active tab
 * [searchQuery]     — current text in the search bar
 * [isSearchActive]  — true while the search bar is expanded
 * [pendingCount]    — badge count for PENDING orders (for nav bar)
 * [isLoading]       — skeleton shown while first fetch is in-flight
 * [isRefreshing]    — pull-to-refresh spinner (list already visible)
 * [errorMessage]    — non-null when the last fetch failed and cache is empty
 * [canCancelOrders] — true for manager / admin / super_admin; gates cancel action
 */
data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val selectedFilter: OrderFilter = OrderFilter.ACTIVE,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val pendingCount: Int = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val canCancelOrders: Boolean = false,
    /** Phase 9.1: drives the live/reconnecting banner in OrderListScreen. */
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
)

/**
 * Filter tabs for the order list.
 *
 * [ALL]         → GET /orders
 * [ACTIVE]      → GET /orders/active  (not DELIVERED or CANCELLED)
 * [PENDING]     → GET /orders/status/PENDING
 * [IN_PROGRESS] → GET /orders/status/IN_PROGRESS
 * [COMPLETED]   → GET /orders/status/COMPLETED
 */
enum class OrderFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
}

/**
 * Colour tokens per order status — used by [OrderItemCard].
 */
fun OrderStatus.containerColor(): Long = when (this) {
    OrderStatus.PENDING     -> 0xFFE3F2FD  // blue-50
    OrderStatus.IN_PROGRESS -> 0xFFFFF8E1  // amber-50
    OrderStatus.COMPLETED   -> 0xFFE8F5E9  // green-50
    OrderStatus.DELIVERED   -> 0xFFF3E5F5  // purple-50
    OrderStatus.CANCELLED   -> 0xFFFFEBEE  // red-50
    OrderStatus.HOLD        -> 0xFFFBE9E7  // deep-orange-50
}

fun OrderStatus.accentColor(): Long = when (this) {
    OrderStatus.PENDING     -> 0xFF1565C0  // blue-800
    OrderStatus.IN_PROGRESS -> 0xFFE65100  // orange-900
    OrderStatus.COMPLETED   -> 0xFF2E7D32  // green-800
    OrderStatus.DELIVERED   -> 0xFF6A1B9A  // purple-800
    OrderStatus.CANCELLED   -> 0xFFB71C1C  // red-900
    OrderStatus.HOLD        -> 0xFFBF360C  // deep-orange-900
}

