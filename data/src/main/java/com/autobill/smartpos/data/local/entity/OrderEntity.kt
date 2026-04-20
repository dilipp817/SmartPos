package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Order
// Maps to "orders" table in Room database.
//
// ForeignKey constraints to RestaurantEntity and TableEntity were removed (v2 migration).
// These constraints caused SQLiteConstraintException when orders arrived from the server
// before the referenced restaurant/table rows were cached locally.
// Room is used as a read-through cache of server data — cross-table FK constraints
// are not appropriate here.
@Entity(
    tableName = "orders",
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["tableId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class OrderEntity(
    @PrimaryKey
    val id: Long,
    val orderNumber: String,
    val restaurantId: Long,
    val tableId: Long?,
    val tableNumber: String?, // null for TAKEAWAY / counter-service orders
    val orderType: String,
    val status: String,
    val subtotal: Double,
    val totalAmount: Double,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val version: Long,
)
