package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.FoodResponse
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel: Food Feature
// Manages UI state for the Food screen
// Handles data loading and state updates
// Uses Hilt for dependency injection
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodsUseCase: GetFoodsUseCase,
) : ViewModel() {


    // Mutable state - internal use only
    private val _uiState = MutableStateFlow(FoodUiState())

    // Public immutable state - exposed to UI
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    // Load foods when ViewModel is created
    init {
        loadFoods(restaurantId = 1)
    }

    // Load foods from the use case and update UI state
    fun loadFoods(restaurantId: Int) {
        viewModelScope.launch {
            // Observe use case result flow
            getFoodsUseCase(restaurantId).collectLatest { result ->
                // Handle different response states
                when (result) {
                    FoodResponse.Loading -> {
                        // Update state: Show loading indicator
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is FoodResponse.Success -> {
                        // Update state: Show loaded foods
                        _uiState.update {
                            it.copy(
                                foods = result.data,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
                    is FoodResponse.Error -> {
                        // Update state: Show error message
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                            )
                        }
                    }
                }
            }
        }
    }
}


