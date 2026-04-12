package com.autobill.smartpos.domain.model

// Domain Model: Payment
// Item 6: restructured — removed paymentType/remainingAmount;
//          added orderId/referenceNumber/transactionId/notes/updatedAt; billId is now nullable
// Item 6-partial: changeAmount added (cash returned to customer when they overpay)
//                 Backend uses BigDecimal; we use Double for consistency with the rest of the codebase.
//                 Only meaningful for CASH payments; 0.0 for all other methods.
data class Payment(
    val id: Long,
    val billId: Long?,              // nullable — payment may precede bill creation
    val orderId: Long,
    val paymentMethod: PaymentMethod,
    val amount: Double,
    val status: PaymentStatus,
    val referenceNumber: String,    // required by backend (non-blank)
    val transactionId: String?,
    val changeAmount: Double = 0.0, // cash returned to customer; 0 for non-cash
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
)

// Item 4: UPPERCASE values; CHEQUE + DIGITAL_WALLET removed; WALLET added
enum class PaymentMethod(val value: String) {
    CASH("CASH"),
    CARD("CARD"),
    UPI("UPI"),
    WALLET("WALLET");

    companion object {
        fun fromValue(value: String): PaymentMethod {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: CASH
        }
    }
}

// Item 3: renamed SUCCESSFUL→SUCCESS, added REFUNDED, UPPERCASE
enum class PaymentStatus(val value: String) {
    PENDING("PENDING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    REFUNDED("REFUNDED");

    companion object {
        fun fromValue(value: String): PaymentStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

// Item 5: PaymentType REMOVED — backend removed payment_type from the API.
// Whether a payment is full or partial is inferred by comparing amount vs bill.total_amount.
