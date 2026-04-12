package com.autobill.smartpos.data.local.entity

// DEPRECATED — Item 15 (April 12, 2026)
// Menu items are fetched as Food via FoodEntity (category-aware, lightweight).
// The heavyweight MenuItem concept with cost/preparationTime/allergens is not
// part of the POS v1 flow. Kept as a plain data class for reference only.
// Do NOT register in AppDatabase. Do NOT add new FKs pointing to this class.
@Deprecated(
    message = "MenuItem local caching is removed. Use FoodEntity instead. Not registered in AppDatabase.",
    level = DeprecationLevel.WARNING,
)
data class MenuItemEntity(
    val id: Long,
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
    val restaurantId: Long,
    val createdAt: String,
    val updatedAt: String,
)
