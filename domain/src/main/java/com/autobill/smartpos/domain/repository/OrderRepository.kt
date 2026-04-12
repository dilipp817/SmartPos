package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderType

/**
 * Repository contract for order data operations.
 * restaurantId always comes from the session — never hardcoded.
 */
interface OrderRepository {

    /**
     * POST /restaurants/{restaurantId}/orders
     *
     * [cartItems] is a list of (foodId, quantity, specialRequests?) triples — built
     * from the local cart before calling this.
     *
     * On success → the returned [Order] has status=PENDING and the table is auto-set
     * to OCCUPIED by the backend.
     *
     * On 409 CONFLICT → the table was occupied between selection and submission.
     * Caller is responsible for prompting the user to re-select a table.
     */
    suspend fun createOrder(
        restaurantId: Long,
        tableId: Long,
        cartItems: List<OrderLineItem>,
        orderType: OrderType,
        notes: String?,
    ): Result<Order>
}

/**
 * Lightweight value object representing one line in the order request.
 * Kept in domain layer so it can be built from [CartItem] without data-layer types.
 */
data class OrderLineItem(
    val foodId: Long,
    val quantity: Int,
    val specialRequests: String? = null,
)

