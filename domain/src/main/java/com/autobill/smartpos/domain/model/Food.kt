package com.autobill.smartpos.domain.model

/**
 * Domain Model: Food
 * Represents a food item in the domain layer
 * Independent of database or API structure
 *
 * Item 13: replaced category: String? with categoryId + categoryName;
 *          added isVegetarian + isSpicy (drives veg/non-veg badges and filter chips)
 */
data class Food(
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
    val preparationTime: Int? = null,   // minutes; shown on KDS and order screen
    val allergens: String? = null,      // free-text e.g. "nuts, dairy"
    val calories: Int? = null,
)
