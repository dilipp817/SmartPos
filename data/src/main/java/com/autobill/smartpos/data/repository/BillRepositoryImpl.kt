package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.remote.BillApiService
import com.autobill.smartpos.data.remote.OrderApiService
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillStatus
import com.autobill.smartpos.domain.repository.BillRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BillRepository implementation.
 *
 * [generateBill] delegates to [OrderApiService.generateBill] (preferred path — auto-computes GST).
 * All other operations use [BillApiService].
 *
 * ⚠️ NEVER compute tax locally — always call [generateBill] and display what the server returns.
 */
@Singleton
class BillRepositoryImpl @Inject constructor(
    private val orderApiService: OrderApiService,
    private val billApiService: BillApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BillRepository {

    /**
     * POST /restaurants/{restaurantId}/orders/{orderId}/generate-bill?discount=...
     *
     * 409 CONFLICT → bill already exists for this order → mapped to [HttpConflictException].
     */
    override suspend fun generateBill(
        restaurantId: Long,
        orderId: Long,
        discount: Double,
    ): Result<Bill> = withContext(ioDispatcher) {
        try {
            val response = orderApiService.generateBill(restaurantId, orderId, discount)
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to generate bill"
            }
            Result.Success(dto.toDomain())
        } catch (e: HttpException) {
            if (e.code() == 409) {
                Result.Failure(HttpConflictException("A bill already exists for this order."))
            } else {
                Result.Failure(e)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getBillById(id: Long): Result<Bill> = withContext(ioDispatcher) {
        try {
            val response = billApiService.getBillById(id)
            val dto = checkNotNull(response.data) { response.message ?: "Bill not found" }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    override suspend fun getBillByNumber(billNumber: String): Result<Bill> =
        withContext(ioDispatcher) {
            try {
                val response = billApiService.getBillByNumber(billNumber)
                val dto = checkNotNull(response.data) { response.message ?: "Bill not found" }
                Result.Success(dto.toDomain())
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    override suspend fun getAllBills(status: BillStatus?): Result<List<Bill>> =
        withContext(ioDispatcher) {
            try {
                val response = billApiService.getAllBills(status?.value)
                val dtoList = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch bills"
                }
                Result.Success(dtoList.map { it.toDomain() })
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    override suspend fun cancelBill(id: Long): Result<Bill> = withContext(ioDispatcher) {
        try {
            val response = billApiService.cancelBill(id)
            val dto = checkNotNull(response.data) { response.message ?: "Failed to cancel bill" }
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}

