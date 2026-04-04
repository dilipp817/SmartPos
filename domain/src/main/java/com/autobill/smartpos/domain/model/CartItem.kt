package com.autobill.smartpos.domain.model

/**
 * Domain Model: CartItem
 * Represents a food item added to the active order cart.
 * Stores food details at time of adding — price changes do not affect in-progress orders.
 */
data class CartItem(
    val foodId: Int,
    val foodName: String,
    val foodPrice: Double,
    val quantity: Int,
    val imageUrl: String? = null,
) {
    /** Computed subtotal for this line item */
    val subtotal: Double get() = foodPrice * quantity
}

