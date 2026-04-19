package com.autobill.smartpos.domain.model

/** Nested address block returned by GET /restaurants/{id} */
data class RestaurantAddress(
    val building: String,
    val street: String,
    val location: String,
    val zipCode: String,
) {
    /** Single-line display string */
    val formatted: String
        get() = buildString {
            if (building.isNotBlank()) { append(building); append(", ") }
            if (street.isNotBlank()) { append(street); append(", ") }
            if (location.isNotBlank()) { append(location) }
            if (zipCode.isNotBlank()) {
                // Only add " - " separator when there is already preceding address content;
                // avoids "- 560001" when only ZIP is present.
                if (isNotEmpty()) append(" - ") else Unit
                append(zipCode)
            }
        }.trimEnd(',', ' ')
}

/**
 * Domain Model: Restaurant outlet details.
 *
 * Field names match the backend GET /api/v1/restaurants/{id} response
 * (MOBILE_GUIDE_REVIEW.md 1.1 — April 16, 2026).
 *
 * Fields removed in the new model (backend no longer returns them):
 *   phone, email, logoUrl, timezone, currency, taxRate, isActive, settings
 *
 * These will be re-added after the joint meeting resolves Section 2.1 of the review.
 */
data class Restaurant(
    val id: Long,
    /** outlet_name — e.g. "Spice Garden" */
    val outletName: String,
    /** displayname — e.g. "Spice Garden — MG Road" */
    val displayName: String,
    /** outlet_manager */
    val outletManager: String,
    val address: RestaurantAddress,
    val createdAt: String,
    val updatedAt: String,
)

/**
 * Parameters for PATCH /api/v1/restaurants/{id}.
 * All fields are nullable — only non-null values are sent in the request body.
 *
 * Editable until joint-decision fields (phone, email, taxRate) are confirmed (Section 2.1).
 */
data class UpdateRestaurantSettingsRequest(
    val outletName: String? = null,
    val displayName: String? = null,
    val outletManager: String? = null,
    val building: String? = null,
    val street: String? = null,
    val location: String? = null,
    val zipCode: String? = null,
)
