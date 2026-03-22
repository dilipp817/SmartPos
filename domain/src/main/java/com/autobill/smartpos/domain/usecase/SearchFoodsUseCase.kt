package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case: SearchFoods
 * Encapsulates business logic for searching foods by query.
 */
class SearchFoodsUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(query: String): Result<List<Food>> = repository.searchFoods(query)
}

