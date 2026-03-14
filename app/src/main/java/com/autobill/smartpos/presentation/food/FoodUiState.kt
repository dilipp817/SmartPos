package com.autobill.smartpos.presentation.food

import com.autobill.smartpos.domain.model.Food

data class FoodUiState(
    val foods: List<Food> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
