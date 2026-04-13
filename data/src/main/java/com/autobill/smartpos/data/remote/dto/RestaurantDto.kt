package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Nested settings block inside GET /restaurants/{id} response. */
@JsonClass(generateAdapter = true)
data class RestaurantSettingsDto(
    @param:Json(name = "enableTips")
    val enableTips: Boolean = false,
    @param:Json(name = "defaultTipPercentage")
    val defaultTipPercentage: Double = 0.0,
    @param:Json(name = "autoPrintBill")
    val autoPrintBill: Boolean = false,
    @param:Json(name = "taxInclusive")
    val taxInclusive: Boolean = false,
)

/** Full restaurant response — GET /api/v1/restaurants/{id} */
@JsonClass(generateAdapter = true)
data class RestaurantDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "address")
    val address: String,
    @param:Json(name = "phone")
    val phone: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "logoUrl")
    val logoUrl: String?,
    @param:Json(name = "timezone")
    val timezone: String,
    @param:Json(name = "currency")
    val currency: String,
    @param:Json(name = "taxRate")
    val taxRate: Double,
    @param:Json(name = "isActive")
    val isActive: Boolean,
    /** Null for legacy restaurants that predate the settings migration. */
    @param:Json(name = "settings")
    val settings: RestaurantSettingsDto?,
    @param:Json(name = "createdAt")
    val createdAt: String,
    @param:Json(name = "updatedAt")
    val updatedAt: String,
)

// ── Update request ────────────────────────────────────────────────────────────

/**
 * PATCH /api/v1/restaurants/{id} — partial update.
 * Null fields are omitted by Moshi (use @Json serializeNulls = false default).
 */
@JsonClass(generateAdapter = true)
data class UpdateRestaurantRequest(
    @param:Json(name = "taxRate")
    val taxRate: Double? = null,
    @param:Json(name = "settings")
    val settings: UpdateRestaurantSettingsBody? = null,
)

@JsonClass(generateAdapter = true)
data class UpdateRestaurantSettingsBody(
    @param:Json(name = "enableTips")
    val enableTips: Boolean? = null,
    @param:Json(name = "defaultTipPercentage")
    val defaultTipPercentage: Double? = null,
    @param:Json(name = "autoPrintBill")
    val autoPrintBill: Boolean? = null,
    @param:Json(name = "taxInclusive")
    val taxInclusive: Boolean? = null,
)

