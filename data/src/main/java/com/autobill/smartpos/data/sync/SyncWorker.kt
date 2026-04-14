package com.autobill.smartpos.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.local.dao.PendingOrderDao
import com.autobill.smartpos.data.local.entity.PendingOrderEntity
import com.autobill.smartpos.data.local.entity.PendingOrderItem
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.OrderApiService
import com.autobill.smartpos.data.remote.dto.CreateOrderRequest
import com.autobill.smartpos.data.remote.dto.OrderItemRequestDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

/**
 * WorkManager worker that drains the offline order queue.
 *
 * Triggered by [OfflineQueueRepositoryImpl.scheduleSyncIfNeeded] — runs only when
 * the CONNECTED network constraint is satisfied.
 *
 * For each PENDING row:
 *  - Marks it SYNCING (crash-safe: reset to PENDING on next start via [PendingOrderDao.resetSyncing])
 *  - Calls POST /restaurants/{id}/orders
 *  - On success  → caches order in Room + deletes queue entry
 *  - On 409      → marks FAILED with "Table occupied" reason (user must re-select)
 *  - On error    → marks PENDING again, returns [Result.retry]
 *
 * Returns [Result.success] when all rows are processed (or there were none).
 * Returns [Result.retry] when at least one row errored with a retryable failure.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val pendingOrderDao: PendingOrderDao,
    private val orderApiService: OrderApiService,
    private val orderDao: OrderDao,
    private val moshi: Moshi,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val UNIQUE_WORK_NAME = "sync_offline_orders"
        const val TAG = "SyncWorker"
    }

    private val itemListType =
        Types.newParameterizedType(List::class.java, PendingOrderItem::class.java)
    private val itemAdapter by lazy { moshi.adapter<List<PendingOrderItem>>(itemListType) }

    override suspend fun doWork(): Result {
        // Reset any rows left in SYNCING state (e.g. from a previous crashed run)
        pendingOrderDao.resetSyncing()

        val pending = pendingOrderDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasRetryableFailure = false

        for (entity in pending) {
            pendingOrderDao.updateStatus(entity.id, PendingOrderEntity.STATUS_SYNCING)
            try {
                val submitted = submitOrder(entity)
                if (submitted) {
                    pendingOrderDao.deleteById(entity.id)
                } else {
                    // 409 — mark failed permanently (user re-selection needed)
                    pendingOrderDao.updateStatus(
                        entity.id,
                        PendingOrderEntity.STATUS_FAILED,
                        "Table is now occupied — please re-select a table",
                    )
                }
            } catch (e: Exception) {
                // Transient error — reset to PENDING so next retry picks it up
                pendingOrderDao.updateStatus(
                    entity.id,
                    PendingOrderEntity.STATUS_PENDING,
                    e.message,
                )
                hasRetryableFailure = true
            }
        }

        return if (hasRetryableFailure) Result.retry() else Result.success()
    }

    /**
     * @return true on success, false on 409 CONFLICT.
     * Throws any other [HttpException] or network exception for the caller to handle.
     */
    private suspend fun submitOrder(entity: PendingOrderEntity): Boolean {
        return try {
            val items = deserializeItems(entity.itemsJson)
            val request = CreateOrderRequest(
                tableId   = entity.tableId,
                items     = items,
                orderType = entity.orderType,
                notes     = entity.notes,
            )
            val response = orderApiService.createOrder(entity.restaurantId, request)
            val dto = checkNotNull(response.data) { response.message ?: "Empty response" }

            // Cache the freshly created order locally
            orderDao.upsertOrder(dto.toEntity())
            orderDao.upsertItems(dto.items.map { it.toEntity(dto.id) })
            true
        } catch (e: HttpException) {
            if (e.code() == 409) false else throw e
        }
    }

    private fun deserializeItems(json: String): List<OrderItemRequestDto> =
        (itemAdapter.fromJson(json) ?: emptyList()).map {
            OrderItemRequestDto(
                foodId          = it.foodId,
                quantity        = it.quantity,
                specialRequests = it.specialRequests,
            )
        }
}

