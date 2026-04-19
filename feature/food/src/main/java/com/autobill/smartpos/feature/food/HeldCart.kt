package com.autobill.smartpos.feature.food

import com.autobill.smartpos.domain.model.CartItem

/**
 * Represents a cart that has been put on hold by the cashier.
 * Held in-memory only — lives for the duration of the app session.
 * When the customer is ready to pay, cashier resumes this cart.
 */
data class HeldCart(
    val id: String,                  // Unique local ID e.g. "Bill #1"
    val label: String,               // Display label e.g. "Bill #1"
    val items: List<CartItem>,       // Snapshot of cart items at time of hold
    val totalAmount: Double,         // Pre-computed total for display in the held list
)
