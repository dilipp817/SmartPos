package com.autobill.smartpos.data.repository

import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory CartRepository implementation.
 * Cart is session-scoped — data lives only while the app is running.
 * Uses LinkedHashMap to preserve insertion order for display.
 */
@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {

    private val _items = MutableStateFlow<LinkedHashMap<Long, CartItem>>(LinkedHashMap())

    override fun observeCartItems(): Flow<List<CartItem>> =
        _items.map { it.values.toList() }

    override suspend fun addItem(food: Food) {
        _items.update { current ->
            val newMap = LinkedHashMap(current)
            val existing = newMap[food.id]
            newMap[food.id] = if (existing != null) {
                existing.copy(quantity = existing.quantity + 1)
            } else {
                CartItem(
                    foodId = food.id,
                    foodName = food.name,
                    foodPrice = food.price,
                    quantity = 1,
                    imageUrl = food.imageUrl,
                )
            }
            newMap
        }
    }

    override suspend fun increaseQuantity(foodId: Long) {
        _items.update { current ->
            val newMap = LinkedHashMap(current)
            val existing = newMap[foodId] ?: return@update current
            newMap[foodId] = existing.copy(quantity = existing.quantity + 1)
            newMap
        }
    }

    override suspend fun decreaseQuantity(foodId: Long) {
        _items.update { current ->
            val newMap = LinkedHashMap(current)
            val existing = newMap[foodId] ?: return@update current
            if (existing.quantity <= 1) {
                newMap.remove(foodId)
            } else {
                newMap[foodId] = existing.copy(quantity = existing.quantity - 1)
            }
            newMap
        }
    }

    override suspend fun clearCart() {
        _items.value = LinkedHashMap()
    }

    override suspend fun restoreItems(items: List<CartItem>) {
        val newMap = LinkedHashMap<Long, CartItem>()
        items.forEach { newMap[it.foodId] = it }
        _items.value = newMap
    }
}
