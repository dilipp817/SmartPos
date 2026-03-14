package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.repository.FoodRepository

class GetFoodsUseCase(
    private val repository: FoodRepository,
) {
    operator fun invoke(restaurantId: Int) = repository.getFoods(restaurantId)
}
