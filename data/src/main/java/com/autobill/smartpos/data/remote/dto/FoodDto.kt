package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Food List Item — returned by list/search/paginated endpoints
// GET /api/v1/foods  |  GET /api/v1/foods/restaurant/{id}  |  GET /api/v1/categories/{id}/foods
// Aligned with backendapi.md §7 (April 12, 2026):
//   Backend returns category_id (not category_name), plus description, preparation_time,
//   allergens, calories in the list response.
@JsonClass(generateAdapter = true)
data class FoodListItemDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "price")
    val price: Double,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "category_id")
    val categoryId: Long? = null,
    @param:Json(name = "category_name")
    val categoryName: String? = null,   // may be present; keep for display fallback
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "is_available")
    val isAvailable: Boolean = true,
    @param:Json(name = "preparation_time")
    val preparationTime: Int? = null,
    @param:Json(name = "is_vegetarian")
    val isVegetarian: Boolean = false,
    @param:Json(name = "is_spicy")
    val isSpicy: Boolean = false,
    @param:Json(name = "allergens")
    val allergens: String? = null,
    @param:Json(name = "calories")
    val calories: Int? = null,
)

// DTO: Food Detail — returned by single-item endpoint
// GET /api/v1/foods/{id}
@JsonClass(generateAdapter = true)
data class FoodResponseDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "price")
    val price: Double,
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "category_id")
    val categoryId: Long? = null,
    @param:Json(name = "category_name")
    val categoryName: String? = null,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "restaurant_name")
    val restaurantName: String? = null,
    @param:Json(name = "is_available")
    val isAvailable: Boolean = true,
    @param:Json(name = "preparation_time")
    val preparationTime: Int? = null,
    @param:Json(name = "allergens")
    val allergens: String? = null,
    @param:Json(name = "calories")
    val calories: Int? = null,
    @param:Json(name = "is_vegetarian")
    val isVegetarian: Boolean = false,
    @param:Json(name = "is_spicy")
    val isSpicy: Boolean = false,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Create Food Request
// POST /api/v1/foods/restaurant/{restaurantId}
@JsonClass(generateAdapter = true)
data class CreateFoodRequest(
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "price")
    val price: Double,
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "category_id")
    val categoryId: Long? = null,
    @param:Json(name = "is_vegetarian")
    val isVegetarian: Boolean = false,
    @param:Json(name = "is_spicy")
    val isSpicy: Boolean = false,
)
