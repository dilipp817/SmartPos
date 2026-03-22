package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case: GetFoods
 * Encapsulates the business logic for fetching foods.
 * Implements Kotlin's invoke operator for cleaner call syntax.
 * Production-ready with proper error handling using Result<T>.
 */
class GetFoodsUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    /**
     * Operator overload allows calling the use case like a function.
     * @return Result<List<Food>> containing foods or error
     */
    suspend operator fun invoke(): Result<List<Food>> = repository.getFoods()
}
