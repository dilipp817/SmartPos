package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Payment
// Maps to API response from POST /restaurants/{restaurantId}/payments
// change_amount added by backend in V20 migration
@JsonClass(generateAdapter = true)
data class PaymentDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "bill_id")
    val billId: Long? = null,
    @param:Json(name = "order_id")
    val orderId: Long,
    @param:Json(name = "payment_method")
    val paymentMethod: String, // CASH | CARD | UPI | WALLET
    @param:Json(name = "amount")
    val amount: Double,
    @param:Json(name = "status")
    val status: String, // PENDING | SUCCESS | FAILED | REFUNDED
    @param:Json(name = "transaction_id")
    val transactionId: String? = null,
    @param:Json(name = "reference_number")
    val referenceNumber: String,
    @param:Json(name = "change_amount")
    val changeAmount: Double = 0.0, // cash returned to customer; 0 for non-cash
    @param:Json(name = "notes")
    val notes: String? = null,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Process Payment Request
// POST /api/v1/payments
// Payment method UPPERCASE: CASH | CARD | UPI | WALLET
//
// Idempotency: always generate a UUID v4 for reference_number BEFORE calling.
// Dedup rule (backend fixed April 12, 2026): if a payment with the same reference_number
// already exists in ANY status, the existing record is returned — no duplicate created.
// → If returned status is FAILED, generate a NEW reference_number for a fresh attempt.
//
// auto_process:
//   true  → payment created AND immediately marked SUCCESS in one atomic call (no PATCH needed).
//           Use for CASH, UPI, WALLET — success is known at call time.
//   false → payment created as PENDING; confirm via PATCH /payments/{id}/process.
//           Use for CARD (async gateway) or when explicit two-step control is needed.
@JsonClass(generateAdapter = true)
data class ProcessPaymentRequest(
    @param:Json(name = "bill_id")
    val billId: Long? = null,         // Optional — if paying against a specific bill
    @param:Json(name = "order_id")
    val orderId: Long,                 // Required
    @param:Json(name = "payment_method")
    val paymentMethod: String,         // CASH | CARD | UPI | WALLET
    @param:Json(name = "amount")
    val amount: Double,
    @param:Json(name = "reference_number")
    val referenceNumber: String,       // Required UUID v4 — dedup key (see above)
    @param:Json(name = "change_amount")
    val changeAmount: Double = 0.0,    // Cash returned to customer; omit/send 0 for non-cash
    @param:Json(name = "transaction_id")
    val transactionId: String? = null,
    @param:Json(name = "notes")
    val notes: String? = null,
    @param:Json(name = "auto_process")
    val autoProcess: Boolean = false,  // true = single-step atomic SUCCESS (CASH/UPI/WALLET)
                                       // false = two-step PENDING → SUCCESS via PATCH  (CARD)
)

// DTO: Update Payment Status Request
// PATCH /api/v1/payments/{id}/status
@JsonClass(generateAdapter = true)
data class UpdatePaymentStatusRequest(
    @param:Json(name = "status")
    val status: String,  // SUCCESS | FAILED | REFUNDED
    @param:Json(name = "transaction_id")
    val transactionId: String? = null,
    @param:Json(name = "notes")
    val notes: String? = null,
)

// DTO: Payment list meta (pagination for payment lists)
@JsonClass(generateAdapter = true)
data class PaymentListMetaDto(
    @param:Json(name = "total")
    val total: Int,
    @param:Json(name = "limit")
    val limit: Int,
    @param:Json(name = "offset")
    val offset: Int,
    @param:Json(name = "has_more")
    val hasMore: Boolean,
)

// DTO: Payment list response
// GET /api/v1/payments/bill/{billId}  |  GET /api/v1/payments/order/{orderId}
@JsonClass(generateAdapter = true)
data class PaymentListDto(
    @param:Json(name = "payments")
    val payments: List<PaymentDto>,
    @param:Json(name = "meta")
    val meta: PaymentListMetaDto,
)
