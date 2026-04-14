package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.model.OrderType
import kotlinx.coroutines.flow.Flow

/**
 * Contract for managing the offline order queue.
 *
 * When a [createOrder] call cannot reach the server (no internet), the payload is
 * persisted here. [scheduleSyncIfNeeded] then schedules a WorkManager job that
 * drains the queue as soon as connectivity is restored.
 */
interface OfflineQueueRepository {

    /**
     * Persist a [createOrder] payload for later submission.
     * @return the local Room row-id (queueId) — passed back as [OfflineQueuedException.queueId].
     */
    suspend fun enqueue(
        restaurantId: Long,
        tableId: Long,
        cartItems: List<OrderLineItem>,
        orderType: OrderType,
        notes: String?,
    ): Long

    /**
     * Hot [Flow<Int>] of the number of PENDING + FAILED entries in the queue.
     * Used by UI to show a "queued orders" badge.
     */
    fun observePendingCount(): Flow<Int>

    /**
     * Schedule (or re-schedule) the [SyncWorker] with a CONNECTED network constraint.
     * Uses [ExistingWorkPolicy.KEEP] — safe to call multiple times.
     */
    fun scheduleSyncIfNeeded()
}

