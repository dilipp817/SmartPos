package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Payment
// Maps to API response from POST /restaurants/{restaurantId}/payments
@JsonClass(generateAdapter = true)
data class PaymentDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "bill_id")
    val billId: Int,
    @param:Json(name = "amount")
    val amount: Double,
    @param:Json(name = "payment_method")
    val paymentMethod: String, // "cash", "card", "upi", "check", etc.
    @param:Json(name = "payment_type")
    val paymentType: String, // "full", "partial"
    @param:Json(name = "status")
    val status: String, // "successful", "failed", "pending"
    @param:Json(name = "transaction_reference")
    val transactionReference: String?,
    @param:Json(name = "change_amount")
    val changeAmount: Double,
    @param:Json(name = "remaining_amount")
    val remainingAmount: Double?,
    @param:Json(name = "created_at")
    val createdAt: String,
)

// DTO: Process Payment Request (Full Payment)
@JsonClass(generateAdapter = true)
data class ProcessPaymentRequest(
    @param:Json(name = "bill_id")
    val billId: Int,
    @param:Json(name = "payment_method")
    val paymentMethod: String,
    @param:Json(name = "payment_type")
    val paymentType: String,
    @param:Json(name = "amount")
    val amount: Double,
    @param:Json(name = "card_details")
    val cardDetails: CardDetailsDto?,
    @param:Json(name = "transaction_reference")
    val transactionReference: String?,
    @param:Json(name = "notes")
    val notes: String?,
)

// DTO: Card Details
@JsonClass(generateAdapter = true)
data class CardDetailsDto(
    @param:Json(name = "last_4_digits")
    val last4Digits: String,
    @param:Json(name = "card_type")
    val cardType: String, // "visa", "mastercard", etc.
)

// DTO: Process Partial Payment Request
@JsonClass(generateAdapter = true)
data class ProcessPartialPaymentRequest(
    @param:Json(name = "bill_id")
    val billId: Int,
    @param:Json(name = "amount")
    val amount: Double,
    @param:Json(name = "payment_method")
    val paymentMethod: String,
)

// DTO: Refund Payment Request
@JsonClass(generateAdapter = true)
data class RefundPaymentRequest(
    @param:Json(name = "reason")
    val reason: String,
    @param:Json(name = "amount")
    val amount: Double,
)

// DTO: Refund Response
@JsonClass(generateAdapter = true)
data class RefundPaymentResponse(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "original_payment_id")
    val originalPaymentId: Int,
    @param:Json(name = "bill_id")
    val billId: Int,
    @param:Json(name = "refund_amount")
    val refundAmount: Double,
    @param:Json(name = "status")
    val status: String,
    @param:Json(name = "reason")
    val reason: String,
    @param:Json(name = "created_at")
    val createdAt: String,
)

