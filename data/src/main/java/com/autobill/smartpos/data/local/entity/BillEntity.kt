package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Bill
// Maps to "bills" table in Room database
@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["id"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["id"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["restaurantId"]),
        Index(value = ["tableId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class BillEntity(
    @PrimaryKey
    val id: Int,
    val billNumber: String,
    val orderId: Int,
    val tableId: Int?,
    val restaurantId: Int,
    val subtotal: Double,
    val discountType: String?, // "percentage", "fixed"
    val discountValue: Double,
    val discountAmount: Double,
    val totalTax: Double,
    val totalAmount: Double,
    val status: String, // "unpaid", "partial", "paid"
    val createdAt: String,
    val printedAt: String?,
)

