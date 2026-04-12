package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standard API envelope wrapper.
 * Every backend response is wrapped in:
 * { "success": true, "message": "...", "data": {...}, "error": null }
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @param:Json(name = "success")
    val success: Boolean,
    @param:Json(name = "message")
    val message: String? = null,
    @param:Json(name = "data")
    val data: T? = null,
    @param:Json(name = "error")
    val error: ApiError? = null,
)

@JsonClass(generateAdapter = true)
data class ApiError(
    @param:Json(name = "code")
    val code: String,
    @param:Json(name = "message")
    val message: String,
)

