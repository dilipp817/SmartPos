package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Menu Item (Food)
// Maps to API response from GET /restaurants/{restaurantId}/menu/items
@JsonClass(generateAdapter = true)
data class MenuItemDto(
    @param:Json(name = "id")
    val id: Int,
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
    val restaurantId: Int,
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
    val id: Int,
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

// DTO: Pagination Info
@JsonClass(generateAdapter = true)
data class PaginationDto(
    @param:Json(name = "page")
    val page: Int,
    @param:Json(name = "limit")
    val limit: Int,
    @param:Json(name = "total")
    val total: Int,
    @param:Json(name = "total_pages")
    val totalPages: Int,
)

