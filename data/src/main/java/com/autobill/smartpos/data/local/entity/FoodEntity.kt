package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity: Food
 * Maps to "foods" table in Room database
 * Enhanced with additional fields for ODRfast UI support
 */
@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["category"]),
        Index(value = ["isAvailable"]),
    ],
)
data class FoodEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val price: Double,
    val restaurantId: Int,
    val imageUrl: String? = null,
    val category: String? = null,
    val description: String? = null,
    val isAvailable: Boolean = true,
)

