package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.FoodListItemDto
import com.autobill.smartpos.data.remote.dto.FoodResponseDto
import com.autobill.smartpos.data.remote.dto.CreateFoodRequest
import com.autobill.smartpos.data.remote.dto.PagedDataDto
import com.autobill.smartpos.data.remote.dto.UpdateFoodRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Food operations (backendapi.md §7).
 *
 * Documented endpoints for mobile:
 *  - GET /foods/restaurant/{restaurantId}  → getFoodsByRestaurant()  Primary menu load
 *  - GET /foods/search                     → searchFoods()           Search / filter
 *  - GET /foods/{id}                       → getFoodById()           Food detail screen
 *  - POST /foods/restaurant/{restaurantId} → createFood()            🔴 admin only
 *
 * Always pass restaurantId from SessionDataStore — NEVER hardcode it.
 */
interface FoodApiService {

    /**
     * Home screen food list — primary recommended endpoint (backendapi.md §7).
     * GET /api/v1/foods/restaurant/{restaurantId}
     *
     * Response shape: ApiResponse<PagedDataDto<FoodListItemDto>>
     * backendapi.md v1.1 (April 12, 2026): response is PAGINATED — data is a wrapper object,
     * not a plain array. Unwrap with: response.data?.data.orEmpty()
     * Pagination metadata: response.data?.pagination
     */
    @GET("foods/restaurant/{restaurantId}")
    suspend fun getFoodsByRestaurant(
        @Path("restaurantId") restaurantId: Long,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20,
        @Query("category_id") categoryId: Long? = null,  // filter by category (backendapi.md §7)
    ): ApiResponse<PagedDataDto<FoodListItemDto>>


    /**
     * Search foods.
     * GET /api/v1/foods/search
     */
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("q") query: String? = null,
        @Query("restaurant_id") restaurantId: Long? = null,
        @Query("category_id") categoryId: Long? = null,
        @Query("is_vegetarian") isVegetarian: Boolean? = null,
        @Query("is_spicy") isSpicy: Boolean? = null,
        @Query("is_available") isAvailable: Boolean? = null,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<PagedDataDto<FoodListItemDto>>

    /**
     * Food detail.
     * GET /api/v1/foods/{id}
     */
    @GET("foods/{id}")
    suspend fun getFoodById(
        @Path("id") id: Long,
    ): ApiResponse<FoodResponseDto>

    /**
     * Create food.
     * POST /api/v1/foods/restaurant/{restaurantId}
     */
    @POST("foods/restaurant/{restaurantId}")
    suspend fun createFood(
        @Path("restaurantId") restaurantId: Long,
        @Body request: CreateFoodRequest,
    ): ApiResponse<FoodResponseDto>

    /**
     * Update food.
     * PUT /api/v1/foods/{id}  — admin / manager only (server returns 403 otherwise)
     */
    @PUT("foods/{id}")
    suspend fun updateFood(
        @Path("id") foodId: Long,
        @Body request: UpdateFoodRequest,
    ): ApiResponse<FoodResponseDto>

    /**
     * Delete food.
     * DELETE /api/v1/foods/{id}  — admin / manager only (server returns 403 otherwise)
     */
    @DELETE("foods/{id}")
    suspend fun deleteFood(
        @Path("id") foodId: Long,
    ): ApiResponse<Unit?>
}
