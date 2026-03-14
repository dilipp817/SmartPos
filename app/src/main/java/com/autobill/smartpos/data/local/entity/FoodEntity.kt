package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [Index(value = ["restaurantId"])],
)
data class FoodEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val price: Double,
    val restaurantId: Int,
)

