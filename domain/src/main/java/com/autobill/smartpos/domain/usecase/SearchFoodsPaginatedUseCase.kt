package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use case for searching foods with pagination support.
 * Implements infinite scroll search results by loading pages progressively.
 *
 * [restaurantId] MUST come from [GetRestaurantIdUseCase] — never hardcoded.
 *
 * Usage:
 * ```kotlin
 * val restaurantId = getRestaurantIdUseCase()
 * val result = searchFoodsPaginatedUseCase(query = "pizza", restaurantId = restaurantId)
 * ```
 */
class SearchFoodsPaginatedUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        query: String,
        restaurantId: Long? = null,
        categoryId: Long? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): PaginationResult<Food> = repository.searchFoodsPaginated(
        query = query,
        restaurantId = restaurantId,
        categoryId = categoryId,
        offset = offset,
        limit = limit,
    )
}
