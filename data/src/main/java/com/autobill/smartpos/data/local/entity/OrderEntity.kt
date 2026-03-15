package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Order
// Maps to "orders" table in Room database
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["tableId"]),
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class OrderEntity(
    @PrimaryKey
    val id: Int,
    val orderNumber: String,
    val tableId: Int?,
    val customerId: Int?,
    val restaurantId: Int,
    val orderType: String, // "dine_in", "takeaway", "delivery"
    val status: String, // "pending", "confirmed", "completed", "cancelled"
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
)

