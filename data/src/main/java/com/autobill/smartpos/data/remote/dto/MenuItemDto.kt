package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ⚠️ DEPRECATED — MenuItem API endpoints no longer exist in BillSmart v1.0 API.
// Use FoodDto (GET /foods/restaurant/{restaurantId}) instead.
// This file is kept temporarily to avoid breaking builds until callers are migrated.
// DTO: Menu Item (Food)
@JsonClass(generateAdapter = true)
data class MenuItemDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "description")
    val description: String,
    @param:Json(name = "category")
    val category: String,
    @param:Json(name = "price")
    val price: Double,
    @param:Json(name = "cost")
    val cost: Double,
    @param:Json(name = "image_url")
    val imageUrl: String,
    @param:Json(name = "is_vegetarian")
    val isVegetarian: Boolean,
    @param:Json(name = "is_vegan")
    val isVegan: Boolean,
    @param:Json(name = "is_available")
    val isAvailable: Boolean,
    @param:Json(name = "preparation_time_minutes")
    val preparationTimeMinutes: Int,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "ingredients")
    val ingredients: List<String>? = null,
    @param:Json(name = "allergens")
    val allergens: List<String>? = null,
    @param:Json(name = "variants")
    val variants: List<MenuItemVariantDto>? = null,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Menu Item Variant (e.g., Small, Medium, Large)
@JsonClass(generateAdapter = true)
data class MenuItemVariantDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "price_modifier")
    val priceModifier: Double,
    @param:Json(name = "description")
    val description: String,
)

// DTO: Paginated Menu Items Response
@JsonClass(generateAdapter = true)
data class PaginatedMenuItemsDto(
    @param:Json(name = "items")
    val items: List<MenuItemDto>,
    @param:Json(name = "pagination")
    val pagination: PaginationDto,
)

// PaginationDto moved to PaginatedResponseDto.kt

