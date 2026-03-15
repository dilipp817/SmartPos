package com.autobill.smartpos.domain.model

// Domain Model: Food
// Represents a food item in the domain layer
// Independent of database or API structure
data class Food(
    val id: Int,
    val name: String,
    val price: Double,
    val restaurantId: Int,
)

