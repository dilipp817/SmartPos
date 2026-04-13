package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Payment
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus

/**
 * Repository contract for payment data operations.
 *
 * ## Idempotency
 * Always generate a reference number ONCE per payment attempt (use [PaymentReferenceGenerator])
 * and persist it before calling [processPayment]. On network-error retries, reuse the SAME
 * reference number. Generate a NEW one only when the returned status is FAILED.
 *
 * ## auto_process behaviour
 * - CASH / UPI / WALLET → pass autoProcess = true (single atomic call, no PATCH needed)
 * - CARD → pass autoProcess = false (creates PENDING; confirm via [processPaymentSuccess])
 */
interface PaymentRepository {

    /**
     * POST /payments
     *
     * [billId]          — nullable; payment may precede bill generation.
     * [referenceNumber] — unique idempotency key (UUID v4 from [PaymentReferenceGenerator]).
     * [autoProcess]     — true for CASH/UPI/WALLET; false for CARD.
     * [changeAmount]    — for CASH: amount tendered − totalAmount; 0 for non-cash.
     */
    suspend fun processPayment(
        billId: Long?,
        orderId: Long,
        paymentMethod: PaymentMethod,
        amount: Double,
        referenceNumber: String,
        autoProcess: Boolean,
        changeAmount: Double = 0.0,
        transactionId: String? = null,
        notes: String? = null,
    ): Result<Payment>

    /**
     * PATCH /payments/{id}/process
     * Confirms a PENDING CARD payment as SUCCESS.
     * Linked bill is auto-set to PAID by the backend.
     */
    suspend fun processPaymentSuccess(id: Long): Result<Payment>

    /**
     * PATCH /payments/{id}/status
     * Valid transitions: PENDING→SUCCESS, PENDING→FAILED, SUCCESS→REFUNDED, FAILED→PENDING.
     */
    suspend fun updatePaymentStatus(
        id: Long,
        status: PaymentStatus,
        transactionId: String? = null,
        notes: String? = null,
    ): Result<Payment>

    /** PATCH /payments/{id}/refund — SUCCESS → REFUNDED */
    suspend fun refundPayment(id: Long): Result<Payment>

    /** GET /payments/{id} */
    suspend fun getPaymentById(id: Long): Result<Payment>

    /** GET /payments/bill/{billId}?offset=0&limit=20 */
    suspend fun getPaymentsForBill(billId: Long): Result<List<Payment>>

    /** GET /payments/order/{orderId}?offset=0&limit=20 */
    suspend fun getPaymentsForOrder(orderId: Long): Result<List<Payment>>
}

