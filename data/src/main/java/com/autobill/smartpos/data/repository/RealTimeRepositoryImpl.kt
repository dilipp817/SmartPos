package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.local.dao.TableDao
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.notifications.SmartPosNotificationManager
import com.autobill.smartpos.data.remote.SmartPosWebSocketManager
import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.autobill.smartpos.domain.repository.RealTimeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [RealTimeRepository] implementation.
 *
 * Delegates connection management to [SmartPosWebSocketManager].
 * Side-effects on every event:
 *  - Persist the updated order / table to Room so offline reads stay fresh.
 *  - Fire push notifications via [SmartPosNotificationManager].
 *
 * The side-effect collector runs in [scope] (IO + SupervisorJob) which lives
 * for the lifetime of the singleton — no memory leak.
 */
@Singleton
class RealTimeRepositoryImpl @Inject constructor(
    private val webSocketManager: SmartPosWebSocketManager,
    private val orderDao: OrderDao,
    private val tableDao: TableDao,
    private val notificationManager: SmartPosNotificationManager,
) : RealTimeRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        scope.launch {
            webSocketManager.events.collect { event ->
                when (event) {
                    is RealTimeEvent.OrderCreated -> {
                        cacheOrder(event.order)
                        notificationManager.notifyNewOrder(event.order)
                    }
                    is RealTimeEvent.OrderUpdated -> {
                        cacheOrder(event.order)
                        notificationManager.notifyOrderStatusChanged(event.order)
                    }
                    is RealTimeEvent.OrderItemUpdated -> {
                        cacheOrder(event.order)
                        val hasReadyItems = event.order.items.any { it.itemStatus == ItemStatus.READY }
                        if (hasReadyItems) notificationManager.notifyItemsReady(event.order)
                    }
                    is RealTimeEvent.TableUpdated -> {
                        tableDao.upsertTable(event.table.toEntity())
                    }
                    else -> Unit  // ConnectionChanged — no cache or notification needed
                }
            }
        }
    }

    override fun connect(restaurantId: Long)          = webSocketManager.connect(restaurantId)
    override fun disconnect()                          = webSocketManager.disconnect()
    override fun observeEvents(): Flow<RealTimeEvent>  = webSocketManager.events
    override fun observeConnectionState(): Flow<ConnectionState> = webSocketManager.connectionState

    // ── Cache helpers ─────────────────────────────────────────────────────────

    private fun cacheOrder(order: Order) {
        scope.launch {
            orderDao.upsertOrder(order.toEntity())
            orderDao.upsertItems(order.items.map { it.toEntity(order.id) })
        }
    }
}

