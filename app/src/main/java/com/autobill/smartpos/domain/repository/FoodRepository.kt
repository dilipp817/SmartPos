package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.core.common.Resource
import com.autobill.smartpos.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getFoods(restaurantId: Int): Flow<Resource<List<Food>>>
}

