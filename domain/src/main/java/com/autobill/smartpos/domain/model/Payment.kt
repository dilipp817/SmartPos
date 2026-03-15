package com.autobill.smartpos.domain.model

// Domain Model: Payment
// Independent of database or API structure
data class Payment(
    val id: Int,
    val billId: Int,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val paymentType: PaymentType,
    val status: PaymentStatus,
    val transactionReference: String?,
    val changeAmount: Double,
    val remainingAmount: Double?,
    val createdAt: String,
)

enum class PaymentMethod(val value: String) {
    CASH("cash"),
    CARD("card"),
    UPI("upi"),
    CHEQUE("check"),
    DIGITAL_WALLET("digital_wallet");

    companion object {
        fun fromValue(value: String): PaymentMethod {
            return entries.find { it.value == value } ?: CASH
        }
    }
}

enum class PaymentType(val value: String) {
    FULL("full"),
    PARTIAL("partial");

    companion object {
        fun fromValue(value: String): PaymentType {
            return entries.find { it.value == value } ?: FULL
        }
    }
}

enum class PaymentStatus(val value: String) {
    SUCCESSFUL("successful"),
    FAILED("failed"),
    PENDING("pending");

    companion object {
        fun fromValue(value: String): PaymentStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

