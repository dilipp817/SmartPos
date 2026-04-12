package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.model.RolePermissions
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.model.canApplyDiscounts
import com.autobill.smartpos.domain.model.canCancelOrders
import com.autobill.smartpos.domain.model.canManageMenu
import com.autobill.smartpos.domain.model.canManageTables
import com.autobill.smartpos.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
 * Called on every cold app start.
 *
 * Logic:
 *  1. No token in DataStore → nothing to recover — return success.
 *     [ObserveSessionUseCase] will emit null → navigation goes to Login automatically.
 *  2. Token exists → call GET /auth/me to validate + recover restaurantId.
 *     - Success → save updated session (restaurantId now guaranteed present).
 *     - Failure (401 / network error) → clear session so navigation goes to Login.
 *       Do NOT leave an expired token in DataStore — it would show Home briefly on next start.
 */
class RecoverSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<User> {
        // Step 1: No token stored — nothing to validate or recover.
        if (!authRepository.isLoggedIn()) {
            return Result.failure(Exception("No session to recover"))
        }

        // Step 2: Token exists — validate it via GET /auth/me.
        val result = authRepository.getCurrentUser()

        return if (result.isSuccess) {
            // Save the refreshed session (ensures restaurantId is always up-to-date).
            result.getOrNull()?.let { authRepository.saveSession(it) }
            result
        } else {
            // Token is expired or invalid — clear it immediately.
            // ObserveSessionUseCase will emit null → navigation redirects to Login.
            authRepository.clearSession()
            result
        }
    }
}

/**
 * Use case: Observe role-based UI permissions.
 *
 * Converts the live session [User] into a [RolePermissions] snapshot so every
 * ViewModel/screen can gate UI controls without repeating role-string comparisons.
 *
 * Emits [RolePermissions.NONE] when no session exists (safe default — no elevated controls).
 * Updates automatically if the session is refreshed (e.g. after session recovery).
 */
class ObserveRolePermissionsUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): Flow<RolePermissions> = authRepository.observeSession()
        .map { user ->
            if (user == null) RolePermissions.NONE
            else RolePermissions(
                canCancelOrders   = user.canCancelOrders(),
                canApplyDiscounts = user.canApplyDiscounts(),
                canManageMenu     = user.canManageMenu(),
                canManageTables   = user.canManageTables(),
            )
        }
}

