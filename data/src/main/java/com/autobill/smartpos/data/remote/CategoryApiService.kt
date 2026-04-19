package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.CategoryDto
import com.autobill.smartpos.data.remote.dto.CreateCategoryRequest
import com.autobill.smartpos.data.remote.dto.FoodListItemDto
import com.autobill.smartpos.data.remote.dto.PagedDataDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Categories (backendapi.md §8 — added in v1.1, April 12, 2026).
 *
 * Role access:
 *  - GET endpoints  → 🟢 any authenticated role (staff / manager / admin)
 *  - POST / PUT / DELETE → 🔴 admin only — mobile POS app should NEVER call these.
 *    They are admin-console operations; included here only for completeness.
 */
interface CategoryApiService {

    /** GET /api/v1/categories?restaurant_id={restaurantId} 🟢 any
     * Primary endpoint for rendering category filter tabs on the menu screen.
     * Returns all categories — filter client-side if needed. */
    @GET("categories")
    suspend fun getCategoriesByRestaurant(
        @Query("restaurant_id") restaurantId: Long,
    ): ApiResponse<List<CategoryDto>>

    /** GET /api/v1/categories/{id} 🟢 any */
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Long,
    ): ApiResponse<CategoryDto>

    /** POST /api/v1/categories 🔴 admin only — do NOT call from POS app. */
    @POST("categories")
    suspend fun createCategory(
        @Query("restaurant_id") restaurantId: Long,
        @Body request: CreateCategoryRequest,
    ): ApiResponse<CategoryDto>

    /** PUT /api/v1/categories/{id} 🔴 admin only — do NOT call from POS app. */
    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Long,
        @Body request: CreateCategoryRequest,
    ): ApiResponse<CategoryDto>

    /** DELETE /api/v1/categories/{id} 🔴 admin only — do NOT call from POS app. */
    @DELETE("categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Long,
    ): ApiResponse<String>

    /**
     * GET /api/v1/categories/{id}/foods 🟢 any
     * Returns paginated food items for a category (backendapi.md §8).
     *
     * ⚠️ Pagination params differ from GET /foods/restaurant/{id}:
     *    - This endpoint uses `offset` (item index, 0-based) + `limit`
     *    - GET /foods/restaurant/{id} uses `page` (page number) + `limit`
     *
     * Prefer GET /foods/restaurant/{id}?category_id={id} for the menu screen —
     * same response shape and consistent pagination. Use this endpoint only when
     * you need to load by category ID alone without a restaurant context.
     */
    @GET("categories/{id}/foods")
    suspend fun getFoodsByCategory(
        @Path("id") categoryId: Long,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
    ): ApiResponse<PagedDataDto<FoodListItemDto>>
}
