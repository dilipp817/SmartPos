package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Payment
// ForeignKey constraints to OrderEntity and BillEntity were removed (v2 migration).
// Payments may arrive from the server before their parent order/bill is cached locally.
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["billId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class PaymentEntity(
    @PrimaryKey
    val id: Long,
    val orderId: Long,
    val billId: Long?,              // nullable — payment may precede bill creation
    val paymentMethod: String,      // CASH | CARD | UPI | WALLET
    val amount: Double,
    val status: String,             // PENDING | SUCCESS | FAILED | REFUNDED
    val referenceNumber: String,
    val transactionId: String?,
    val changeAmount: Double,       // cash returned to customer; 0 for non-cash
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
)
