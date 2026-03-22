package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food

/**
 * Repository interface defining the contract for food data operations.
 * Implementation is in the data module to decouple business logic from data access.
 * Uses Result<T> for production-ready error handling.
 */
interface FoodRepository {
    /**
     * Fetches all foods from the data source.
     */
    suspend fun getFoods(): Result<List<Food>>

    /**
     * Fetches a single food by ID.
     */
    suspend fun getFoodById(id: Int): Result<Food>

    /**
     * Searches foods by name or other criteria.
     */
    suspend fun searchFoods(query: String): Result<List<Food>>
}

