package com.autobill.smartpos.data.remote

import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.RealTimeEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebSocket is deferred to v2 (contract M-09 / M-19).
 *
 * This is a no-op stub kept so that [RealTimeRepositoryImpl] compiles without changes.
 * [connectionState] always emits [ConnectionState.DISCONNECTED], which causes all
 * polling fallbacks (KDS 15 s, Orders 15 s, Tables 30 s) to start immediately on
 * screen resume — this is the correct v1 real-time strategy.
 *
 * [events] never emits — screens rely entirely on polling.
 *
 * DELETE this file when v2 WebSocket backend is ready and replace with the real impl.
 */
@Singleton
class SmartPosWebSocketManager @Inject constructor() {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _events = MutableSharedFlow<RealTimeEvent>(extraBufferCapacity = 0)
    val events: SharedFlow<RealTimeEvent> = _events.asSharedFlow()

    /** No-op — WebSocket not available in v1. */
    fun connect(restaurantId: Long) = Unit

    /** No-op — nothing to disconnect. */
    fun disconnect() = Unit
}
