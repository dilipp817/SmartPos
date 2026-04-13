package com.autobill.smartpos.domain.model

/**
 * Restaurant-level feature flags and configuration returned by GET /restaurants/{id}.
 * These control app behaviour — e.g. show tip input only when [enableTips] is true.
 */
data class RestaurantSettings(
    /** Whether the tip UI is shown to staff on the payment screen. */
    val enableTips: Boolean = false,
    /** Default tip percentage pre-filled in the tip input (only relevant when [enableTips]=true). */
    val defaultTipPercentage: Double = 0.0,
    /** Whether the app should trigger a print job after a bill is generated. */
    val autoPrintBill: Boolean = false,
    /** If true, displayed prices already include tax — affects bill summary display. */
    val taxInclusive: Boolean = false,
)

/** Domain Model: Restaurant outlet details. */
data class Restaurant(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    /** May be null if the restaurant has not uploaded a logo. */
    val logoUrl: String?,
    val timezone: String,
    /** ISO 4217 currency code — e.g. "INR". */
    val currency: String,
    /** Combined tax rate percentage — e.g. 18.0 for 9% CGST + 9% SGST. */
    val taxRate: Double,
    val isActive: Boolean,
    val settings: RestaurantSettings,
    val createdAt: String,
    val updatedAt: String,
)

/**
 * Parameters that staff / managers can update via PATCH /restaurants/{id}.
 * Null fields are omitted from the request (partial update).
 */
data class UpdateRestaurantSettingsRequest(
    val taxRate: Double? = null,
    val enableTips: Boolean? = null,
    val defaultTipPercentage: Double? = null,
    val autoPrintBill: Boolean? = null,
    val taxInclusive: Boolean? = null,
)

