package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Bill
// Item 15: removed discountType/discountValue/tableId FK/printedAt/totalTax;
//          added taxAmount/cgstAmount/sgstAmount/restaurantName/updatedAt;
//          status now ISSUED | PARTIAL | PAID | CANCELLED (UPPERCASE)
// Migrated via MIGRATION_4_5 in AppDatabase.
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
    ],
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
