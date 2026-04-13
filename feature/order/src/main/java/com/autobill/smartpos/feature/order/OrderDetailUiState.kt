package com.autobill.smartpos.feature.order

import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderItem
import com.autobill.smartpos.domain.model.OrderStatus

/**
 * UI state for the Order Detail screen.
 *
 * [order]              — full order with items; null while first load is in-flight
 * [isLoading]          — skeleton shown while first fetch is in-flight
 * [isRefreshing]       — pull-to-refresh spinner (order already visible)
 * [errorMessage]       — non-null when a fetch or mutation failed
 * [isUpdatingStatus]   — true while PATCH /status is in-flight
 * [addItemDialog]      — non-null → AddItem dialog is open
 * [isAddingItem]       — true while POST /items is in-flight
 * [addItemError]       — inline error shown inside the Add Item dialog
 * [editItemDialog]     — non-null → EditItem dialog is open
 * [isEditingItem]      — true while PUT /items/{id} is in-flight
 * [editItemError]      — inline error shown inside the Edit Item dialog
 * [removingItemIds]    — ids of items with DELETE in-flight (shows spinner on that row)
 * [showCancelDialog]   — true → Cancel Order confirmation dialog is open
 * [isCancelling]       — true while DELETE /orders/{id} is in-flight
 * [canCancelOrders]    — gated to manager / admin / super_admin roles
 * [orderCancelled]     — one-shot: true after successful cancellation → Route calls onBack()
 * [successMessage]     — one-shot snackbar message after a successful mutation
 * [conflictMessage]    — one-shot: set when a 409 persists after one retry
 */
data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    // status update
    val isUpdatingStatus: Boolean = false,
    // add-item dialog
    val addItemDialog: AddItemDialogState? = null,
    val isAddingItem: Boolean = false,
    val addItemError: String? = null,
    // edit-item dialog
    val editItemDialog: EditItemDialogState? = null,
    val isEditingItem: Boolean = false,
    val editItemError: String? = null,
    // per-item remove in-flight
    val removingItemIds: Set<Long> = emptySet(),
    // cancel order
    val showCancelDialog: Boolean = false,
    val isCancelling: Boolean = false,
    // permissions
    val canCancelOrders: Boolean = false,
    // one-shot events
    val orderCancelled: Boolean = false,
    val successMessage: String? = null,
    val conflictMessage: String? = null,
) {
    /**
     * Valid next statuses the user may select via the status chip bar.
     * CANCELLED is excluded here — it is handled by the dedicated "Cancel Order" button.
     *
     * Roadmap transitions:
     *   PENDING     → IN_PROGRESS, HOLD
     *   IN_PROGRESS → COMPLETED
     *   HOLD        → IN_PROGRESS
     *   COMPLETED   → DELIVERED
     *   DELIVERED / CANCELLED → (final — no transitions)
     */
    val allowedStatusTransitions: List<OrderStatus>
        get() = when (order?.status) {
            OrderStatus.PENDING     -> listOf(OrderStatus.IN_PROGRESS, OrderStatus.HOLD)
            OrderStatus.IN_PROGRESS -> listOf(OrderStatus.COMPLETED)
            OrderStatus.HOLD        -> listOf(OrderStatus.IN_PROGRESS)
            OrderStatus.COMPLETED   -> listOf(OrderStatus.DELIVERED)
            else                    -> emptyList()
        }

    /** Add-item is only allowed while the order is PENDING or HOLD. */
    val canAddItems: Boolean
        get() = order?.status == OrderStatus.PENDING || order?.status == OrderStatus.HOLD

    /** True when the order is in a final state (no further actions possible). */
    val isOrderFinal: Boolean
        get() = order?.status == OrderStatus.DELIVERED || order?.status == OrderStatus.CANCELLED
}

/**
 * State for the Add Item dialog's food picker + quantity selector.
 *
 * [searchQuery]   — text entered by the user
 * [foodResults]   — search results from [SearchFoodsUseCase]
 * [selectedFood]  — food the user tapped in the results list
 * [quantity]      — stepper value (min 1)
 * [specialRequests] — optional kitchen note
 * [isSearching]   — true while the search call is in-flight
 */
data class AddItemDialogState(
    val searchQuery: String = "",
    val foodResults: List<Food> = emptyList(),
    val selectedFood: Food? = null,
    val quantity: Int = 1,
    val specialRequests: String = "",
    val isSearching: Boolean = false,
)

/**
 * State for the Edit Item dialog.
 *
 * Initialised from an existing [OrderItem]; user can change quantity and special requests.
 */
data class EditItemDialogState(
    val item: OrderItem,
    val quantity: Int = item.quantity,
    val specialRequests: String = item.specialRequests ?: "",
)

/**
 * Whether an item is server-locked.
 * Items in READY, SERVED, or CANCELLED state cannot be edited or deleted (server returns 400).
 */
fun ItemStatus.isLocked(): Boolean =
    this == ItemStatus.READY || this == ItemStatus.SERVED || this == ItemStatus.CANCELLED

/** Colour token for each [ItemStatus] chip — used in [OrderDetailScreen]. */
fun ItemStatus.containerColor(): Long = when (this) {
    ItemStatus.PENDING     -> 0xFFE3F2FD   // blue-50
    ItemStatus.IN_PROGRESS -> 0xFFFFF8E1   // amber-50
    ItemStatus.READY       -> 0xFFF3E5F5   // purple-50
    ItemStatus.SERVED      -> 0xFFE8F5E9   // green-50
    ItemStatus.CANCELLED   -> 0xFFFFEBEE   // red-50
}

fun ItemStatus.accentColor(): Long = when (this) {
    ItemStatus.PENDING     -> 0xFF1565C0   // blue-800
    ItemStatus.IN_PROGRESS -> 0xFFE65100   // orange-900
    ItemStatus.READY       -> 0xFF6A1B9A   // purple-800
    ItemStatus.SERVED      -> 0xFF2E7D32   // green-800
    ItemStatus.CANCELLED   -> 0xFFB71C1C   // red-900
}

