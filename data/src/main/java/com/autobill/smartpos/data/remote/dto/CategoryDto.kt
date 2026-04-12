package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Category response
// GET /api/v1/categories?restaurant_id={id}  |  GET /api/v1/categories/{id}
@JsonClass(generateAdapter = true)
data class CategoryDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "display_order")
    val displayOrder: Int = 0,
    @param:Json(name = "is_active")
    val isActive: Boolean = true,
    @param:Json(name = "food_count")
    val foodCount: Int = 0,
)

// DTO: Create / Update Category Request
// POST /api/v1/categories?restaurant_id={id}
// PUT  /api/v1/categories/{id}
@JsonClass(generateAdapter = true)
data class CreateCategoryRequest(
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "description")
    val description: String? = null,
    @param:Json(name = "image_url")
    val imageUrl: String? = null,
    @param:Json(name = "display_order")
    val displayOrder: Int = 0,
)

