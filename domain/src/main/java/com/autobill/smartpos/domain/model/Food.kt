package com.autobill.smartpos.domain.model

/**
 * Domain Model: Food
 * Represents a food item in the domain layer
 * Independent of database or API structure
 * Enhanced with additional fields for ODRfast UI support
 */
data class Food(
    val id: Int,
    val name: String,
    val price: Double,
    val restaurantId: Int,
    val imageUrl: String? = null,
    val category: String? = null,
    val description: String? = null,
    val isAvailable: Boolean = true,
)

