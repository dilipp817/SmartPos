package com.autobill.smartpos.domain.model

/**
 * Domain model: Logged-in user session.
 * restaurantId comes from login response — never hardcoded.
 * null restaurantId means super_admin (can access all outlets).
 */
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val restaurantId: Long?,   // ← key field for multi-outlet support
    val token: String,
    /** Absolute Unix epoch seconds when the token expires (from login `expires_at`). */
    val expiresAt: Long = 0L,
    val deviceId: String? = null,
    val deviceType: String? = null,
)

