package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case: SearchFoods
 * Encapsulates business logic for searching foods by query.
 *
 * [restaurantId] scopes results to the outlet (contract M-11). Pass the value from
 * [GetRestaurantIdUseCase]. The repository will return a failure if this is null
 * (no active session), but callers should resolve it before calling this use case.
 */
class SearchFoodsUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(query: String, restaurantId: Long?): Result<List<Food>> =
        repository.searchFoods(query, restaurantId)
}
