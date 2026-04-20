package com.autobill.smartpos

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.autobill.smartpos.debug.DebugTools
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for SmartPos.
 *
 * Implements [Configuration.Provider] for on-demand WorkManager initialisation
 * with [HiltWorkerFactory] so that [SyncWorker] can receive injected dependencies.
 * The default auto-initialiser is removed in AndroidManifest.xml.
 */
@HiltAndroidApp
class SmartPosApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // debug builds → shows persistent "🚩 Feature Flags" notification (like Chucker)
        // release builds → no-op (DebugTools compiled from app/src/release/)
        DebugTools.init(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
