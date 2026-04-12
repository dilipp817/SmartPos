package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Payment
// Item 15: removed paymentType/remainingAmount/restaurantId FK;
//          billId is now nullable (payment may precede bill);
//          added orderId FK, referenceNumber, transactionId, notes, updatedAt;
//          changeAmount kept; status values UPPERCASE
// Migrated via MIGRATION_4_5 in AppDatabase.
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
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
