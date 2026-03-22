package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use case for fetching foods with pagination support.
 * Implements infinite scroll by loading pages progressively.
 *
 * Usage:
 * ```kotlin
 * val result = getFoodsPaginatedUseCase(offset = 0, limit = 20)
 * when (result) {
 *     is PaginationResult.Success -> handleData(result.pagination)
 *     is PaginationResult.Failure -> handleError(result.exception)
 *     PaginationResult.Loading -> showLoading()
 * }
 * ```
 */
class GetFoodsPaginatedUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        offset: Int = 0,
        limit: Int = 20,
    ): PaginationResult<Food> = repository.getFoodsPaginated(offset = offset, limit = limit)
}

