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






