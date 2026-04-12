package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Table
// Maps to "tables" table in Room database
@Entity(
    tableName = "tables",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["restaurantId"]),
        Index(value = ["status"]),
        Index(value = ["floor"]),
    ],
)
data class TableEntity(
    @PrimaryKey
    val id: Long,
    val tableNumber: String,
    val floor: Int,
    val capacity: Int,
    val status: String, // "available", "occupied", "reserved"
    val currentOrderId: Long?,
    val lastOccupiedAt: String?,
    val restaurantId: Long,
    val createdAt: String,
    val updatedAt: String,
)

