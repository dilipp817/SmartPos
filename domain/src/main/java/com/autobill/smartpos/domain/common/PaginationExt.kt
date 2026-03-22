package com.autobill.smartpos.domain.common

/**
 * Extension functions for converting between Result and PaginationResult patterns.
 * Helps bridge the gap between single operations and paginated operations.
 */

/**
 * Convert PaginationResult to UiState for UI layer consumption
 *
 * @return UiState with paginated data
 */
fun <T> PaginationResult<T>.toUiState(): UiState<Pagination<T>> = when (this) {
    is PaginationResult.Success -> UiState.Success(pagination)
    is PaginationResult.Failure -> UiState.Error(
        message = exception.message ?: "Unknown error",
        exception = exception,
    )
    is PaginationResult.Loading -> UiState.Loading
}

/**
 * Merge two pagination results (for appending pages)
 */
fun <T> PaginationResult<T>.merge(other: PaginationResult<T>): PaginationResult<T> {
    return when {
        this is PaginationResult.Success && other is PaginationResult.Success -> {
            PaginationResult.Success(
                pagination.copy(
                    data = pagination.data + other.pagination.data,
                    hasMore = other.pagination.hasMore,
                    currentPage = other.pagination.currentPage,
                )
            )
        }
        this is PaginationResult.Success -> this
        other is PaginationResult.Success -> other
        this is PaginationResult.Failure -> this
        else -> this
    }
}





