package com.autobill.smartpos.feature.order

import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order

/**
 * UI state for the Kitchen Display Screen.
 *
 * [orders]           — active orders fetched from GET /orders/active; grouped in UI by table
 * [isLoading]        — skeleton shown while first fetch is in-flight
 * [isRefreshing]     — pull-to-refresh spinner (orders already visible)
 * [errorMessage]     — one-shot: non-null when a fetch or mutation failed
 * [updatingItemIds]  — ids of items with a PATCH /status call in-flight (shows row spinner)
 * [successMessage]   — one-shot: snackbar text after a successful item status update
 * [selectedFilter]   — active tab filter on the Kitchen Display header
 */
data class KitchenDisplayUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val updatingItemIds: Set<Long> = emptySet(),
    val successMessage: String? = null,
    val selectedFilter: KitchenDisplayFilter = KitchenDisplayFilter.ALL_ACTIVE,
) {
    /**
     * Orders filtered by [selectedFilter].
     * Kitchen Display only shows active orders (not DELIVERED or CANCELLED).
     */
    val filteredOrders: List<Order>
        get() = when (selectedFilter) {
            KitchenDisplayFilter.ALL_ACTIVE  -> orders
            KitchenDisplayFilter.PENDING     -> orders.filter { o -> o.items.any { it.itemStatus == ItemStatus.PENDING } }
            KitchenDisplayFilter.IN_PROGRESS -> orders.filter { o -> o.items.any { it.itemStatus == ItemStatus.IN_PROGRESS } }
            KitchenDisplayFilter.READY       -> orders.filter { o -> o.items.any { it.itemStatus == ItemStatus.READY } }
        }
}

/**
 * Filter tabs for the Kitchen Display Screen — subset of item statuses relevant to the kitchen.
 */
enum class KitchenDisplayFilter(val label: String) {
    ALL_ACTIVE("All Active"),
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    READY("Ready to Serve"),
}

// ── Kitchen item status progression helpers ───────────────────────────────────

/**
 * The single "primary" next status for a forward-only kitchen tap.
 *   PENDING     → IN_PROGRESS
 *   IN_PROGRESS → READY
 *   READY       → SERVED
 *   SERVED / CANCELLED → null (locked)
 */
fun ItemStatus.kdsNextStatus(): ItemStatus? = when (this) {
    ItemStatus.PENDING     -> ItemStatus.IN_PROGRESS
    ItemStatus.IN_PROGRESS -> ItemStatus.READY
    ItemStatus.READY       -> ItemStatus.SERVED
    else                   -> null
}

/**
 * Label for the primary action button on each kitchen item row.
 *   PENDING     → "Start"
 *   IN_PROGRESS → "Ready"
 *   READY       → "Served"
 *   locked      → ""
 */
fun ItemStatus.kdsActionLabel(): String = when (this) {
    ItemStatus.PENDING     -> "Start"
    ItemStatus.IN_PROGRESS -> "Ready"
    ItemStatus.READY       -> "Served"
    else                   -> ""
}

/**
 * Whether cancel is available from this status.
 * Only PENDING and IN_PROGRESS items can be cancelled via the Kitchen Display.
 */
fun ItemStatus.kdsCancelable(): Boolean =
    this == ItemStatus.PENDING || this == ItemStatus.IN_PROGRESS
