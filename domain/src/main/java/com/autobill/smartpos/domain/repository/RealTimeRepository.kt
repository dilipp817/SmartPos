package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.RealTimeEvent
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the WebSocket real-time event pipeline.
 *
 * Implementation lives in :data — feature modules only see this interface.
 * [connect] / [disconnect] are idempotent — safe to call multiple times.
 */
interface RealTimeRepository {

    /**
     * Open the WebSocket for [restaurantId].
     * Auto-reconnects with exponential back-off on failure.
     * No-op if a connection is already open for the same restaurant.
     */
    fun connect(restaurantId: Long)

    /**
     * Close the WebSocket gracefully and cancel any pending reconnect.
     * Called on logout.
     */
    fun disconnect()

    /**
     * Hot [Flow] of all inbound [RealTimeEvent]s.
     * Backed by [SharedFlow] — no replay; collectors only see future events.
     */
    fun observeEvents(): Flow<RealTimeEvent>

    /** Hot [Flow] of [ConnectionState] changes. */
    fun observeConnectionState(): Flow<ConnectionState>
}

