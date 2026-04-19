package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Contract for authentication operations.
 * Implemented in the data layer — domain has no knowledge of Retrofit or DataStore.
 */
interface AuthRepository {
    /** Login with credentials. Returns authenticated User with restaurantId. */
    suspend fun login(
        username: String,
        password: String,
        deviceId: String? = null,
        deviceType: String? = null,
    ): Result<User>

    /**
     * Fetch current user from /auth/me using stored token.
     * Used to recover restaurantId if local session is cleared.
     */
    suspend fun getCurrentUser(): Result<User>

    /** Validate the stored JWT. Returns the user info embedded in the token. */
    suspend fun validateToken(token: String): Result<User>

    /** Observe the active session (null = logged out). */
    fun observeSession(): Flow<User?>

    /** Save the user session locally (token + restaurantId). */
    suspend fun saveSession(user: User)

    /** Clear the session (logout). */
    suspend fun clearSession()

    /** Whether a valid session token exists. */
    suspend fun isLoggedIn(): Boolean

    /** Returns the stored restaurantId, or null if not logged in. */
    suspend fun getRestaurantId(): Long?

    /** Returns the stored JWT token, or null if not logged in. */
    suspend fun getToken(): String?

    /**
     * Returns the stored token expiry as an absolute Unix epoch second.
     * Returns 0 if not stored or not logged in.
     */
    suspend fun getExpiresAt(): Long
}

