package com.autobill.smartpos.data.local.entity

// DEPRECATED — Item 15 (April 12, 2026)
// Customer data is no longer cached locally. The concept of a local customer entity
// was removed because the backend confirmed customers are not part of the POS v1 flow.
// This class is kept as a plain data class for reference only.
// Do NOT register in AppDatabase. Do NOT add new FKs pointing to this class.
@Deprecated(
    message = "Customer local caching is removed. Not registered in AppDatabase.",
    level = DeprecationLevel.WARNING,
)
data class CustomerEntity(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val address: String,
    val loyaltyPoints: Int,
    val totalSpent: Double,
    val totalOrders: Int,
    val restaurantId: Long,
    val createdAt: String,
)
