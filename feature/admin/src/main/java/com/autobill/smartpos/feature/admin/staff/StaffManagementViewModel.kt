package com.autobill.smartpos.feature.admin.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StaffManagementViewModel @Inject constructor(
    private val observeSessionUseCase: ObserveSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffManagementUiState())
    val uiState: StateFlow<StaffManagementUiState> = _uiState.asStateFlow()

    init {
        observeSessionUseCase()
            .onEach { user ->
                _uiState.update { it.copy(currentUser = user, isLoading = false) }
            }
            .launchIn(viewModelScope)

        _uiState.update { it.copy(isLoading = true) }
    }
}

