package com.autobill.smartpos.domain.model

// Domain Model: Table
// Independent of database or API structure
data class Table(
    val id: Long,
    val tableNumber: String,
    val floor: Int,
    val capacity: Int,
    val status: TableStatus, // Available, Occupied, Reserved
    val currentOrderId: Long?,
    val lastOccupiedAt: String?,
    val restaurantId: Long,
    val createdAt: String,
    val updatedAt: String,
    val version: Long = 0,  // read-only; used for optimistic locking (409 CONFLICT handling)
)

// Table status enum
// Values are UPPERCASE to match backend API
enum class TableStatus(val value: String) {
    AVAILABLE("AVAILABLE"),
    OCCUPIED("OCCUPIED"),
    RESERVED("RESERVED"),
    CLEANING("CLEANING"),
    MAINTENANCE("MAINTENANCE");

    companion object {
        fun fromValue(value: String): TableStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: AVAILABLE
        }
    }
}

