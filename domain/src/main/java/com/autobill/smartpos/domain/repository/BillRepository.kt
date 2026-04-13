package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillStatus

/**
 * Repository contract for bill data operations.
 *
 * ⚠️ NEVER calculate tax on the app — always call [generateBill] and display
 * what the server returns (18% GST split as 9% CGST + 9% SGST).
 *
 * One bill per order — a second call returns 409 CONFLICT if a bill already exists.
 */
interface BillRepository {

    /**
     * POST /restaurants/{restaurantId}/orders/{orderId}/generate-bill?discount=0
     *
     * Backend auto-computes: subtotal, cgstAmount (9%), sgstAmount (9%),
     * taxAmount (18%), totalAmount.
     *
     * [discount] — optional rupee discount applied before tax (default 0).
     *
     * Returns 409 CONFLICT if a bill already exists for this order.
     */
    suspend fun generateBill(
        restaurantId: Long,
        orderId: Long,
        discount: Double = 0.0,
    ): Result<Bill>

    /** GET /bills/{id} — full bill with line items */
    suspend fun getBillById(id: Long): Result<Bill>

    /** GET /bills/number/{billNumber} — fetch by human-readable bill number */
    suspend fun getBillByNumber(billNumber: String): Result<Bill>

    /**
     * GET /bills?status=ISSUED
     * ⚠️ Always pass a status — omitting it returns an empty list (backend contract).
     */
    suspend fun getAllBills(status: BillStatus?): Result<List<Bill>>

    /**
     * PATCH /bills/{id}/cancel
     * Only allowed when bill status is ISSUED.
     */
    suspend fun cancelBill(id: Long): Result<Bill>
}

