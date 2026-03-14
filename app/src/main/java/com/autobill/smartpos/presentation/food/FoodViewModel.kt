package com.autobill.smartpos.presentation.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.core.common.FoodResponse
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoodViewModel(
    private val getFoodsUseCase: GetFoodsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodUiState())
    val uiState: StateFlow<FoodUiState> = _uiState.asStateFlow()

    init {
        loadFoods(restaurantId = 1)
    }

    fun loadFoods(restaurantId: Int) {
        viewModelScope.launch {
            getFoodsUseCase(restaurantId).collectLatest { result ->
                when (result) {
                    FoodResponse.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is FoodResponse.Success -> {
                        _uiState.update {
                            it.copy(
                                foods = result.data,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
                    is FoodResponse.Error -> {
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

    companion object {
        fun provideFactory(getFoodsUseCase: GetFoodsUseCase): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FoodViewModel(getFoodsUseCase) as T
                }
            }
    }
}
