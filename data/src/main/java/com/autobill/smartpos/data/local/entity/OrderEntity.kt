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
    ],
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
    val tableId: Long?,             // nullable to support SET_NULL on table delete
    val tableNumber: String,
    val orderType: String,          // DINE_IN  TAKEAWAY  DELIVERY
    val status: String,             // PENDING  IN_PROGRESS  COMPLETED  DELIVERED  CANCELLED  HOLD
    val subtotal: Double,           // pre-tax item total
    val totalAmount: Double,        // = subtotal at order stage; final amount on bill
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val version: Long,
    // customerId removed — backend has no Customer entity (MOBILE_TEAM_RESPONSE.md Point 5, April 17, 2026)
)
