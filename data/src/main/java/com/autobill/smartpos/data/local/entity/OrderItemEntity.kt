package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Order Item
// Maps to "order_items" table in Room database
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MenuItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuItemId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = MenuItemVariantEntity::class,
            parentColumns = ["id"],
            childColumns = ["variantId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["menuItemId"]),
        Index(value = ["variantId"]),
    ],
)
data class OrderItemEntity(
    @PrimaryKey
    val id: Int,
    val orderId: Int,
    val menuItemId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val variantId: Int?,
    val specialInstructions: String?,
    val subtotal: Double,
)

