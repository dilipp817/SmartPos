package com.autobill.smartpos.feature.reports

import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus

/**
 * UI state for the Order History screen (Phase 8.2).
 *
 * [startDateMs] / [endDateMs]  — date range (default: today)
 * [selectedFilter]             — status filter applied on the client-side
 * [orders]                     — currently displayed (filtered) list
 * [isLoading]                  — skeleton / progress shown on first load
 * [isRefreshing]               — pull-to-refresh spinner (list already visible)
 * [errorMessage]               — non-null when fetch failed and cache is empty
 * [showStartPicker] / [showEndPicker] — DatePickerDialog visibility
 */
data class OrderHistoryUiState(
    val startDateMs: Long = todayStartMs(),
    val endDateMs: Long = todayEndMs(),
    val selectedFilter: OrderHistoryFilter = OrderHistoryFilter.ALL,
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val showStartPicker: Boolean = false,
    val showEndPicker: Boolean = false,
)

/**
 * Filter options for Order History.
 * [ALL]       → show every order in the date range
 * [DELIVERED] → show only paid / delivered orders
 * [CANCELLED] → show only cancelled orders
 */
enum class OrderHistoryFilter(val label: String) {
    ALL("All"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled"),
}

/** Applies the [OrderHistoryFilter] to a raw order list. */
internal fun List<Order>.applyFilter(filter: OrderHistoryFilter): List<Order> = when (filter) {
    OrderHistoryFilter.ALL       -> this
    OrderHistoryFilter.DELIVERED -> filter { it.status == OrderStatus.DELIVERED }
    OrderHistoryFilter.CANCELLED -> filter { it.status == OrderStatus.CANCELLED }
}

