package com.autobill.smartpos.domain.common

/**
 * Represents paginated data with metadata.
 * Used to manage pagination state across repository and viewmodel layers.
 *
 * @param data List of items for current page
 * @param currentPage Current page number (0-indexed)
 * @param limit Items per page
 * @param total Total items available
 * @param hasMore Whether more pages available
 */
data class Pagination<T>(
    val data: List<T> = emptyList(),
    val currentPage: Int = 0,
    val limit: Int = 20,
    val total: Int = 0,
    val hasMore: Boolean = false,
) {
    val offset: Int get() = currentPage * limit
    val isFirstPage: Boolean get() = currentPage == 0
    val canLoadMore: Boolean get() = hasMore && data.isNotEmpty()

    fun nextPage(): Pagination<T> = copy(currentPage = currentPage + 1)
    fun reset(): Pagination<T> = copy(currentPage = 0, data = emptyList())
}

/**
 * Result wrapper for paginated data operations.
 * Combines Result pattern with pagination metadata.
 */
sealed class PaginationResult<T> {
    data class Success<T>(val pagination: Pagination<T>) : PaginationResult<T>()
    data class Failure<T>(val exception: Exception) : PaginationResult<T>()
    object Loading : PaginationResult<Nothing>()
}


