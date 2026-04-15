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
     * @param restaurantId Outlet scope — from login session via GetRestaurantIdUseCase.
     *                     NEVER hardcode. Null only for super_admin queries.
     * @param offset Starting position in results
     * @param limit Number of items per page
     * @param category Optional category filter
     * @param sort Optional sort parameter (e.g., "price:asc", "name:desc")
     * @return PaginationResult with food items and pagination metadata
     */
    suspend fun getFoodsPaginated(
        restaurantId: Long? = null,
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
    suspend fun searchFoods(query: String, restaurantId: Long? = null): Result<List<Food>>

    /** Searches foods with pagination support. */
    suspend fun searchFoodsPaginated(
        query: String,
        restaurantId: Long? = null,
        offset: Int = 0,
        limit: Int = 20,
    ): PaginationResult<Food>

    // ── Admin / menu-management mutations (admin role only) ──────────────────

    /**
     * Creates a new food item for the given restaurant.
     * POST /foods/restaurant/{restaurantId}  — 403 for non-admin callers.
     */
    suspend fun createFood(
        restaurantId: Long,
        name: String,
        price: Double,
        description: String?,
        imageUrl: String?,
        categoryId: Long?,
        isVegetarian: Boolean,
        isSpicy: Boolean,
        preparationTime: Int?,
        allergens: String?,
        calories: Int?,
    ): Result<Food>

    /**
     * Updates an existing food item.
     * PUT /foods/{id}  — 403 for non-admin callers.
     */
    suspend fun updateFood(
        foodId: Long,
        restaurantId: Long,
        name: String,
        price: Double,
        description: String?,
        imageUrl: String?,
        categoryId: Long?,
        isVegetarian: Boolean,
        isSpicy: Boolean,
        isAvailable: Boolean,
        preparationTime: Int?,
        allergens: String?,
        calories: Int?,
    ): Result<Food>

    /**
     * Deletes a food item.
     * DELETE /foods/{id}  — 403 for non-admin callers.
     */
    suspend fun deleteFood(foodId: Long): Result<Unit>
}

