package com.autobill.smartpos.feature.food

import com.autobill.smartpos.domain.model.Food

/**
 * UI State: Food Screen
 * Legacy state class - kept for reference
 * New implementation uses UiState<List<Food>> from domain layer
 * This demonstrates the transition to production-ready patterns
 */
data class FoodUiState(
    // List of foods to display
    val foods: List<Food> = emptyList(),

    // Indicates if data is currently being loaded
    val isLoading: Boolean = false,

    // Error message to display to user (null if no error)
    val errorMessage: String? = null,
)

