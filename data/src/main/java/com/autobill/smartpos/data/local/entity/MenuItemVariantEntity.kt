package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Menu Item Variant
// Maps to "menu_item_variants" table in Room database
@Entity(
    tableName = "menu_item_variants",
    foreignKeys = [
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["menuItemId"])],
)
data class MenuItemVariantEntity(
    @PrimaryKey
    val id: Int,
    val menuItemId: Int,
    val name: String,
    val priceModifier: Double,
    val description: String,
)

