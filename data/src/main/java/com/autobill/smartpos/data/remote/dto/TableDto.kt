package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Table response
// Status values are UPPERCASE: AVAILABLE | OCCUPIED | RESERVED | CLEANING | MAINTENANCE
// floor + last_occupied_at added by backend in V22 migration (BACKEND_ALIGNMENT.md Item 3)
@JsonClass(generateAdapter = true)
data class TableDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "floor")
    val floor: Int = 1,
    @param:Json(name = "capacity")
    val capacity: Int,
    @param:Json(name = "status")
    val status: String, // AVAILABLE | OCCUPIED | RESERVED | CLEANING | MAINTENANCE
    @param:Json(name = "current_order_id")
    val currentOrderId: Long? = null,
    @param:Json(name = "last_occupied_at")
    val lastOccupiedAt: String? = null, // null = never occupied; set server-side on table release
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
    @param:Json(name = "version")
    val version: Long = 1,
)

// DTO: Table list response wrapper
// GET /api/v1/restaurants/{restaurantId}/tables
// API shape: { "tables": [...], "total": 10 }
// ⚠️ backendapi.md does not include "status" in the data body — default guards against crash.
@JsonClass(generateAdapter = true)
data class TableListDto(
    @param:Json(name = "tables")
    val tables: List<TableDto>,
    @param:Json(name = "total")
    val total: Int,
    @param:Json(name = "status")
    val status: String = "success",
)

// DTO: Create / Update Table Request
@JsonClass(generateAdapter = true)
data class CreateTableRequest(
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "floor")
    val floor: Int = 1,             // optional; defaults to 1 (backend V22)
    @param:Json(name = "capacity")
    val capacity: Int,
    @param:Json(name = "status")
    val status: String = "AVAILABLE",
)
// Note: Update table status uses PATCH .../tables/{id}/status?new_status=OCCUPIED (query param, no body)
