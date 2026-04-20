package com.autobill.smartpos.feature.order

import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.common.TaxConstants

/**
 * UI state for the Create Order confirmation screen.
 *
 * [table]          — the table selected in Phase 4; null while loading OR when isNoTable=true
 * [isNoTable]      — true when tableId=0 (TAKEAWAY or TABLE_MANAGEMENT=false order — no table needed)
 * [cartItems]      — local cart snapshot (read-only on this screen)
 * [orderType]      — DINE_IN / TAKEAWAY / DELIVERY; pre-set from nav arg
 * [notes]          — optional kitchen/table notes
 * [isTableLoading] — true while fetching table details from cache (always false when isNoTable=true)
 * [isSubmitting]   — true while POST /orders is in-flight
 * [isOffline]      — true when ConnectivityMonitor reports no internet (Phase 9.2)
 * [errorMessage]   — non-null when submission failed (shown inline)
 * [tableConflict]  — true when a 409 was returned (table now occupied)
 * [orderCreated]   — one-shot: non-null orderId after successful online creation
 * [orderQueued]    — one-shot: true when order was saved to offline queue (Phase 9.2)
 */
data class CreateOrderUiState(
    val table: Table? = null,
    val isNoTable: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val orderType: OrderType = OrderType.DINE_IN,
    val notes: String = "",
    val isTableLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val tableConflict: Boolean = false,
    val orderCreated: Long? = null,         // one-shot orderId — consumed by Route
    val orderQueued: Boolean = false,       // one-shot — consumed by Route (Phase 9.2)
) {
    /** Subtotal of all cart items. */
    val subtotal: Double get() = cartItems.sumOf { it.subtotal }

    /** Estimated GST (18%) — for preview only; server computes the real value in generate-bill. */
    val estimatedTax: Double get() = subtotal * TaxConstants.GST_ESTIMATE_RATE

    /** Estimated total — preview only. */
    val estimatedTotal: Double get() = subtotal + estimatedTax

    /**
     * True when the cart is non-empty and no request is in-flight.
     * For no-table orders [isNoTable=true] we skip table loading entirely,
     * so [isTableLoading] is always false and does not block submission.
     */
    val canPlaceOrder: Boolean get() = cartItems.isNotEmpty() && !isSubmitting && (isNoTable || !isTableLoading)
}
