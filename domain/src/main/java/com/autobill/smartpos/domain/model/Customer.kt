package com.autobill.smartpos.domain.model

// Domain Model: Customer
// Independent of database or API structure
data class Customer(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val address: String,
    val loyaltyPoints: Int,
    val totalSpent: Double,
    val totalOrders: Int,
    val createdAt: String,
) {
    val fullName: String
        get() = "$firstName $lastName"
}

