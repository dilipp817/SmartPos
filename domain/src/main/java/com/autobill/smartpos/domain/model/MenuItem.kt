package com.autobill.smartpos.domain.model

// Domain Model: Menu Item (Food)
// Independent of database or API structure
data class MenuItem(
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
    val ingredients: List<String>? = null,
    val allergens: List<String>? = null,
    val variants: List<MenuItemVariant>? = null,
    val createdAt: String,
    val updatedAt: String,
)

// Domain Model: Menu Item Variant
data class MenuItemVariant(
    val id: Long,
    val name: String,
    val priceModifier: Double,
    val description: String,
)

