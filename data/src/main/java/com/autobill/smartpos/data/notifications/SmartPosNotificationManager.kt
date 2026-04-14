package com.autobill.smartpos.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.autobill.smartpos.data.R
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Android notification channels and fires notifications for
 * real-time WebSocket events (Phase 9.1).
 *
 * Channels:
 *  [CHANNEL_NEW_ORDERS]   (HIGH importance) — kitchen: new order arrived
 *  [CHANNEL_ITEM_READY]   (DEFAULT)         — floor: item(s) ready to serve
 *  [CHANNEL_ORDER_STATUS] (DEFAULT)         — order status changes
 *
 * Permission strategy:
 *  - API < 33  : no runtime permission needed — notify() called directly.
 *  - API 33+   : POST_NOTIFICATIONS is requested by MainActivity on first launch.
 *                notify() is called only inside a checkSelfPermission == GRANTED
 *                branch so lint's flow analyser is fully satisfied with no suppressions.
 */
@Singleton
class SmartPosNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_NEW_ORDERS   = "smartpos_new_orders"
        const val CHANNEL_ITEM_READY   = "smartpos_item_ready"
        const val CHANNEL_ORDER_STATUS = "smartpos_order_status"

        private val notifIdCounter = AtomicInteger(2000)
    }

    init { createChannels() }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Kitchen alert: a brand-new order has arrived. */
    fun notifyNewOrder(order: Order) = show(
        channelId = CHANNEL_NEW_ORDERS,
        title     = "🆕 New Order — ${order.orderNumber}",
        body      = "Table ${order.tableNumber} · ${order.items.size} items · " +
                    order.orderType.value.replace("_", " "),
    )

    /** Floor alert: one or more items in this order are READY to be served. */
    fun notifyItemsReady(order: Order) {
        val readyCount = order.items.count { it.itemStatus == ItemStatus.READY }
        if (readyCount == 0) return
        show(
            channelId = CHANNEL_ITEM_READY,
            title     = "✅ Items Ready — Table ${order.tableNumber}",
            body      = "$readyCount item${if (readyCount > 1) "s" else ""} ready · ${order.orderNumber}",
        )
    }

    /** General order status change (used for ORDER_UPDATED events). */
    fun notifyOrderStatusChanged(order: Order) = show(
        channelId = CHANNEL_ORDER_STATUS,
        title     = "${order.orderNumber} — ${order.status.value.replace("_", " ")}",
        body      = "Table ${order.tableNumber}",
    )

    // ── Channel setup ─────────────────────────────────────────────────────────

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannels(
            listOf(
                NotificationChannel(
                    CHANNEL_NEW_ORDERS,
                    "New Orders",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply { description = "Alerts when a new order arrives at the kitchen" },

                NotificationChannel(
                    CHANNEL_ITEM_READY,
                    "Items Ready",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Alerts floor staff when kitchen items are ready to serve" },

                NotificationChannel(
                    CHANNEL_ORDER_STATUS,
                    "Order Status",
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = "Order-level status change notifications" },
            )
        )
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun show(channelId: String, title: String, body: String) {
        val systemManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Layer 1 — fast exit if the user has disabled ALL notifications for the app
        // via Android Settings (this does NOT require POST_NOTIFICATIONS permission to call).
        if (!systemManager.areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (channelId == CHANNEL_NEW_ORDERS)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

        // Layer 2 — POST_NOTIFICATIONS runtime permission (API 33+).
        // POST_NOTIFICATIONS is mandatory in this app — MainActivity requests it on first launch.
        // This guard defends against mid-session revocation (user goes to Settings and revokes).
        // On API < 33 ActivityCompat.checkSelfPermission returns PERMISSION_GRANTED for any
        // permission not declared dangerous at that API level, so no version branching is needed.
        // android.app.NotificationManager.notify() carries @RequiresPermission(conditional=true),
        // so a checkSelfPermission == GRANTED guard fully satisfies lint with no suppression.
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                systemManager.notify(notifIdCounter.getAndIncrement(), notification)
            } catch (_: SecurityException) {
                // Last-resort catch — should not be reached given the explicit guards above.
            }
        }
    }
}
