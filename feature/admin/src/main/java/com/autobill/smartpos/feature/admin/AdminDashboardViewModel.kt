package com.autobill.smartpos.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.model.isSuperAdmin
import com.autobill.smartpos.domain.usecase.GetAdminStatsUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getAdminStatsUseCase: GetAdminStatsUseCase,
    private val getRestaurantUseCase: GetRestaurantUseCase,
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        // Keep restaurant info live from cache
        observeRestaurantUseCase()
            .onEach { r -> _uiState.update { it.copy(restaurant = r) } }
            .launchIn(viewModelScope)

        // Derive role display from session
        observeSessionUseCase()
            .onEach { user ->
                if (user != null) {
                    _uiState.update { it.copy(
                        username    = user.username,
                        role        = user.role,
                        isSuperAdmin = user.isSuperAdmin(),
                    ) }
                }
            }
            .launchIn(viewModelScope)

        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update { it.copy(
                    isLoading    = false,
                    isSuperAdmin = true,
                ) }
                return@launch
            }
            // Refresh restaurant details
            getRestaurantUseCase(restaurantId)
            // Load aggregated stats
            val stats = getAdminStatsUseCase(restaurantId)
            _uiState.update { it.copy(isLoading = false, stats = stats) }
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}

