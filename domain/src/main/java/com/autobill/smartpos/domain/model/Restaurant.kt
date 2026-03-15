package com.autobill.smartpos.domain.model

// Domain Model: Restaurant
// Independent of database or API structure
// Used in domain layer and business logic
data class Restaurant(
    val id: Int,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val logoUrl: String,
    val timezone: String,
    val currency: String,
    val taxRate: Double,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

