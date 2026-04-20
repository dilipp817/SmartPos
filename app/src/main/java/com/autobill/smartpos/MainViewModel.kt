package com.autobill.smartpos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.repository.AuthRepository
import com.autobill.smartpos.domain.repository.FeatureFlagRepository
import com.autobill.smartpos.domain.usecase.ConnectRealTimeUseCase
import com.autobill.smartpos.domain.usecase.DisconnectRealTimeUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantUseCase
import com.autobill.smartpos.domain.usecase.LogoutUseCase
import com.autobill.smartpos.domain.usecase.ObserveConnectivityUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import com.autobill.smartpos.domain.usecase.RecoverSessionUseCase
import com.autobill.smartpos.domain.usecase.ScheduleSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-level ViewModel — determines the initial navigation destination.
 *
 * Startup sequence:
 *  1. [sessionState] starts as [SessionResult.Loading] → splash spinner shown.
 *  2. [RecoverSessionUseCase] runs:
 *       - No token       → nothing to do.
 *       - Token valid    → session refreshed from GET /auth/me (restaurantId guaranteed).
 *       - Token expired  → session cleared → [ObserveSessionUseCase] emits null.
 *  3. [_startupComplete] flips to true → [sessionState] resolves:
 *       - User in DataStore  → [SessionResult.Resolved(user)]  → navigate to Home.
 *       - No user in DataStore → [SessionResult.Resolved(null)] → navigate to Login.
 *
 * Keeping [Loading] until recovery is done prevents a brief flash of the Home screen
 * when an expired token is detected and the session is cleared.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val recoverSessionUseCase: RecoverSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getRestaurantUseCase: GetRestaurantUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val connectRealTimeUseCase: ConnectRealTimeUseCase,
    private val disconnectRealTimeUseCase: DisconnectRealTimeUseCase,
    private val observeConnectivityUseCase: ObserveConnectivityUseCase,
    private val scheduleSyncUseCase: ScheduleSyncUseCase,
    private val authRepository: AuthRepository,
    private val featureFlagRepository: FeatureFlagRepository,
    appPrefsDataStore: AppPrefsDataStore,
) : ViewModel() {

    /** Observed by [MainActivity] to set the theme before the first frame. */
    val isDarkTheme: StateFlow<Boolean> = appPrefsDataStore.observeIsDarkTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    /**
     * Flips to true once [RecoverSessionUseCase] completes (success or failure).
     * While false, [sessionState] stays [SessionResult.Loading] regardless of DataStore.
     */
    private val _startupComplete = MutableStateFlow(false)

    /**
     * Combined flow: only resolves after startup recovery is done.
     *
     * - [SessionResult.Loading]       → recovery still running — show splash
     * - [SessionResult.Resolved(user)] → recovery done, user non-null → Home
     * - [SessionResult.Resolved(null)] → recovery done, no session → Login
     */
    val sessionState: StateFlow<SessionResult> = combine(
        observeSessionUseCase(),
        _startupComplete,
    ) { user, startupComplete ->
        if (!startupComplete) SessionResult.Loading
        else SessionResult.Resolved(user)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SessionResult.Loading,
    )

    /**
     * Fires when a proactive token expiry check detects an expired/invalid token.
     * Observed by [MainActivity] to show a "Session expired — please log in" dialog.
     */
    private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpiredEvent: SharedFlow<Unit> = _sessionExpiredEvent.asSharedFlow()

    /**
     * Epoch-ms timestamp of the last successful [refreshFromRemoteApi] call.
     * Used to throttle foreground refreshes — we don't need to hit the server on
     * every onResume (notification tray dismiss, Chucker screen, rotation, etc.).
     */
    private var lastFlagRefreshMs = 0L
    private val FLAG_REFRESH_INTERVAL_MS = 15 * 60 * 1_000L  // 15 minutes

    init {
        viewModelScope.launch {
            // Run recovery first — validate token, refresh restaurantId, or clear expired session.
            recoverSessionUseCase()
            // Only after recovery completes does the UI move past the splash screen.
            _startupComplete.value = true
            // Load restaurant details in the background so settings and name are available
            // immediately when any screen opens. Non-blocking — navigation already resolved above.
            loadRestaurantDetails()
            // Connect WebSocket after session is confirmed
            val restaurantId = getRestaurantIdUseCase()
            if (restaurantId != null) connectRealTimeUseCase(restaurantId)
            // Refresh flags after startup — non-blocking, silent on failure.
            // Stamp the time so the first onResume (which fires right after startup)
            // doesn't immediately make a second identical call.
            refreshFlagsIfDue()
        }

        // Phase 9.2: re-schedule offline queue sync whenever connectivity is restored.
        // Uses KEEP policy so this is safe to call on every transition from offline → online.
        var wasOnline = true
        observeConnectivityUseCase()
            .onEach { isOnline ->
                if (isOnline && !wasOnline) {
                    scheduleSyncUseCase()
                }
                wasOnline = isOnline
            }
            .launchIn(viewModelScope)
    }

    /**
     * Fetch restaurant details from the network and cache locally.
     * Silent on failure — cached value (from a previous session) will be used instead.
     */
    private suspend fun loadRestaurantDetails() {
        val restaurantId = getRestaurantIdUseCase() ?: return  // not logged in
        getRestaurantUseCase(restaurantId)                     // result cached in RestaurantDataStore
    }

    /**
     * Called on every app foreground / onResume.
     * If the stored token expires within the next 5 minutes, validates it proactively:
     *  - 401 → clears the session and fires [sessionExpiredEvent]
     *  - 200 → session still valid, nothing to do
     *
     * See MOBILE_TEAM_RESPONSE.md Point 2 — shipped April 17, 2026.
     */
    fun checkTokenExpiryOnForeground() {
        viewModelScope.launch {
            val token = authRepository.getToken() ?: return@launch  // not logged in — nothing to do
            val expiresAt = authRepository.getExpiresAt()
            if (expiresAt == 0L) return@launch  // no expiry stored (very old session) — skip

            val nowSeconds = System.currentTimeMillis() / 1_000L
            val EXPIRY_BUFFER_SECONDS = 5 * 60L   // 5 minutes

            if (nowSeconds > expiresAt - EXPIRY_BUFFER_SECONDS) {
                // Token is expired or expiring very soon — validate with the server
                val result = authRepository.validateToken(token)
                if (result.isFailure) {
                    // Server returned 401 or network confirms expiry — force logout
                    logoutUseCase()
                    _sessionExpiredEvent.tryEmit(Unit)
                    return@launch  // don't refresh flags if we just logged out
                }
            }

            // Refresh feature flags if enough time has passed since the last fetch.
            // Throttled to FLAG_REFRESH_INTERVAL_MS — avoids a network call on every
            // notification-tray dismiss, Chucker open, screen rotation, etc.
            refreshFlagsIfDue()
        }
    }

    /**
     * Refreshes feature flags from the server only if [FLAG_REFRESH_INTERVAL_MS] has
     * elapsed since the last successful call. Silent on network failure.
     *
     * Call sites:
     *  1. [init] — once on cold start (process creation)
     *  2. [checkTokenExpiryOnForeground] — on app foreground, throttled to ≤ once per 15 min
     */
    private suspend fun refreshFlagsIfDue() {
        val now = System.currentTimeMillis()
        if (now - lastFlagRefreshMs < FLAG_REFRESH_INTERVAL_MS) return
        featureFlagRepository.refreshFromRemoteApi()
        lastFlagRefreshMs = System.currentTimeMillis()
    }

    fun logout() {
        // Disconnect WebSocket before clearing session
        disconnectRealTimeUseCase()
        viewModelScope.launch { logoutUseCase() }
    }
}

/** Tri-state result for app startup navigation. */
sealed interface SessionResult {
    /** Recovery still running — show splash/loading indicator. */
    object Loading : SessionResult

    /** Recovery complete. [user] is null when logged out, non-null when logged in. */
    data class Resolved(val user: User?) : SessionResult
}
