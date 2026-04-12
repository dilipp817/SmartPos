package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observe live cart items */
class GetCartUseCase @Inject constructor(private val repo: CartRepository) {
    operator fun invoke(): Flow<List<CartItem>> = repo.observeCartItems()
}

/** Add a food item to cart (or +1 if already present) */
class AddToCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(food: Food) = repo.addItem(food)
}

/** Increase quantity of a cart item by 1 */
class IncreaseCartQuantityUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(foodId: Long) = repo.increaseQuantity(foodId)
}

/** Decrease quantity of a cart item by 1 — removes if quantity reaches 0 */
class DecreaseCartQuantityUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke(foodId: Long) = repo.decreaseQuantity(foodId)
}

/** Clear all items from cart */
class ClearCartUseCase @Inject constructor(private val repo: CartRepository) {
    suspend operator fun invoke() = repo.clearCart()
}
