package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus
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

/** Use case: Fetch all orders for a restaurant. */
class GetAllOrdersUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<List<Order>> =
        repository.getAllOrders(restaurantId)
}

/** Use case: Fetch active orders (not DELIVERED or CANCELLED). */
class GetActiveOrdersUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<List<Order>> =
        repository.getActiveOrders(restaurantId)
}

/** Use case: Fetch orders filtered by a specific [OrderStatus]. */
class GetOrdersByStatusUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        status: OrderStatus,
    ): Result<List<Order>> = repository.getOrdersByStatus(restaurantId, status)
}

/** Use case: Get count of PENDING orders (used for nav badge). */
class GetPendingOrdersCountUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<Int> =
        repository.countPendingOrders(restaurantId)
}

/** Use case: Search orders by order number, table number, or status keyword. */
class SearchOrdersUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        query: String,
    ): Result<List<Order>> = repository.searchOrders(restaurantId, query)
}

// ── Phase 5.3 — Order Detail use cases ──────────────────────────────────────

/** Use case: Fetch a single order by ID — full detail with all items. */
class GetOrderByIdUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(restaurantId: Long, orderId: Long): Result<Order> =
        repository.getOrderById(restaurantId, orderId)
}

/** Use case: Update the status of an order (e.g. PENDING → IN_PROGRESS). */
class UpdateOrderStatusUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        status: OrderStatus,
    ): Result<Order> = repository.updateOrderStatus(restaurantId, orderId, status)
}

/** Use case: Add a new item to an existing order (PENDING or HOLD only). */
class AddItemToOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        foodId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order> = repository.addItemToOrder(
        restaurantId    = restaurantId,
        orderId         = orderId,
        foodId          = foodId,
        quantity        = quantity,
        specialRequests = specialRequests,
    )
}

/**
 * Use case: Update quantity / special requests for an existing order item.
 * ⚠️ Will fail with an error if the item is in READY, SERVED, or CANCELLED state.
 */
class UpdateOrderItemUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order> = repository.updateOrderItem(
        restaurantId    = restaurantId,
        orderId         = orderId,
        itemId          = itemId,
        quantity        = quantity,
        specialRequests = specialRequests,
    )
}

/** Use case: Remove a single item from an order. */
class RemoveItemFromOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
    ): Result<Order> = repository.removeItemFromOrder(restaurantId, orderId, itemId)
}

/** Use case: Cancel an entire order. Role-gated — caller must verify [canCancelOrders] first. */
class CancelOrderUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(restaurantId: Long, orderId: Long): Result<Order> =
        repository.cancelOrder(restaurantId, orderId)
}

// ── Phase 5.4 — KDS ──────────────────────────────────────────────────────────

/**
 * Use case: Update the kitchen status of a single order item.
 *
 * Forward-only KDS progression:
 *   PENDING → IN_PROGRESS | CANCELLED
 *   IN_PROGRESS → READY | CANCELLED
 *   READY → SERVED
 *   SERVED / CANCELLED → locked (UI must not call this)
 *
 * PATCH /restaurants/{restaurantId}/orders/{orderId}/items/{itemId}/status?newStatus=IN_PROGRESS
 */
class UpdateItemStatusUseCase @Inject constructor(
    private val repository: OrderRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
        newStatus: com.autobill.smartpos.domain.model.ItemStatus,
    ): Result<Order> = repository.updateItemStatus(restaurantId, orderId, itemId, newStatus)
}

