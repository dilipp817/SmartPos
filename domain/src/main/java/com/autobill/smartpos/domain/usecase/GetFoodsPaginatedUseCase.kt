package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use case for fetching foods with pagination support.
 * Implements infinite scroll by loading pages progressively.
 *
 * [restaurantId] MUST come from [GetRestaurantIdUseCase] — never hardcoded.
 * Null only when used by super_admin across all outlets.
 *
 * Usage:
 * ```kotlin
 * val restaurantId = getRestaurantIdUseCase()
 * val result = getFoodsPaginatedUseCase(restaurantId = restaurantId, offset = 0, limit = 20)
 * ```
 */
class GetFoodsPaginatedUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long? = null,
        offset: Int = 0,
        limit: Int = 20,
        category: String? = null,
        sort: String? = null,
    ): PaginationResult<Food> = repository.getFoodsPaginated(
        restaurantId = restaurantId,
        offset = offset,
        limit = limit,
        category = category,
        sort = sort,
    )
}
