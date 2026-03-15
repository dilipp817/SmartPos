package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.FoodResponse
import com.autobill.smartpos.domain.model.Food
import kotlinx.coroutines.flow.Flow

// Repository Contract/Interface
// Defines the contract for data access operations related to Food
// Implementations can fetch from API, database, cache, etc.
interface FoodRepository {
    // Fetches foods for a specific restaurant
    // Returns a Flow that emits FoodResponse states (Loading, Success, Error)
    fun getFoods(restaurantId: Int): Flow<FoodResponse<List<Food>>>
}

