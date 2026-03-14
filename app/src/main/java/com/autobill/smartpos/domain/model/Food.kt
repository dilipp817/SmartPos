package com.autobill.smartpos.domain.model

data class Food(
    val id: Int,
    val name: String,
    val price: Double,
    val restaurantId: Int,
)

