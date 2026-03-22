package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO: Food Item
 * Maps to API response from GET /foods
 * Enhanced to support ODRfast UI with images, categories, and availability
 */
@JsonClass(generateAdapter = true)
data class FoodDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "price")
    val price: Double,
    @param:Json(name = "restroId")
    val restaurantId: Int,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "category")
    val category: String? = null,
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "is_available")
    val isAvailable: Boolean = true,
)

