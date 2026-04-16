package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.RestaurantDataStore
import com.autobill.smartpos.data.local.SessionDataStore
import com.autobill.smartpos.data.remote.AuthApiService
import com.autobill.smartpos.data.remote.dto.LoginRequestDto
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository implementation.
 *
 * Key responsibilities:
 *  - Call auth API (login / me / validate)
 *  - Persist / clear session via SessionDataStore
 *  - Expose restaurantId to all callers — it is NEVER hardcoded
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sessionDataStore: SessionDataStore,
    private val restaurantDataStore: RestaurantDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override suspend fun login(
        username: String,
        password: String,
        deviceId: String?,
        deviceType: String?,
    ): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val envelope = authApiService.login(
                LoginRequestDto(
                    username = username,
                    password = password,
                    deviceId = deviceId,
                    deviceType = deviceType,
                )
            )
            val data = checkNotNull(envelope.data) {
                envelope.error?.message ?: envelope.message ?: "Login failed"
            }
            User(
                id = data.id,
                username = data.username,
                email = data.email,
                role = data.role,
                restaurantId = data.restaurantId,  // ← from response, NEVER hardcoded
                token = data.token,
                expiresIn = data.expiresIn,
                deviceId = data.deviceId,
                deviceType = data.deviceType,
            )
        }
    }

    override suspend fun getCurrentUser(): Result<User> = withContext(ioDispatcher) {
        runCatching {
            val envelope = authApiService.getCurrentUser()
            val data = checkNotNull(envelope.data) {
                envelope.error?.message ?: envelope.message ?: "Failed to get current user"
            }
            val storedToken = sessionDataStore.getToken()
                ?: error("No token in local storage — cannot reconstruct session")
            // /auth/me does not return expires_in — preserve the value stored at login time.
            val storedExpiresIn = sessionDataStore.getExpiresIn()
            User(
                id = data.id,
                username = data.username,
                email = data.email,
                role = data.role,
                restaurantId = data.restaurantId,
                token = storedToken,
                expiresIn = storedExpiresIn,
                deviceId = data.deviceId,
                deviceType = data.deviceType,
            )
        }
    }

    override suspend fun validateToken(token: String): Result<User> = withContext(ioDispatcher) {
        runCatching {
            // Token is sent via Authorization: Bearer header by AuthInterceptor automatically.
            // Backend confirmed: ?token= query param returns 401 — token must be in the header.
            val envelope = authApiService.validateToken()
            val data = checkNotNull(envelope.data) {
                envelope.error?.message ?: envelope.message ?: "Token validation failed"
            }
            check(data.valid) { "Token is invalid or expired" }
            // /auth/validate does not return expires_in — preserve the value stored at login time.
            val storedExpiresIn = sessionDataStore.getExpiresIn()
            User(
                id = data.userId,
                username = data.username,
                email = "",
                role = data.role,
                restaurantId = data.restaurantId,  // ← recovered from JWT claims
                token = token,
                expiresIn = storedExpiresIn,
            )
        }
    }

    override fun observeSession(): Flow<User?> = sessionDataStore.observeUser()

    override suspend fun saveSession(user: User) = sessionDataStore.saveUser(user)

    override suspend fun clearSession() {
        sessionDataStore.clearUser()
        restaurantDataStore.clearRestaurant()   // remove stale restaurant data on logout
    }

    override suspend fun isLoggedIn(): Boolean = sessionDataStore.getToken() != null

    /** Always read from storage — never hardcode. */
    override suspend fun getRestaurantId(): Long? = sessionDataStore.getRestaurantId()

    override suspend fun getToken(): String? = sessionDataStore.getToken()
}

