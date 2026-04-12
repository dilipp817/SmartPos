package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Use case: Login
 * Calls auth API, saves session (token + restaurantId), returns User.
 * restaurantId must NEVER be hardcoded — it comes from this response.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        username: String,
        password: String,
        deviceId: String? = null,
        deviceType: String? = null,
    ): Result<User> {
        require(username.isNotBlank()) { "Username must not be blank" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        val result = authRepository.login(username, password, deviceId, deviceType)
        if (result.isSuccess) {
            result.getOrNull()?.let { authRepository.saveSession(it) }
        }
        return result
    }
}

