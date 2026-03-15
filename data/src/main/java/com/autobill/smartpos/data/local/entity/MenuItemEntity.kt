package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Menu Item
// Maps to "menu_items" table in Room database
@Entity(
    tableName = "menu_items",
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
        Index(value = ["category"]),
        Index(value = ["isAvailable"]),
    ],
)
data class MenuItemEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val cost: Double,
    val imageUrl: String,
    val isVegetarian: Boolean,
    val isVegan: Boolean,
    val isAvailable: Boolean,
    val preparationTimeMinutes: Int,
    val restaurantId: Int,
    val createdAt: String,
    val updatedAt: String,
)

