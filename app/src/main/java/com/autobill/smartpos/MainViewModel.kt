package com.autobill.smartpos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantUseCase
import com.autobill.smartpos.domain.usecase.LogoutUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import com.autobill.smartpos.domain.usecase.RecoverSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
) : ViewModel() {

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

    init {
        viewModelScope.launch {
            // Run recovery first — validate token, refresh restaurantId, or clear expired session.
            recoverSessionUseCase()
            // Only after recovery completes does the UI move past the splash screen.
            _startupComplete.value = true
            // Load restaurant details in the background so settings and name are available
            // immediately when any screen opens. Non-blocking — navigation already resolved above.
            loadRestaurantDetails()
        }
    }

    /**
     * Fetch restaurant details from the network and cache locally.
     * Silent on failure — cached value (from a previous session) will be used instead.
     */
    private suspend fun loadRestaurantDetails() {
        val restaurantId = getRestaurantIdUseCase() ?: return  // not logged in
        getRestaurantUseCase(restaurantId)                     // result cached in RestaurantDataStore
    }

    fun logout() {
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

