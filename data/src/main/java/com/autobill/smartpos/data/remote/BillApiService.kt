package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.AddBillItemRequest
import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.BillDto
import com.autobill.smartpos.data.remote.dto.BillSummaryDto
import com.autobill.smartpos.data.remote.dto.CreateBillRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BillApiService {

    /**
     * POST /api/v1/bills
     * Manual bill creation. Prefer OrderApiService.generateBill() for auto-computed amounts.
     */
    @POST("bills")
    suspend fun createBill(
        @Body request: CreateBillRequest,
    ): ApiResponse<BillDto>

    /**
     * GET /api/v1/bills?status=ISSUED
     *
     * ⚠️ `status` is REQUIRED — omitting it (passing null) causes the backend to return
     * an empty list silently (no error). Always pass an explicit value.
     *
     * Valid values: ISSUED | PARTIAL | PAID | CANCELLED
     * Use [BillStatus.value] at the call site.
     *
     * TODO (backend Q&A): Confirm whether a `?status=ALL` or omitting the param should
     * return all bills, or whether separate calls per status are the intended design.
     */
    @GET("bills")
    suspend fun getAllBills(
        @Query("status") status: String,
    ): ApiResponse<List<BillSummaryDto>>

    /** GET /api/v1/bills/{id} */
    @GET("bills/{id}")
    suspend fun getBillById(
        @Path("id") id: Long,
    ): ApiResponse<BillDto>

    /** GET /api/v1/bills/number/{billNumber} */
    @GET("bills/number/{billNumber}")
    suspend fun getBillByNumber(
        @Path("billNumber") billNumber: String,
    ): ApiResponse<BillDto>

    // NOTE: PUT /bills/{id} and DELETE /bills/{id} were present in v1.0 but were explicitly
    // removed from the backend in v1.1 (April 12, 2026) as dangerous operations.
    // Do NOT re-add them. Use PATCH /bills/{id}/cancel for cancellation.

    /** PATCH /api/v1/bills/{id}/paid — no body required */
    @PATCH("bills/{id}/paid")
    suspend fun markBillAsPaid(
        @Path("id") id: Long,
    ): ApiResponse<BillDto>

    /** PATCH /api/v1/bills/{id}/cancel — no body required */
    @PATCH("bills/{id}/cancel")
    suspend fun cancelBill(
        @Path("id") id: Long,
    ): ApiResponse<BillDto>

    /** POST /api/v1/bills/{id}/items — body is a list
     * ⚠️ NOT in backendapi.md — verify with backend before calling. */
    @POST("bills/{id}/items")
    suspend fun addItemsToBill(
        @Path("id") id: Long,
        @Body items: List<AddBillItemRequest>,
    ): ApiResponse<BillDto>

    /** DELETE /api/v1/bills/{id}/items/{itemId}
     * ⚠️ NOT in backendapi.md — verify with backend before calling. */
    @DELETE("bills/{id}/items/{itemId}")
    suspend fun removeItemFromBill(
        @Path("id") id: Long,
        @Path("itemId") itemId: Long,
    ): ApiResponse<BillDto>
}

