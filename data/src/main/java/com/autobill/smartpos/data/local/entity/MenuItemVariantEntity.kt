package com.autobill.smartpos.data.local.entity

// DEPRECATED — Item 15 (April 12, 2026)
// Variants are not part of the POS v1 order flow (removed in OrderItem domain model).
// Kept as a plain data class for reference only.
// Do NOT register in AppDatabase.
@Deprecated(
    message = "MenuItemVariant caching removed. Variants not supported in POS v1. Not registered in AppDatabase.",
    level = DeprecationLevel.WARNING,
)
data class MenuItemVariantEntity(
    val id: Long,
    val menuItemId: Long,
    val name: String,
    val priceModifier: Double,
    val description: String,
)
