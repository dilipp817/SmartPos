package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

/**
 * Use Case: GetFoodById
 * Encapsulates business logic for fetching a single food by ID.
 */
class GetFoodByIdUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(id: Int): Result<Food> = repository.getFoodById(id)
}

