package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.PaymentDto
import com.autobill.smartpos.data.remote.dto.PaymentListDto
import com.autobill.smartpos.data.remote.dto.ProcessPaymentRequest
import com.autobill.smartpos.data.remote.dto.UpdatePaymentStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PaymentApiService {

    /**
     * POST /api/v1/payments
     *
     * **Idempotency:** always generate a UUID v4, store it locally, then pass it as
     * [ProcessPaymentRequest.referenceNumber]. Reuse the same UUID on any retry.
     * Backend deduplicates on ANY existing status — if a payment with the same
     * reference_number already exists (PENDING, SUCCESS, or FAILED), the existing
     * record is returned without creating a duplicate (fixed April 12, 2026).
     *
     * **If the returned payment is FAILED:** the original attempt genuinely failed —
     * generate a new UUID and create a fresh payment; do NOT reuse the old reference_number.
     *
     * **auto_process behaviour** (set in [ProcessPaymentRequest.autoProcess]):
     * - `true`  → CASH / UPI / WALLET — payment is created and immediately marked SUCCESS
     *             in a single atomic call. Bill status is updated in the same transaction.
     *             No follow-up PATCH needed.
     * - `false` → CARD — payment is created as PENDING. Call [processPaymentSuccess] or
     *             [updatePaymentStatus] after gateway confirmation.
     */
    @POST("payments")
    suspend fun processPayment(
        @Body request: ProcessPaymentRequest,
    ): ApiResponse<PaymentDto>

    /** GET /api/v1/payments/{id} */
    @GET("payments/{id}")
    suspend fun getPaymentById(
        @Path("id") id: Long,
    ): ApiResponse<PaymentDto>

    /** GET /api/v1/payments/bill/{billId}?offset=0&limit=20 */
    @GET("payments/bill/{billId}")
    suspend fun getPaymentsForBill(
        @Path("billId") billId: Long,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<PaymentListDto>

    /** GET /api/v1/payments/order/{orderId}?offset=0&limit=20 */
    @GET("payments/order/{orderId}")
    suspend fun getPaymentsForOrder(
        @Path("orderId") orderId: Long,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<PaymentListDto>

    /**
     * PATCH /api/v1/payments/{id}/status
     * Explicit status transitions (backendapi.md §6):
     *   PENDING → SUCCESS
     *   PENDING → FAILED
     *   SUCCESS → REFUNDED
     *   FAILED  → PENDING  (re-attempt — generate new reference_number for the retry)
     * When set to SUCCESS, linked bill auto-transitions (PARTIAL or PAID).
     */
    @PATCH("payments/{id}/status")
    suspend fun updatePaymentStatus(
        @Path("id") id: Long,
        @Body request: UpdatePaymentStatusRequest,
    ): ApiResponse<PaymentDto>

    /**
     * PATCH /api/v1/payments/{id}/process
     * Shortcut to mark payment as SUCCESS and auto-mark linked bill as PAID.
     */
    @PATCH("payments/{id}/process")
    suspend fun processPaymentSuccess(
        @Path("id") id: Long,
    ): ApiResponse<PaymentDto>

    /**
     * PATCH /api/v1/payments/{id}/refund
     * Transitions SUCCESS → REFUNDED.
     */
    @PATCH("payments/{id}/refund")
    suspend fun refundPayment(
        @Path("id") id: Long,
    ): ApiResponse<PaymentDto>
}

