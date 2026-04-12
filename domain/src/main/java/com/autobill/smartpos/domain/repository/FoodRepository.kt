package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food

/**
 * Repository interface defining the contract for food data operations.
 * Implementation is in the data module to decouple business logic from data access.
 * Uses Result<T> for error handling and PaginationResult<T> for paginated operations.
 */
interface FoodRepository {
    /**
     * Fetches all foods from the data source (non-paginated for single operations).
     */
    suspend fun getFoods(): Result<List<Food>>

    /**
     * Fetches foods with pagination support for infinite scroll.
     *
     * @param offset Starting position in results
     * @param limit Number of items per page
     * @param category Optional category filter
     * @param sort Optional sort parameter (e.g., "price:asc", "name:desc")
     * @return PaginationResult with food items and pagination metadata
     */
    suspend fun getFoodsPaginated(
        offset: Int = 0,
        limit: Int = 20,
        category: String? = null,
        sort: String? = null,
    ): PaginationResult<Food>

    /**
     * Fetches a single food by ID.
     */
    suspend fun getFoodById(id: Long): Result<Food>

    /**
     * Searches foods by name or other criteria (non-paginated).
     */
    suspend fun searchFoods(query: String): Result<List<Food>>

    /**
     * Searches foods with pagination support.
     *
     * @param query Search query string
     * @param offset Starting position in results
     * @param limit Number of items per page
     * @return PaginationResult with search results and pagination metadata
     */
    suspend fun searchFoodsPaginated(
        query: String,
        offset: Int = 0,
        limit: Int = 20,
    ): PaginationResult<Food>
}

