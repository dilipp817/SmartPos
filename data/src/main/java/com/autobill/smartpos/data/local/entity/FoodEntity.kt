package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity: Food
 * Maps to "foods" table in Room database
 *
 * Schema is part of the v1 production baseline (April 19, 2026).
 * Add a new MIGRATION_1_2 in AppDatabase for any future schema changes.
 */
@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["categoryId"]),
        Index(value = ["isAvailable"]),
    ],
)
data class FoodEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val price: Double,
    val restaurantId: Long,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val description: String? = null,
    val isAvailable: Boolean = true,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val preparationTime: Int? = null,
    val allergens: String? = null,
    val calories: Int? = null,
)
