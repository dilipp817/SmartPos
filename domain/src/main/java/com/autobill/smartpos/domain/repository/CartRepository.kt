package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.Food
import kotlinx.coroutines.flow.Flow

/**
 * Repository Interface: Cart
 * Manages the in-memory cart for the active POS session.
 * Cart is session-scoped — cleared when the order is placed.
 */
interface CartRepository {
    /** Observe live cart items as a stream */
    fun observeCartItems(): Flow<List<CartItem>>

    /** Add a food item, or increment quantity if already present */
    suspend fun addItem(food: Food)

    /** Increment quantity of an existing cart item by 1 */
    suspend fun increaseQuantity(foodId: Long)

    /** Decrement quantity by 1 — removes item if quantity reaches 0 */
    suspend fun decreaseQuantity(foodId: Long)

    /** Remove all items from cart */
    suspend fun clearCart()

    /** Restore a snapshot of cart items — used when resuming a held bill */
    suspend fun restoreItems(items: List<CartItem>)
}
