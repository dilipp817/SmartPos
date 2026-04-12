package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case: Observe active session.
 * Used to react to login/logout state across the app.
 */
class ObserveSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<User?> = authRepository.observeSession()
}

/**
 * Use case: Get current restaurantId from session.
 * All screens that need restaurant-scoped API calls use this.
 */
class GetRestaurantIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Long? = authRepository.getRestaurantId()
}

/**
 * Use case: Logout
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.clearSession()
}

/**
 * Use case: Recover session from backend (/auth/me).
 * Called on app start if a token exists but restaurantId is missing from local storage.
 */
class RecoverSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> {
        val result = authRepository.getCurrentUser()
        if (result.isSuccess) {
            result.getOrNull()?.let { authRepository.saveSession(it) }
        }
        return result
    }
}

