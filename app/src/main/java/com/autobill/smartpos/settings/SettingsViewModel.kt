package com.autobill.smartpos.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.usecase.LogoutUseCase
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Combines three live data sources:
 *  1. [ObserveSessionUseCase]    — username / email / role
 *  2. [ObserveRestaurantUseCase] — restaurant name / currency
 *  3. [AppPrefsDataStore]        — isDarkTheme preference
 *
 * All three are observed as hot flows so the UI always reflects the latest state.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val observeSessionUseCase: ObserveSessionUseCase,
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val appPrefsDataStore: AppPrefsDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe session (user profile)
        observeSessionUseCase()
            .onEach { user ->
                _uiState.update {
                    it.copy(
                        username = user?.username ?: "",
                        email    = user?.email    ?: "",
                        role     = user?.role     ?: "",
                    )
                }
            }
            .launchIn(viewModelScope)

        // Observe restaurant (name, currency)
        observeRestaurantUseCase()
            .onEach { restaurant ->
                _uiState.update {
                    it.copy(
                        restaurantName = restaurant?.name     ?: "",
                        currency       = restaurant?.currency ?: "INR",
                    )
                }
            }
            .launchIn(viewModelScope)

        // Observe theme preference
        appPrefsDataStore.observeIsDarkTheme()
            .onEach { dark -> _uiState.update { it.copy(isDarkTheme = dark) } }
            .launchIn(viewModelScope)
    }

    // ── User actions ──────────────────────────────────────────────────────────

    fun toggleDarkTheme() {
        val newValue = !_uiState.value.isDarkTheme
        viewModelScope.launch {
            appPrefsDataStore.setDarkTheme(newValue)
            // uiState updates automatically via the observed flow above
        }
    }

    fun logout() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            logoutUseCase()
            // After clearSession(), ObserveSessionUseCase emits null →
            // MainActivity's sessionState resolves to Resolved(null) →
            // NavHost navigates to Login automatically.
        }
    }
}

