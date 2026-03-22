package com.autobill.smartpos.domain.common

/**
 * Sealed class to represent the result of an operation.
 * Either Success with data or Failure with error details.
 * Production-ready error handling following Railway-Oriented Programming.
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    // Utility functions
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> Failure(exception)
        Loading -> Loading
    }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Failure -> Failure(exception)
        Loading -> Loading
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> = apply {
        if (this is Success) action(data)
    }

    inline fun onFailure(action: (Exception) -> Unit): Result<T> = apply {
        if (this is Failure) action(exception)
    }

    fun getOrNull(): T? = if (this is Success) data else null
    fun exceptionOrNull(): Exception? = if (this is Failure) exception else null
}

// Extension functions for common operations
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isFailure(): Boolean = this is Result.Failure
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

