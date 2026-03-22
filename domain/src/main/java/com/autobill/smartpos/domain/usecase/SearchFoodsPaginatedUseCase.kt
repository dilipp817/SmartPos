package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use case for searching foods with pagination support.
 * Implements infinite scroll search results by loading pages progressively.
 *
 * Usage:
 * ```kotlin
 * val result = searchFoodsPaginatedUseCase(query = "pizza", offset = 0, limit = 20)
 * when (result) {
 *     is PaginationResult.Success -> handleSearchResults(result.pagination)
 *     is PaginationResult.Failure -> handleError(result.exception)
 *     PaginationResult.Loading -> showLoading()
 * }
 * ```
 */
class SearchFoodsPaginatedUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        query: String,
        offset: Int = 0,
        limit: Int = 20,
    ): PaginationResult<Food> = repository.searchFoodsPaginated(
        query = query,
        offset = offset,
        limit = limit,
    )
}

