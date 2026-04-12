package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Order Item
// Maps to "order_items" table in Room database
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["foodId"]),
        Index(value = ["itemStatus"]),
    ],
)
data class OrderItemEntity(
    @PrimaryKey
    val id: Long,
    val orderId: Long,
    val foodId: Long,
    val foodName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val itemStatus: String,         // PENDING | IN_PROGRESS | COMPLETED
    val specialRequests: String?,
    val createdAt: String,
)
