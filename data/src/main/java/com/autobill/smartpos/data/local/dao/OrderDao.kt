package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobill.smartpos.data.local.entity.OrderEntity
import com.autobill.smartpos.data.local.entity.OrderItemEntity

@Dao
interface OrderDao {

    // ── Order header ─────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrders(orders: List<OrderEntity>)

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    suspend fun getAllOrders(restaurantId: Long): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId AND status = :status ORDER BY createdAt DESC")
    suspend fun getOrdersByStatus(restaurantId: Long, status: String): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId AND status NOT IN ('DELIVERED','CANCELLED') ORDER BY createdAt DESC")
    suspend fun getActiveOrders(restaurantId: Long): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId AND (orderNumber LIKE '%' || :query || '%' OR tableNumber LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    suspend fun searchOrders(restaurantId: Long, query: String): List<OrderEntity>

    /** ISO-8601 lexicographic comparison is safe for "yyyy-MM-dd'T'HH:mm:ss" strings. */
    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId AND createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    suspend fun getOrdersByDateRange(
        restaurantId: Long,
        startDate: String,
        endDate: String,
    ): List<OrderEntity>

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    // ── Order items ──────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Long): List<OrderItemEntity>

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)
}

