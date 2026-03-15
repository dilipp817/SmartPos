package com.autobill.smartpos.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Entity: Payment
// Maps to "payments" table in Room database
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = BillEntity::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
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
        Index(value = ["billId"]),
        Index(value = ["restaurantId"]),
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
    ],
)
data class PaymentEntity(
    @PrimaryKey
    val id: Int,
    val billId: Int,
    val restaurantId: Int,
    val amount: Double,
    val paymentMethod: String, // "cash", "card", "upi", "check", etc.
    val paymentType: String, // "full", "partial"
    val status: String, // "successful", "failed", "pending"
    val transactionReference: String?,
    val changeAmount: Double,
    val remainingAmount: Double?,
    val createdAt: String,
)

