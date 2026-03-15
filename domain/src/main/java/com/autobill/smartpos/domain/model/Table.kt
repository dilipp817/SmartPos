package com.autobill.smartpos.domain.model

// Domain Model: Table
// Independent of database or API structure
data class Table(
    val id: Int,
    val tableNumber: String,
    val floor: Int,
    val capacity: Int,
    val status: TableStatus, // Available, Occupied, Reserved
    val currentOrderId: Int?,
    val lastOccupiedAt: String?,
    val restaurantId: Int,
    val createdAt: String,
    val updatedAt: String,
)

// Table status enum
enum class TableStatus(val value: String) {
    AVAILABLE("available"),
    OCCUPIED("occupied"),
    RESERVED("reserved"),
    CLEANING("cleaning"),
    MAINTENANCE("maintenance");

    companion object {
        fun fromValue(value: String): TableStatus {
            return entries.find { it.value == value } ?: AVAILABLE
        }
    }
}

