package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Customer
// Maps to API response from POST/GET /restaurants/{restaurantId}/customers
@JsonClass(generateAdapter = true)
data class CustomerDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "first_name")
    val firstName: String,
    @param:Json(name = "last_name")
    val lastName: String,
    @param:Json(name = "phone")
    val phone: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "address")
    val address: String,
    @param:Json(name = "loyalty_points")
    val loyaltyPoints: Int,
    @param:Json(name = "total_spent")
    val totalSpent: Double,
    @param:Json(name = "total_orders")
    val totalOrders: Int,
    @param:Json(name = "created_at")
    val createdAt: String,
)

// DTO: Create Customer Request
@JsonClass(generateAdapter = true)
data class CreateCustomerRequest(
    @param:Json(name = "first_name")
    val firstName: String,
    @param:Json(name = "last_name")
    val lastName: String,
    @param:Json(name = "phone")
    val phone: String,
    @param:Json(name = "email")
    val email: String,
    @param:Json(name = "address")
    val address: String,
)

// DTO: Paginated Customers Response
@JsonClass(generateAdapter = true)
data class PaginatedCustomersDto(
    @param:Json(name = "customers")
    val customers: List<CustomerDto>,
    @param:Json(name = "pagination")
    val pagination: PaginationDto,
)

