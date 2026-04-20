package com.autobill.smartpos.debug

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.autobill.smartpos.data.R

/**
 * DEBUG build — shows a persistent "🚩 Feature Flags" notification alongside Chucker.
 * Tapping the notification opens FeatureFlagsActivity where each flag can be toggled.
 *
 * Called from SmartPosApp.onCreate. In release builds the no-op version of this
 * file (app/src/release/.../DebugTools.kt) is compiled instead — zero overhead in prod.
 */
object DebugTools {

    private const val CHANNEL_ID  = "smartpos_debug_flags"
    private const val NOTIF_ID    = 9_001   // fixed ID so only one notification is shown

    fun init(app: Application) {
        createChannel(app)
        showNotification(app)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Feature Flags (Debug)",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Persistent shortcut to the debug feature-flag panel"
            setShowBadge(false)
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun showNotification(context: Context) {
        val intent = Intent(context, FeatureFlagsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚩 Feature Flags")
            .setContentText("Tap to toggle feature flags for this debug build")
            .setContentIntent(pendingIntent)
            .setOngoing(true)           // persistent — won't be dismissed by swipe
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not yet granted (e.g. first launch before MainActivity
            // has requested it). Notification will not appear — not critical for debug.
        }
    }
}

