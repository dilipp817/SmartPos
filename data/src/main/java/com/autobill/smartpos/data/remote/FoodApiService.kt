package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.FoodDto
import com.autobill.smartpos.data.remote.dto.PaginatedResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Food operations.
 * Defines endpoints for retrieving food data from backend.
 *
 * Pagination details:
 * - Default limit: 20 items per page
 * - Offset-based pagination using limit and offset query parameters
 * - Response includes metadata (total, has_more, current_page)
 */
interface FoodApiService {
    /**
     * Fetches all foods with pagination support
     *
     * @param offset Starting position (default: 0)
     * @param limit Items per page (default: 20)
     * @return Paginated response with food items and metadata
     */
    @GET("foods")
    suspend fun getFoods(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): PaginatedResponseDto<FoodDto>

    /**
     * Fetches a single food by ID
     *
     * @param id Food ID
     * @return Single food item
     */
    @GET("foods/{id}")
    suspend fun getFoodById(
        @Path("id") id: Int,
    ): FoodDto

    /**
     * Searches foods by query with pagination support
     *
     * @param query Search query string
     * @param offset Starting position (default: 0)
     * @param limit Items per page (default: 20)
     * @return Paginated response with search results
     */
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("q") query: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): PaginatedResponseDto<FoodDto>
}

