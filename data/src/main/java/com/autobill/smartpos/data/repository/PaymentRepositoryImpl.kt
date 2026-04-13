package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.remote.PaymentApiService
import com.autobill.smartpos.data.remote.dto.ProcessPaymentRequest
import com.autobill.smartpos.data.remote.dto.UpdatePaymentStatusRequest
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Payment
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus
import com.autobill.smartpos.domain.repository.PaymentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PaymentRepository implementation.
 *
 * ## Idempotency
 * The caller is responsible for generating and persisting the [referenceNumber] BEFORE calling
 * [processPayment]. On network-error retries, pass the same reference number. Generate a new
 * one only when the returned status is FAILED.
 *
 * ## auto_process
 * - true  → CASH / UPI / WALLET — single atomic SUCCESS call (no PATCH needed).
 * - false → CARD — creates PENDING; confirm via [processPaymentSuccess].
 */
@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val apiService: PaymentApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PaymentRepository {

    override suspend fun processPayment(
        billId: Long?,
        orderId: Long,
        paymentMethod: PaymentMethod,
        amount: Double,
        referenceNumber: String,
        autoProcess: Boolean,
        changeAmount: Double,
        transactionId: String?,
        notes: String?,
    ): Result<Payment> = withContext(ioDispatcher) {
        try {
            val request = ProcessPaymentRequest(
                billId          = billId,
                orderId         = orderId,
                paymentMethod   = paymentMethod.value,
                amount          = amount,
                referenceNumber = referenceNumber,
                changeAmount    = changeAmount,
                transactionId   = transactionId,
                notes           = notes,
                autoProcess     = autoProcess,
            )
            val response = apiService.processPayment(request)
            val dto = checkNotNull(response.data) {
                response.message ?: "Payment failed"
            }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun processPaymentSuccess(id: Long): Result<Payment> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.processPaymentSuccess(id)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Failed to confirm payment"
                }
                Result.Success(dto.toDomain())
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    override suspend fun updatePaymentStatus(
        id: Long,
        status: PaymentStatus,
        transactionId: String?,
        notes: String?,
    ): Result<Payment> = withContext(ioDispatcher) {
        try {
            val request = UpdatePaymentStatusRequest(
                status        = status.value,
                transactionId = transactionId,
                notes         = notes,
            )
            val response = apiService.updatePaymentStatus(id, request)
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update payment status"
            }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun refundPayment(id: Long): Result<Payment> = withContext(ioDispatcher) {
        try {
            val response = apiService.refundPayment(id)
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to process refund"
            }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getPaymentById(id: Long): Result<Payment> = withContext(ioDispatcher) {
        try {
            val response = apiService.getPaymentById(id)
            val dto = checkNotNull(response.data) { response.message ?: "Payment not found" }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getPaymentsForBill(billId: Long): Result<List<Payment>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getPaymentsForBill(billId)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch payments"
                }
                Result.Success(dto.payments.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    override suspend fun getPaymentsForOrder(orderId: Long): Result<List<Payment>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getPaymentsForOrder(orderId)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch payments"
                }
                Result.Success(dto.payments.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }
}

