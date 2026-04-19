package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.BuildConfig
import com.autobill.smartpos.data.local.SessionDataStore
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.remote.dto.WsEventType
import com.autobill.smartpos.data.remote.dto.WsOrderEventDto
import com.autobill.smartpos.data.remote.dto.WsPingMessageDto
import com.autobill.smartpos.data.remote.dto.WsTableEventDto
import com.autobill.smartpos.data.remote.dto.WsTypeProbeDto
import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton WebSocket client for SmartPos real-time events.
 *
 * ⚠️ TODO (HIGH PRIORITY — MUST DISCUSS WITH BACKEND BEFORE ENABLING):
 *  The backend confirmed on April 17, 2026 that there is currently NO WebSocket server
 *  (@EnableWebSocket / STOMP / SockJS is not present). This class is fully implemented
 *  on the mobile side but the [connect] method is temporarily disabled as a no-op.
 *
 *  Real-time order/table push is a critical product feature (KDS screen depends on it).
 *  Three options need joint design discussion with the backend team:
 *   1. Polling        — GET /orders/active every 10–15 s (zero backend work, available now)
 *   2. Server-Sent Events (SSE) — medium effort, backend adds an SSE endpoint
 *   3. STOMP over WebSocket     — high effort, full WebSocket server needed
 *
 *  Action items:
 *   - [ ] Mobile + Backend: schedule design meeting to agree on approach
 *   - [ ] Backend: implement chosen real-time transport
 *   - [ ] Mobile: re-enable [connect] (remove the guard below) once endpoint is confirmed
 *
 *  See MOBILE_TEAM_RESPONSE.md Point 3.
 *
 * Responsibilities (once enabled):
 *  - Connect to `wss://{host}/api/v1/ws/restaurants/{id}` with Bearer auth
 *  - Parse inbound JSON messages and emit [RealTimeEvent]s on [events]
 *  - Send PING every 30 s to keep the connection alive
 *  - Reconnect with exponential back-off (1 s → 2 s → 4 s → … → 30 s max)
 *    on any unintentional close / failure
 *  - Stop reconnecting on intentional [disconnect]
 *
 * Uses the existing [OkHttpClient] (which already has auth interceptor + debug SSL trust).
 * Uses the existing [Moshi] instance (which has KotlinJsonAdapterFactory).
 */
