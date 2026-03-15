package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Restaurant Details
// Maps to API response from GET /restaurants/{restaurantId}
@JsonClass(generateAdapter = true)
data class RestaurantDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "address")
    val address: String,
    @param:Json(name = "phone")
    val phone: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "logo_url")
    val logoUrl: String,
    @param:Json(name = "timezone")
    val timezone: String,
    @param:Json(name = "currency")
    val currency: String,
    @param:Json(name = "tax_rate")
    val taxRate: Double,
    @param:Json(name = "is_active")
    val isActive: Boolean,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

