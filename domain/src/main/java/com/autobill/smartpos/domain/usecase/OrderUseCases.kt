package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * Use case: Create a new order from the local cart.
 *
 * Callers should:
 *  1. Collect [GetCartUseCase] to get the current cart items.
 *  2. Map them to [OrderLineItem] list.
 *  3. Call this use case.
 *  4. On [Result.Success] → call [ClearCartUseCase].
 *  5. On [Result.Failure] with HTTP 409 → prompt user to re-select table.
 *
 * 409 CONFLICT is NOT retried automatically — the table selection must be
 * repeated by the user (the table is now occupied by someone else).
 */
class CreateOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        tableId: Long,
        cartItems: List<OrderLineItem>,
        orderType: OrderType,
        notes: String?,
    ): Result<Order> = repository.createOrder(
        restaurantId = restaurantId,
        tableId      = tableId,
        cartItems    = cartItems,
        orderType    = orderType,
        notes        = notes,
    )
}

