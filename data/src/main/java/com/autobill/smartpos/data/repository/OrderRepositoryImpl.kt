package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.OrderApiService
import com.autobill.smartpos.data.remote.dto.CreateOrderRequest
import com.autobill.smartpos.data.remote.dto.OrderItemRequestDto
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
}




