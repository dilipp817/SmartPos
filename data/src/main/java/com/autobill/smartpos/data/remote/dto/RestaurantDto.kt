package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── GET response ──────────────────────────────────────────────────────────────

/**
 * Nested address block inside GET /api/v1/restaurants/{id} response.
 * Wire key: "address" (nested object).
 *
 * Note: the PATCH request body uses "store_address" (not "address") — see [StoreAddressRequest].
 */
@JsonClass(generateAdapter = true)
data class RestaurantAddressDto(
    @param:Json(name = "building")
    val building: String = "",
    @param:Json(name = "street")
    val street: String = "",
    @param:Json(name = "location")
    val location: String = "",
    @param:Json(name = "zip_code")
    val zipCode: String = "",
)

/**
 * Full restaurant response — GET /api/v1/restaurants/{id}
 *
 * Updated per MOBILE_GUIDE_REVIEW.md 1.1 (April 16, 2026).
 * Fields removed: name, phone, email, logoUrl, timezone, currency, taxRate, isActive, settings
 * Fields added:  outlet_name, displayname, outlet_manager, nested address object
 */
@JsonClass(generateAdapter = true)
data class RestaurantDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "outlet_name")
    val outletName: String,
    @param:Json(name = "displayname")
    val displayName: String = "",
    @param:Json(name = "outlet_manager")
    val outletManager: String = "",
    @param:Json(name = "address")
    val address: RestaurantAddressDto = RestaurantAddressDto(),
    @param:Json(name = "created_at")
    val createdAt: String = "",
    @param:Json(name = "updated_at")
    val updatedAt: String = "",
)

// ── PATCH request ─────────────────────────────────────────────────────────────

/**
 * Nested address object sent in the PATCH request body.
 * Wire key in request: "store_address".
 */
@JsonClass(generateAdapter = true)
data class StoreAddressRequest(
    @param:Json(name = "building")
    val building: String? = null,
    @param:Json(name = "street")
    val street: String? = null,
    @param:Json(name = "location")
    val location: String? = null,
    @param:Json(name = "zip_code")
    val zipCode: String? = null,
)

/**
 * PATCH /api/v1/restaurants/{id} — partial update.
 *
 * Updated per MOBILE_GUIDE_REVIEW.md 1.1 (April 16, 2026).
 * Null fields are omitted by Moshi (serializeNulls = false default).
 */
@JsonClass(generateAdapter = true)
data class UpdateRestaurantRequest(
    @param:Json(name = "outlet_name")
    val outletName: String? = null,
    @param:Json(name = "displayname")
    val displayName: String? = null,
    @param:Json(name = "outlet_manager")
    val outletManager: String? = null,
    @param:Json(name = "store_address")
    val storeAddress: StoreAddressRequest? = null,
)
