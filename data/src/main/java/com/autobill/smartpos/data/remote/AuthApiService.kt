package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.ApiResponse
import com.autobill.smartpos.data.remote.dto.LoginRequestDto
import com.autobill.smartpos.data.remote.dto.LoginResponseDto
import com.autobill.smartpos.data.remote.dto.TokenValidationResponseDto
import com.autobill.smartpos.data.remote.dto.UserInfoResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit API Service for Authentication.
 * Endpoints: login, me, validate (per API_REFERENCE.md §Authentication)
 *
 * Token delivery:
 *  - /auth/login  → no token needed (unauthenticated)
 *  - /auth/me     → token via "Authorization: Bearer" header (auto via AuthInterceptor)
 *  - /auth/validate → token via "Authorization: Bearer" header (auto via AuthInterceptor)
 *
 * ⚠️ Backend confirmed: /auth/validate reads token ONLY from Authorization header.
 * Sending it as ?token= query param returns 401 every time — JWT in URLs is a
 * security exposure (written to server access logs).
 */
interface AuthApiService {

    /**
     * POST /api/v1/auth/login
     * Unauthenticated — no Bearer header needed.
     * Returns token + restaurantId. Mobile MUST store restaurantId for all subsequent calls.
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto,
    ): ApiResponse<LoginResponseDto>

    /**
     * GET /api/v1/auth/me
     * Token delivered via Authorization: Bearer header (AuthInterceptor).
     * Returns current user info including restaurantId.
     * Called on every cold app start to recover/verify session.
     */
    @GET("auth/me")
    suspend fun getCurrentUser(): ApiResponse<UserInfoResponseDto>

    /**
     * POST /api/v1/auth/validate
     * Token delivered via Authorization: Bearer header (AuthInterceptor) — NOT as query param.
     * Returns restaurantId from JWT claims — last fallback for session recovery.
     *
     * Confirmed by backend: sending ?token=<jwt> as query param returns 401 UNAUTHORIZED.
     */
    @POST("auth/validate")
    suspend fun validateToken(): ApiResponse<TokenValidationResponseDto>
}
