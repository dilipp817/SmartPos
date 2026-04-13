package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Payment
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus
import com.autobill.smartpos.domain.repository.PaymentRepository
import javax.inject.Inject

/**
 * Use case: Submit a payment for a bill.
 *
 * Always generate a [referenceNumber] via [PaymentReferenceGenerator.generate()] BEFORE
 * calling this — store it locally so the same value can be passed on network-error retries.
 *
 * - CASH / UPI / WALLET → pass [autoProcess] = true (single atomic call → SUCCESS + bill PAID)
 * - CARD → pass [autoProcess] = false (creates PENDING; confirm with [ConfirmCardPaymentUseCase])
 */
class ProcessPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(
        billId: Long?,
        orderId: Long,
        paymentMethod: PaymentMethod,
        amount: Double,
        referenceNumber: String,
        autoProcess: Boolean,
        changeAmount: Double = 0.0,
        transactionId: String? = null,
        notes: String? = null,
    ): Result<Payment> = repository.processPayment(
        billId          = billId,
        orderId         = orderId,
        paymentMethod   = paymentMethod,
        amount          = amount,
        referenceNumber = referenceNumber,
        autoProcess     = autoProcess,
        changeAmount    = changeAmount,
        transactionId   = transactionId,
        notes           = notes,
    )
}

/**
 * Use case: Confirm a pending CARD payment as successful.
 *
 * PATCH /payments/{id}/process — transitions PENDING → SUCCESS.
 * The linked bill is automatically set to PAID by the backend.
 */
class ConfirmCardPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(id: Long): Result<Payment> =
        repository.processPaymentSuccess(id)
}

/**
 * Use case: Explicitly update a payment's status.
 *
 * Valid transitions:
 *  PENDING → SUCCESS | FAILED
 *  SUCCESS → REFUNDED
 *  FAILED  → PENDING  (re-attempt — generate new reference_number first)
 */
class UpdatePaymentStatusUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(
        id: Long,
        status: PaymentStatus,
        transactionId: String? = null,
        notes: String? = null,
    ): Result<Payment> = repository.updatePaymentStatus(id, status, transactionId, notes)
}

/** Use case: Refund a successfully completed payment (SUCCESS → REFUNDED). */
class RefundPaymentUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(id: Long): Result<Payment> = repository.refundPayment(id)
}

/** Use case: Fetch all payments linked to a specific bill. */
class GetPaymentsForBillUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(billId: Long): Result<List<Payment>> =
        repository.getPaymentsForBill(billId)
}

/** Use case: Fetch all payments linked to a specific order. */
class GetPaymentsForOrderUseCase @Inject constructor(
    private val repository: PaymentRepository,
) {
    suspend operator fun invoke(orderId: Long): Result<List<Payment>> =
        repository.getPaymentsForOrder(orderId)
}

