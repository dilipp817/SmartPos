package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.RestaurantDto
import com.autobill.smartpos.data.remote.dto.UpdateRestaurantRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface RestaurantApiService {

    /**
     * GET /api/v1/restaurants/{id}
     * Returns full restaurant details including settings block.
     * Called on app startup (after session recovery) to populate the local cache.
     */
    @GET("restaurants/{id}")
    suspend fun getRestaurant(
        @Path("id") id: Long,
    ): ApiResponse<RestaurantDto>

    /**
     * PATCH /api/v1/restaurants/{id}
     * Partial update — only fields present in [request] are changed.
     *
     * ⚠️ Returns 403 Forbidden for staff / kitchen roles.
     *    Only manager / admin / super_admin may call this endpoint.
     */
    @PATCH("restaurants/{id}")
    suspend fun updateRestaurant(
        @Path("id") id: Long,
        @Body request: UpdateRestaurantRequest,
    ): ApiResponse<RestaurantDto>
}

