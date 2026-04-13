package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.OrderApiService
import com.autobill.smartpos.data.remote.dto.CreateOrderRequest
import com.autobill.smartpos.data.remote.dto.OrderItemRequestDto
import com.autobill.smartpos.data.remote.dto.UpdateOrderItemRequest
import com.autobill.smartpos.data.remote.dto.UpdateOrderStatusRequest
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.repository.OrderRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OrderRepository implementation.
 *
 * Phase 5.1 scope: createOrder only.
 * Additional methods (getOrder, updateStatus, etc.) added in Phase 5.2 / 5.3.
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: OrderApiService,
    private val orderDao: OrderDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : OrderRepository {

    /**
     * POST /restaurants/{restaurantId}/orders
     *
     * On success:
     *  - Caches the order header + items in Room.
     *  - Returns the full [Order] domain object.
     *
     * On 409 CONFLICT:
     *  - The table was occupied between the user selecting it and submitting.
     *  - Propagated as [Result.Failure] — caller shows "re-select table" prompt.
     */
    override suspend fun createOrder(
        restaurantId: Long,
        tableId: Long,
        cartItems: List<OrderLineItem>,
        orderType: OrderType,
        notes: String?,
    ): Result<Order> = withContext(ioDispatcher) {
        try {
            val request = CreateOrderRequest(
                tableId   = tableId,
                items     = cartItems.map {
                    OrderItemRequestDto(
                        foodId          = it.foodId,
                        quantity        = it.quantity,
                        specialRequests = it.specialRequests,
                    )
                },
                orderType = orderType.value,
                notes     = notes,
            )
            val response = apiService.createOrder(restaurantId, request)
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to create order"
            }
            // Cache header + items
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })

            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            // Map 409 to domain-level exception so feature modules don't need Retrofit
            if (e.code() == 409) {
                Result.Failure(HttpConflictException("Table is already occupied. Please select a different table."))
            } else {
                Result.Failure(e)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    // ── Phase 5.2 — Read operations ──────────────────────────────────────────

    override suspend fun getAllOrders(restaurantId: Long): Result<List<Order>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getAllOrders(restaurantId)
                val dto = checkNotNull(response.data) { response.message ?: "Failed to fetch orders" }
                val orders = dto.orders.map { it.toDomain() }
                orderDao.upsertOrders(dto.orders.map { it.toEntity() })
                dto.orders.forEach { orderDto ->
                    orderDao.upsertItems(orderDto.items.map { it.toEntity(orderDto.id) })
                }
                Result.Success(orders)
            } catch (e: Exception) {
                val cached = orderDao.getAllOrders(restaurantId).map { it.toDomain() }
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
            }
        }

    override suspend fun getActiveOrders(restaurantId: Long): Result<List<Order>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getActiveOrders(restaurantId)
                val dto = checkNotNull(response.data) { response.message ?: "Failed to fetch active orders" }
                val orders = dto.orders.map { it.toDomain() }
                orderDao.upsertOrders(dto.orders.map { it.toEntity() })
                dto.orders.forEach { orderDto ->
                    orderDao.upsertItems(orderDto.items.map { it.toEntity(orderDto.id) })
                }
                Result.Success(orders)
            } catch (e: Exception) {
                val cached = orderDao.getActiveOrders(restaurantId).map { it.toDomain() }
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
            }
        }

    override suspend fun getOrdersByStatus(
        restaurantId: Long,
        status: OrderStatus,
    ): Result<List<Order>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getOrdersByStatus(restaurantId, status.value)
            val dto = checkNotNull(response.data) { response.message ?: "Failed to fetch orders by status" }
            val orders = dto.orders.map { it.toDomain() }
            orderDao.upsertOrders(dto.orders.map { it.toEntity() })
            dto.orders.forEach { orderDto ->
                orderDao.upsertItems(orderDto.items.map { it.toEntity(orderDto.id) })
            }
            Result.Success(orders)
        } catch (e: Exception) {
            val cached = orderDao.getOrdersByStatus(restaurantId, status.value).map { it.toDomain() }
            if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
        }
    }

    override suspend fun countPendingOrders(restaurantId: Long): Result<Int> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.countPendingOrders(restaurantId)
                val dto = checkNotNull(response.data) { response.message ?: "Failed to fetch pending count" }
                Result.Success(dto.pendingCount)
            } catch (e: Exception) {
                // Fallback: count from local cache
                val count = orderDao.getOrdersByStatus(restaurantId, OrderStatus.PENDING.value).size
                Result.Success(count)
            }
        }

    override suspend fun searchOrders(restaurantId: Long, query: String): Result<List<Order>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.searchOrders(restaurantId, query)
                val dto = checkNotNull(response.data) { response.message ?: "Search failed" }
                Result.Success(dto.orders.map { it.toDomain() })
            } catch (e: Exception) {
                val cached = orderDao.searchOrders(restaurantId, query).map { it.toDomain() }
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
            }
        }

    // ── Phase 5.3 — Order Detail mutations ───────────────────────────────────

    override suspend fun getOrderById(restaurantId: Long, orderId: Long): Result<Order> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getOrderById(restaurantId, orderId)
                val dto = checkNotNull(response.data) { response.message ?: "Order not found" }
                orderDao.upsertOrder(dto.toEntity())
                orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
                Result.Success(dto.toDomain())
            } catch (e: Exception) {
                // Cache fallback — rebuild with items
                val entity = orderDao.getOrderById(orderId)
                if (entity != null) {
                    val items = orderDao.getItemsForOrder(orderId)
                    Result.Success(entity.toDomain(items))
                } else {
                    Result.Failure(e)
                }
            }
        }

    override suspend fun updateOrderStatus(
        restaurantId: Long,
        orderId: Long,
        status: OrderStatus,
    ): Result<Order> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateOrderStatus(
                restaurantId = restaurantId,
                orderId      = orderId,
                request      = UpdateOrderStatusRequest(status = status.value),
            )
            val dto = checkNotNull(response.data) { response.message ?: "Failed to update status" }
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 409) Result.Failure(HttpConflictException("Order was modified by another process."))
            else Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun addItemToOrder(
        restaurantId: Long,
        orderId: Long,
        foodId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order> = withContext(ioDispatcher) {
        try {
            val response = apiService.addItemToOrder(
                restaurantId = restaurantId,
                orderId      = orderId,
                request      = OrderItemRequestDto(
                    foodId          = foodId,
                    quantity        = quantity,
                    specialRequests = specialRequests,
                ),
            )
            val dto = checkNotNull(response.data) { response.message ?: "Failed to add item" }
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 409) Result.Failure(HttpConflictException("Order was modified by another process."))
            else Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun updateOrderItem(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
        quantity: Int,
        specialRequests: String?,
    ): Result<Order> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateOrderItem(
                restaurantId = restaurantId,
                orderId      = orderId,
                itemId       = itemId,
                request      = UpdateOrderItemRequest(
                    quantity        = quantity,
                    specialRequests = specialRequests,
                ),
            )
            val dto = checkNotNull(response.data) { response.message ?: "Failed to update item" }
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            when (e.code()) {
                409  -> Result.Failure(HttpConflictException("Order was modified by another process."))
                400  -> Result.Failure(Exception("This item cannot be edited — it is already ${getLockedItemReason(e)}."))
                else -> Result.Failure(e)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun removeItemFromOrder(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
    ): Result<Order> = withContext(ioDispatcher) {
        try {
            val response = apiService.removeItemFromOrder(restaurantId, orderId, itemId)
            val dto = checkNotNull(response.data) { response.message ?: "Failed to remove item" }
            orderDao.upsertOrder(dto.toEntity())
            orderDao.deleteItemsForOrder(orderId)
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 409) Result.Failure(HttpConflictException("Order was modified by another process."))
            else Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun cancelOrder(restaurantId: Long, orderId: Long): Result<Order> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.cancelOrder(restaurantId, orderId)
                val dto = checkNotNull(response.data) { response.message ?: "Failed to cancel order" }
                orderDao.upsertOrder(dto.toEntity())
                Result.Success(dto.toDomain())
            } catch (e: HttpException) {
                if (e.code() == 409) Result.Failure(HttpConflictException("Order was modified by another process."))
                else Result.Failure(e)
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    // ── Phase 5.4 — KDS item status update ───────────────────────────────────

    override suspend fun updateItemStatus(
        restaurantId: Long,
        orderId: Long,
        itemId: Long,
        newStatus: com.autobill.smartpos.domain.model.ItemStatus,
    ): Result<com.autobill.smartpos.domain.model.Order> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateOrderItemStatus(
                restaurantId = restaurantId,
                orderId      = orderId,
                itemId       = itemId,
                newStatus    = newStatus.value,
            )
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update item status"
            }
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 409) Result.Failure(HttpConflictException("Order was modified by another process."))
            else Result.Failure(e)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    /** Extracts a human-readable lock reason from a 400 response body (best-effort). */
    private fun getLockedItemReason(e: HttpException): String =
        try { e.response()?.errorBody()?.string() ?: "locked" } catch (_: Exception) { "locked" }
}




