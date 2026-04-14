package com.autobill.smartpos.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Contract for observing internet connectivity.
 *
 * Implementation lives in :data (ConnectivityMonitor + ConnectivityRepositoryImpl).
 * Feature modules only see this interface via use cases.
 */
interface ConnectivityRepository {

    /**
     * Hot [Flow<Boolean>] backed by [ConnectivityManager.NetworkCallback].
     * true  = at least one validated internet-capable network is available.
     * false = no usable network (offline).
     */
    fun observeIsOnline(): Flow<Boolean>

    /**
     * Synchronous snapshot of connectivity at call time.
     * Used by [OrderRepositoryImpl] to decide between online/offline path.
     */
    fun isCurrentlyOnline(): Boolean
}

