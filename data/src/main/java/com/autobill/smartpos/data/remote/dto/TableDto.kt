package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Table
// Maps to API response from GET /restaurants/{restaurantId}/tables
@JsonClass(generateAdapter = true)
data class TableDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "floor")
    val floor: Int,
    @param:Json(name = "capacity")
    val capacity: Int,
    @param:Json(name = "status")
    val status: String, // "available", "occupied", "reserved"
    @param:Json(name = "current_order_id")
    val currentOrderId: Int?,
    @param:Json(name = "last_occupied_at")
    val lastOccupiedAt: String?,
    @param:Json(name = "restaurant_id")
    val restaurantId: Int,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Update Table Status Request
@JsonClass(generateAdapter = true)
data class UpdateTableStatusRequest(
    @param:Json(name = "status")
    val status: String,
    @param:Json(name = "current_order_id")
    val currentOrderId: Int?,
)

// DTO: Paginated Tables Response
@JsonClass(generateAdapter = true)
data class PaginatedTablesDto(
    @param:Json(name = "tables")
    val tables: List<TableDto>,
)

