package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.common.toUiState
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel: Food Feature
 * Manages UI state for the Food screen using production-ready patterns.
 * Uses Result<T> for data operations and UiState<T> for UI state.
 * Injected with Hilt for dependency management.
 */
@HiltViewModel
class FoodViewModel @Inject constructor(
    private val getFoodsUseCase: GetFoodsUseCase,
) : ViewModel() {

    // Mutable internal state
    private val _foodsState = MutableStateFlow<UiState<List<Food>>>(UiState.Idle)
    
    // Public immutable state - exposed to UI
    val foodsState: StateFlow<UiState<List<Food>>> = _foodsState.asStateFlow()

    // Load foods when ViewModel is created
    init {
        loadFoods()
    }

    /**
     * Loads foods from the use case and updates UI state.
     * Handles success, error, and loading states.
     */
    fun loadFoods() {
        viewModelScope.launch {
            // Set loading state
            _foodsState.value = UiState.Loading

            // Execute use case with Result pattern
            val result = getFoodsUseCase()

            // Update state based on Result
            _foodsState.update {
                when (result) {
                    is Result.Success -> UiState.Success(result.data)
                    is Result.Failure -> UiState.Error(
                        message = result.exception.message ?: "Failed to load foods",
                        exception = result.exception
                    )
                    Result.Loading -> UiState.Loading
                }
            }
        }
    }

    /**
     * Retries loading foods
     */
    fun retryLoadFoods() {
        loadFoods()
    }
}


