package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Restaurant
// Maps to "restaurants" table in Room database
@Entity(
    tableName = "restaurants",
    indices = [Index(value = ["id"])],
)
data class RestaurantEntity(
    @PrimaryKey
    val id: Long,
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

