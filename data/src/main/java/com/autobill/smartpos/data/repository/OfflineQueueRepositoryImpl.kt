package com.autobill.smartpos.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.autobill.smartpos.data.local.dao.PendingOrderDao
import com.autobill.smartpos.data.local.entity.PendingOrderEntity
import com.autobill.smartpos.data.local.entity.PendingOrderItem
import com.autobill.smartpos.data.sync.SyncWorker
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OfflineQueueRepository
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists offline [createOrder] payloads to Room and schedules [SyncWorker]
 * to drain the queue once connectivity is restored.
 */
@Singleton
class OfflineQueueRepositoryImpl @Inject constructor(
    private val dao: PendingOrderDao,
    private val workManager: WorkManager,
    private val moshi: Moshi,
) : OfflineQueueRepository {

    private val itemListType = Types.newParameterizedType(List::class.java, PendingOrderItem::class.java)
    private val itemAdapter by lazy { moshi.adapter<List<PendingOrderItem>>(itemListType) }

    override suspend fun enqueue(
        restaurantId: Long,
        tableId: Long,
        cartItems: List<OrderLineItem>,
        orderType: OrderType,
        notes: String?,
    ): Long {
        val items = cartItems.map {
            PendingOrderItem(
                foodId          = it.foodId,
                quantity        = it.quantity,
                specialRequests = it.specialRequests,
            )
        }
        val entity = PendingOrderEntity(
            restaurantId = restaurantId,
            tableId      = tableId,
            orderType    = orderType.value,
            notes        = notes,
            itemsJson    = itemAdapter.toJson(items),
        )
        return dao.insert(entity)
    }

    override fun observePendingCount(): Flow<Int> = dao.observeCount()

    override fun scheduleSyncIfNeeded() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag(SyncWorker.TAG)
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,   // don't duplicate if one is already pending/running
            request,
        )
    }
}

