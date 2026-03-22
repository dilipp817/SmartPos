package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.repository.FoodRepository
import javax.inject.Inject

// Use Case: GetFoods
// Encapsulates the business logic for fetching foods from a restaurant
// Implements Kotlin's invoke operator for cleaner call syntax: useCase(restaurantId)
class GetFoodsUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    // Operator overload allows calling the use case like a function
    operator fun invoke(restaurantId: Int) = repository.getFoods(restaurantId)
}
