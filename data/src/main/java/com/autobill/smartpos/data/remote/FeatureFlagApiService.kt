package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.FeatureFlagResponseDto
import retrofit2.http.GET

/**
 * Retrofit service for fetching feature flags from the backend.
 *
 * Requires a valid Bearer token (sent automatically by AuthInterceptor).
 * Returns per-restaurant flags — the server scopes the response to the
 * restaurant associated with the authenticated user's token.
 *
 * Endpoint to implement on the backend:
 *   GET /api/v1/feature-flags
 *   Response: { "data": { "flags": { "offline_order_queue": false, ... } } }
 */
interface FeatureFlagApiService {

    @GET("feature-flags")
    suspend fun getFeatureFlags(): ApiResponse<FeatureFlagResponseDto>
}

