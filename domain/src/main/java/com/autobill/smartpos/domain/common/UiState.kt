package com.autobill.smartpos.domain.common

/**
 * Sealed class representing UI state for async operations.
 * Provides a clean way to handle Loading, Success, and Error states in the UI.
 * This is the recommended pattern for composable state management.
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Exception? = null) : UiState<Nothing>()

    // Utility functions
    inline fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, exception)
        Loading -> Loading
        Idle -> Idle
    }

    inline fun onSuccess(action: (T) -> Unit): UiState<T> = apply {
        if (this is Success) action(data)
    }

    inline fun onError(action: (String, Exception?) -> Unit): UiState<T> = apply {
        if (this is Error) action(message, exception)
    }

    fun getOrNull(): T? = if (this is Success) data else null
}

// Extension functions
fun <T> UiState<T>.isLoading(): Boolean = this is UiState.Loading
fun <T> UiState<T>.isSuccess(): Boolean = this is UiState.Success
fun <T> UiState<T>.isError(): Boolean = this is UiState.Error
fun <T> UiState<T>.isIdle(): Boolean = this is UiState.Idle

/**
 * Helper function to convert Result to UiState
 */
fun <T> Result<T>.toUiState(): UiState<T> = when (this) {
    is Result.Success -> UiState.Success(data)
    is Result.Failure -> UiState.Error(
        message = exception.message ?: "An error occurred",
        exception = exception
    )
    Result.Loading -> UiState.Loading
}

