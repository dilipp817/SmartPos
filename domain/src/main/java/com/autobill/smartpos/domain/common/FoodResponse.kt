package com.autobill.smartpos.domain.common

// FoodResponse: Sealed interface for handling async operations
// Represents three possible states: Loading, Success, or Error
// Used throughout the app to handle API calls and async operations
sealed interface FoodResponse<out T> {
    // State: Loading - Operation in progress
    data object Loading : FoodResponse<Nothing>

    // State: Success - Operation completed successfully
    data class Success<T>(val data: T) : FoodResponse<T>

    // State: Error - Operation failed
    data class Error(val message: String, val throwable: Throwable? = null) : FoodResponse<Nothing>
}

