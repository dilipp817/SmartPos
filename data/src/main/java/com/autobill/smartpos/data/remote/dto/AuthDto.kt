package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Request DTOs ─────────────────────────────────────────────────────────────

/**
 * Request body for POST /api/v1/auth/login
 * deviceId and deviceType are optional — sent for multi-counter tracking.
 */
@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @param:Json(name = "username")
    val username: String,
    @param:Json(name = "password")
    val password: String,
    @param:Json(name = "device_id")
    val deviceId: String? = null,
    @param:Json(name = "device_type")
    val deviceType: String? = null,
)

// ─── Response DTOs ─────────────────────────────────────────────────────────────

/**
 * Response data from POST /api/v1/auth/login
 * restaurantId is the critical field — app uses this for ALL restaurant-scoped API calls.
 * null restaurantId = super_admin user.
 */
@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "username")
    val username: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "role")
    val role: String,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long?,   // ← NEVER hardcode this; read from response
    @param:Json(name = "token")
    val token: String,
    @param:Json(name = "expires_in")
    val expiresIn: Long,
    @param:Json(name = "expires_at")
    val expiresAt: Long = 0L,   // absolute Unix epoch seconds — backend shipped April 17, 2026
    @param:Json(name = "device_id")
    val deviceId: String? = null,
    @param:Json(name = "device_type")
    val deviceType: String? = null,
)

/**
 * Response data from GET /api/v1/auth/me
 * Used to recover restaurantId without re-login.
 */
@JsonClass(generateAdapter = true)
data class UserInfoResponseDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "username")
    val username: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "role")
    val role: String,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long?,
    @param:Json(name = "device_id")
    val deviceId: String? = null,
    @param:Json(name = "device_type")
    val deviceType: String? = null,
    @param:Json(name = "is_active")
    val isActive: Boolean = true,
)

/**
 * Response data from POST /api/v1/auth/validate
 */
@JsonClass(generateAdapter = true)
data class TokenValidationResponseDto(
    @param:Json(name = "valid")
    val valid: Boolean,
    @param:Json(name = "username")
    val username: String,
    @param:Json(name = "user_id")
    val userId: Long,
    @param:Json(name = "role")
    val role: String,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long?,
)
