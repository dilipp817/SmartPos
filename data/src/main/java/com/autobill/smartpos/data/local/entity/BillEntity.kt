package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Bill
// ForeignKey constraints to OrderEntity and RestaurantEntity were removed (v2 migration).
// Bills may be fetched from the server before their parent order is cached locally,
// which caused foreign key constraint failures. Room is a server-data cache here.
@Entity(
    tableName = "bills",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["restaurantId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class BillEntity(
    @PrimaryKey
    val id: Long,
    val billNumber: String,
    val orderId: Long,
    val restaurantId: Long,
    val restaurantName: String?,
    val subtotal: Double,
    val taxAmount: Double,
    val cgstAmount: Double,         // 9% CGST
    val sgstAmount: Double,         // 9% SGST
    val discountAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,         // cumulative paid so far; server-managed
    val remainingAmount: Double,    // totalAmount - paidAmount; server-managed
    val status: String,             // ISSUED | PARTIAL | PAID | CANCELLED
    val createdAt: String,
    val updatedAt: String,
)
