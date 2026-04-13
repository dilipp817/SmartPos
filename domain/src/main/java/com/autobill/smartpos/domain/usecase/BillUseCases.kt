package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillStatus
import com.autobill.smartpos.domain.repository.BillRepository
import javax.inject.Inject

/**
 * Use case: Generate a bill for a completed order.
 *
 * ⚠️ Never calculate tax on the app — the backend computes all amounts.
 * [discount] is an optional rupee amount (not percentage). Default: 0.0.
 *
 * Returns 409 CONFLICT (wrapped as [Result.Failure]) if a bill already exists for the order.
 */
class GenerateBillUseCase @Inject constructor(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        orderId: Long,
        discount: Double = 0.0,
    ): Result<Bill> = repository.generateBill(restaurantId, orderId, discount)
}

/** Use case: Fetch a single bill by its numeric ID (full detail with line items). */
class GetBillByIdUseCase @Inject constructor(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(id: Long): Result<Bill> = repository.getBillById(id)
}

/** Use case: Fetch a single bill by its human-readable bill number (e.g. BILL-1-20260413-001). */
class GetBillByNumberUseCase @Inject constructor(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(billNumber: String): Result<Bill> =
        repository.getBillByNumber(billNumber)
}

/**
 * Use case: Fetch a list of bills, optionally filtered by status.
 *
 * ⚠️ Always pass a status — omitting it returns an empty list (backend contract §5).
 */
class GetAllBillsUseCase @Inject constructor(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(status: BillStatus? = BillStatus.ISSUED): Result<List<Bill>> =
        repository.getAllBills(status)
}

/**
 * Use case: Cancel a bill.
 * Only allowed when the bill status is ISSUED.
 */
class CancelBillUseCase @Inject constructor(
    private val repository: BillRepository,
) {
    suspend operator fun invoke(id: Long): Result<Bill> = repository.cancelBill(id)
}

