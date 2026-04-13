package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus
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

    /** GET /restaurants/{restaurantId}/orders */
    suspend fun getAllOrders(restaurantId: Long): Result<List<Order>>

    /** GET /restaurants/{restaurantId}/orders/active — not DELIVERED or CANCELLED */
    suspend fun getActiveOrders(restaurantId: Long): Result<List<Order>>

    /** GET /restaurants/{restaurantId}/orders/status/{status} */
    suspend fun getOrdersByStatus(restaurantId: Long, status: OrderStatus): Result<List<Order>>

    /** GET /restaurants/{restaurantId}/orders/count/pending */
    suspend fun countPendingOrders(restaurantId: Long): Result<Int>

    /** GET /restaurants/{restaurantId}/orders/search?q=... */
    suspend fun searchOrders(restaurantId: Long, query: String): Result<List<Order>>

    // ── Phase 5.3 — Order Detail mutations ───────────────────────────────────

    /** GET /restaurants/{restaurantId}/orders/{orderId} — full order with items + version */
    suspend fun getOrderById(restaurantId: Long, orderId: Long): Result<Order>

    /** PATCH /restaurants/{restaurantId}/orders/{orderId}/status */
    suspend fun updateOrderStatus(
        restaurantId: Long,
        orderId: Long,
        status: OrderStatus,
    ): Result<Order>

    /** POST /restaurants/{restaurantId}/orders/{orderId}/items — PENDING or HOLD only */
    suspend fun addItemToOrder(
        restaurantId: Long,
        orderId: Long,
        foodId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order>

    /**
     * PUT /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}
     * ⚠️ Returns 400 if item is READY, SERVED, or CANCELLED (server-locked).
     */
    suspend fun updateOrderItem(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order>

    /** DELETE /restaurants/{restaurantId}/orders/{orderId}/items/{itemId} */
    suspend fun removeItemFromOrder(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
    ): Result<Order>

    /** DELETE /restaurants/{restaurantId}/orders/{orderId} — cancel entire order */
    suspend fun cancelOrder(restaurantId: Long, orderId: Long): Result<Order>
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

