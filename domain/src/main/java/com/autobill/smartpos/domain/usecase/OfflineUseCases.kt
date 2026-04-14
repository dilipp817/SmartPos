package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.repository.ConnectivityRepository
import com.autobill.smartpos.domain.repository.OfflineQueueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ── Phase 9.2 — Offline Mode Use Cases ───────────────────────────────────────

/**
 * Observe real-time internet connectivity.
 * true  = online, false = offline.
 * Backed by [ConnectivityManager.NetworkCallback] — no polling.
 */
class ObserveConnectivityUseCase @Inject constructor(
    private val repository: ConnectivityRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeIsOnline()
}

/**
 * Synchronous connectivity snapshot.
 * Used internally by [OrderRepositoryImpl] to choose online vs. offline path.
 */
class IsOnlineUseCase @Inject constructor(
    private val repository: ConnectivityRepository,
) {
    operator fun invoke(): Boolean = repository.isCurrentlyOnline()
}

/**
 * Observe the number of orders queued for offline sync (PENDING + FAILED).
 * Shown as a badge on the order queue indicator in the UI.
 */
class ObservePendingQueueCountUseCase @Inject constructor(
    private val repository: OfflineQueueRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observePendingCount()
}

/**
 * Schedule (or keep) the offline sync WorkManager job.
 * The job runs as soon as a CONNECTED network constraint is satisfied.
 * Safe to call multiple times — uses KEEP deduplication policy.
 *
 * Called from [MainViewModel] on every connectivity restore as a safety net
 * (covers orders queued while the app was in the background or force-stopped).
 */
class ScheduleSyncUseCase @Inject constructor(
    private val repository: OfflineQueueRepository,
) {
    operator fun invoke() = repository.scheduleSyncIfNeeded()
}

