package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Customer
// Maps to "customers" table in Room database
@Entity(
    tableName = "customers",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["phone"]),
        Index(value = ["email"]),
    ],
)
data class CustomerEntity(
    @PrimaryKey
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val address: String,
    val loyaltyPoints: Int,
    val totalSpent: Double,
    val totalOrders: Int,
    val restaurantId: Int,
    val createdAt: String,
)

