package com.autobill.smartpos.feature.food

import com.autobill.smartpos.domain.model.Food

// UI State: Food Screen
// Represents the complete state of the Food screen UI
// All properties are immutable and copied when updated
data class FoodUiState(
    // List of foods to display
    val foods: List<Food> = emptyList(),

    // Indicates if data is currently being loaded
    val isLoading: Boolean = false,

    // Error message to display to user (null if no error)
    val errorMessage: String? = null,
)

