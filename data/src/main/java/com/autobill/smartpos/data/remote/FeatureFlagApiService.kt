package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.FeatureFlagResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service for fetching feature flags from the backend.
 *
 * Requires a valid Bearer token (sent automatically by AuthInterceptor).
 * Returns per-restaurant flags scoped to the given restaurantId.
 *
 * Endpoint: GET /api/v1/restaurants/{restaurantId}/feature-flags
 * Contract: FINAL_ORDER_TYPE_CONTRACT.md — Section C3 (April 21, 2026)
 */
interface FeatureFlagApiService {

    @GET("restaurants/{restaurantId}/feature-flags")
    suspend fun getFeatureFlags(
        @Path("restaurantId") restaurantId: Long,
    ): ApiResponse<FeatureFlagResponseDto>
}

