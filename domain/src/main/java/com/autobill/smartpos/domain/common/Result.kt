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

/**
 * Typed exception representing an HTTP 409 CONFLICT from the backend.
 * Thrown by repository implementations and caught by ViewModels in feature modules
 * without needing a Retrofit dependency in the presentation layer.
 *
 * Common causes:
 *  - Table already OCCUPIED when creating an order
 *  - Optimistic lock violation on an order/table
 *  - Bill already exists for this order
 */
class HttpConflictException(message: String = "Resource conflict (HTTP 409)") : Exception(message)

/**
 * Typed exception signalling that the order was saved to the local offline queue instead of
 * being sent to the server — because there was no network connectivity at call time.
 *
 * This is a recoverable, non-error condition.  The ViewModel catches it and shows a
 * "queued — will sync when online" confirmation instead of an error banner.
 *
 * [queueId] is the Room row-id of the newly created [PendingOrderEntity].
 * The [SyncWorker] will pick it up once connectivity is restored.
 */
class OfflineQueuedException(val queueId: Long) :
    Exception("No internet — order #$queueId queued for sync")
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

