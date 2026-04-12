package com.autobill.smartpos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.usecase.LogoutUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import com.autobill.smartpos.domain.usecase.RecoverSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * App-level ViewModel — determines the initial navigation destination.
 *
 * [sessionState]:
 *   - null  → still loading (show splash)
 *   - User  → valid session exists → navigate to main screen
 *   - (emitted as null after clearSession) → logged out → navigate to login
 *
 * On startup, if a token is stored but the restaurantId is missing, [RecoverSessionUseCase]
 * calls GET /auth/me and re-populates the session from the backend.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeSessionUseCase: ObserveSessionUseCase,
    private val recoverSessionUseCase: RecoverSessionUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    /**
     * Tri-state:
     *  - Loading phase  → null (internal sentinel — not exposed directly)
     *  - Session exists → User
     *  - No session     → null User (after DataStore emits null)
     *
     * We use a wrapper so the UI can distinguish "still loading" from "logged out".
     */
    val sessionState: StateFlow<SessionResult> = observeSessionUseCase()
        .map { user -> SessionResult.Resolved(user) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = SessionResult.Loading,
        )

    init {
        // On first launch, attempt to recover session from backend if restaurantId is missing.
        viewModelScope.launch {
            recoverSessionUseCase()
        }
    }

    fun logout() {
        viewModelScope.launch { logoutUseCase() }
    }
}

/** Tri-state result for app startup navigation. */
sealed interface SessionResult {
    /** DataStore not yet emitted — show splash/loading. */
    object Loading : SessionResult

    /** DataStore emitted. [user] is null when logged out, non-null when logged in. */
    data class Resolved(val user: User?) : SessionResult
}

