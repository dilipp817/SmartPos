package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.device.ConnectivityMonitor
import com.autobill.smartpos.domain.repository.ConnectivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges [ConnectivityMonitor] (data) to [ConnectivityRepository] (domain).
 * Keeps the domain layer free of Android framework imports.
 */
@Singleton
class ConnectivityRepositoryImpl @Inject constructor(
    private val monitor: ConnectivityMonitor,
) : ConnectivityRepository {

    override fun observeIsOnline(): Flow<Boolean> = monitor.isOnline

    override fun isCurrentlyOnline(): Boolean = monitor.isCurrentlyOnline()
}