@Singleton
class SmartPosWebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi,
    private val sessionDataStore: SessionDataStore,
) {
    // ── Public flows ──────────────────────────────────────────────────────────

    private val _events = MutableSharedFlow<RealTimeEvent>(
        replay            = 0,
        extraBufferCapacity = 64,
        onBufferOverflow  = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<RealTimeEvent> = _events.asSharedFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // ── Internal state ────────────────────────────────────────────────────────

    /** Dedicated background scope — not tied to any ViewModel lifecycle. */
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null

    private var currentRestaurantId: Long? = null

    /** true after [disconnect] — prevents reconnect loop on intentional close. */
    @Volatile private var isIntentionalDisconnect = false
    private var reconnectAttempt = 0

    companion object {
        private const val TAG = "SmartPosWS"
        // Delays in ms: 1 s, 2 s, 4 s, 8 s, 16 s, then cap at 30 s
        private val RECONNECT_DELAYS = listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L, 30_000L)
        private const val PING_INTERVAL_MS = 30_000L

        /**
         * TODO (HIGH PRIORITY): Flip to `true` once the backend WebSocket endpoint is live.
         *
         * The full connect / reconnect / ping implementation below is complete and ready.
         * It is disabled ONLY because the backend has no WebSocket server yet (April 17, 2026).
         * Do NOT enable this flag without first confirming the backend endpoint URL with the
         * backend team and completing the joint design discussion.
         *
         * See class-level KDoc for the three options under discussion.
         */
        private const val WS_ENABLED = false
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * TODO (HIGH PRIORITY): Remove the [WS_ENABLED] guard and the log line once the backend
     * WebSocket endpoint is confirmed and live. The implementation below is complete.
     */
    fun connect(restaurantId: Long) {
        if (!WS_ENABLED) {
            Log.w(TAG, "connect($restaurantId) skipped — WebSocket disabled pending backend implementation. See WS_ENABLED flag.")
            return
        }

        if (_connectionState.value == ConnectionState.CONNECTED &&
            currentRestaurantId == restaurantId) return   // already connected

        currentRestaurantId      = restaurantId
        isIntentionalDisconnect  = false
        reconnectAttempt         = 0

        // Close any existing socket before opening a new one
        webSocket?.close(1000, "Reconnecting for restaurant $restaurantId")
        webSocket = null

        openWebSocket(restaurantId)
    }

    fun disconnect() {
        isIntentionalDisconnect = true
        reconnectJob?.cancel()
        pingJob?.cancel()
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun openWebSocket(restaurantId: Long) {
        val token = sessionDataStore.getToken() ?: return   // no token → nothing to do

        // Derive wss:// URL from the REST base URL
        val wsUrl = BuildConfig.BASE_URL
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/') + "/api/v1/ws/restaurants/$restaurantId"

        _connectionState.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = okHttpClient.newWebSocket(request, createListener(restaurantId))
    }

    private fun createListener(restaurantId: Long) = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            reconnectAttempt = 0
            _connectionState.value = ConnectionState.CONNECTED
            emit(RealTimeEvent.ConnectionChanged(ConnectionState.CONNECTED))
            startPingLoop(webSocket)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            parseAndEmit(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            pingJob?.cancel()
            _connectionState.value = ConnectionState.DISCONNECTED
            emit(RealTimeEvent.ConnectionChanged(ConnectionState.DISCONNECTED))
            if (!isIntentionalDisconnect) scheduleReconnect(restaurantId)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            pingJob?.cancel()
            _connectionState.value = ConnectionState.DISCONNECTED
            emit(RealTimeEvent.ConnectionChanged(ConnectionState.DISCONNECTED))
            // 1000 = normal close; anything else → reconnect
            if (!isIntentionalDisconnect && code != 1000) scheduleReconnect(restaurantId)
        }
    }

    // ── Message parsing ───────────────────────────────────────────────────────

    private fun parseAndEmit(raw: String) {
        try {
            val probe = moshi.adapter(WsTypeProbeDto::class.java).fromJson(raw) ?: return

            val event: RealTimeEvent? = when (probe.type) {

                WsEventType.ORDER_CREATED,
                WsEventType.ORDER_UPDATED,
                WsEventType.ORDER_ITEM_UPDATED -> {
                    val msg   = moshi.adapter(WsOrderEventDto::class.java).fromJson(raw)
                    val order = msg?.data?.toDomain() ?: return
                    when (probe.type) {
                        WsEventType.ORDER_CREATED      -> RealTimeEvent.OrderCreated(order)
                        WsEventType.ORDER_ITEM_UPDATED -> RealTimeEvent.OrderItemUpdated(order)
                        else                            -> RealTimeEvent.OrderUpdated(order)
                    }
                }

                WsEventType.TABLE_UPDATED -> {
                    val msg   = moshi.adapter(WsTableEventDto::class.java).fromJson(raw)
                    val table = msg?.data?.toDomain() ?: return
                    RealTimeEvent.TableUpdated(table)
                }

                WsEventType.CONNECTED -> RealTimeEvent.ConnectionChanged(ConnectionState.CONNECTED)
                WsEventType.PONG      -> null   // heartbeat ack — no domain event needed
                else                  -> null   // unknown type — ignore gracefully
            }

            event?.let { emit(it) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse WebSocket message: $e")
        }
    }

    // ── Reconnection ──────────────────────────────────────────────────────────

    private fun scheduleReconnect(restaurantId: Long) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = RECONNECT_DELAYS.getOrElse(reconnectAttempt) { RECONNECT_DELAYS.last() }
            reconnectAttempt++
            _connectionState.value = ConnectionState.RECONNECTING
            emit(RealTimeEvent.ConnectionChanged(ConnectionState.RECONNECTING))
            delay(delayMs)
            if (!isIntentionalDisconnect) openWebSocket(restaurantId)
        }
    }

    // ── PING ──────────────────────────────────────────────────────────────────

    private fun startPingLoop(ws: WebSocket) {
        pingJob?.cancel()
        pingJob = scope.launch {
            val pingJson = moshi.adapter(WsPingMessageDto::class.java).toJson(WsPingMessageDto())
            while (true) {
                delay(PING_INTERVAL_MS)
                if (!ws.send(pingJson)) break  // socket closed — loop ends
            }
        }
    }

    // ── Emit helper ───────────────────────────────────────────────────────────

    private fun emit(event: RealTimeEvent) {
        scope.launch { _events.emit(event) }
    }
}

