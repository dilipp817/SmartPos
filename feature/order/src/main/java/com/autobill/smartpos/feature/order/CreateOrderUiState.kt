package com.autobill.smartpos.feature.order

import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.model.Table

/**
 * UI state for the Create Order confirmation screen.
 *
 * [table]          — the table selected in Phase 4; null while loading
 * [cartItems]      — local cart snapshot (read-only on this screen)
 * [orderType]      — DINE_IN / TAKEAWAY / DELIVERY; defaults to DINE_IN
 * [notes]          — optional kitchen/table notes
 * [isTableLoading] — true while fetching table details from cache
 * [isSubmitting]   — true while POST /orders is in-flight
 * [errorMessage]   — non-null when submission failed (shown inline)
 * [tableConflict]  — true when a 409 was returned (table now occupied)
 * [orderCreated]   — one-shot: non-null orderId after successful creation
 */
data class CreateOrderUiState(
    val table: Table? = null,
    val cartItems: List<CartItem> = emptyList(),
    val orderType: OrderType = OrderType.DINE_IN,
    val notes: String = "",
    val isTableLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val tableConflict: Boolean = false,
    val orderCreated: Long? = null,         // one-shot orderId — consumed by Route
) {
    /** Subtotal of all cart items. */
    val subtotal: Double get() = cartItems.sumOf { it.subtotal }

    /** Estimated GST (18%) — for preview only; server computes the real value in generate-bill. */
    val estimatedTax: Double get() = subtotal * 0.18

    /** Estimated total — preview only. */
    val estimatedTotal: Double get() = subtotal + estimatedTax

    /** True when cart is non-empty and no request is in-flight. */
    val canPlaceOrder: Boolean get() = cartItems.isNotEmpty() && !isSubmitting && !isTableLoading
}

