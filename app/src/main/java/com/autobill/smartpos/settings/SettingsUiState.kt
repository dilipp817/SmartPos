package com.autobill.smartpos.settings

/**
 * UI state for the Settings screen.
 *
 * [profile] — populated from the active session (non-null once session is loaded).
 * [restaurantName] — from the cached restaurant (empty if not yet fetched).
 * [isDarkTheme] — persisted in [AppPrefsDataStore], toggled by the user.
 * [isLoggingOut] — true while the logout coroutine is running.
 */
data class SettingsUiState(
    // Profile
    val username: String = "",
    val email: String = "",
    val role: String = "",
    // Restaurant info
    val restaurantName: String = "",
    val currency: String = "INR",
    // Appearance
    val isDarkTheme: Boolean = false,
    // Actions
    val isLoggingOut: Boolean = false,
) {
    /** Display-friendly role label. */
    val roleLabel: String
        get() = when (role.lowercase()) {
            "super_admin" -> "Super Admin"
            "admin"       -> "Admin"
            "manager"     -> "Manager"
            "kitchen"     -> "Kitchen"
            else          -> "Staff"
        }

    /** Colour token (as ARGB Long) for the role chip background. */
    val roleChipColor: Long
        get() = when (role.lowercase()) {
            "super_admin", "admin" -> 0xFFFFE0B2L   // amber-100
            "manager"              -> 0xFFE3F2FDL   // blue-50
            "kitchen"              -> 0xFFE8F5E9L   // green-50
            else                   -> 0xFFF5F5F5L   // grey-100
        }

    val roleChipTextColor: Long
        get() = when (role.lowercase()) {
            "super_admin", "admin" -> 0xFFE65100L   // deep orange
            "manager"              -> 0xFF1565C0L   // blue-800
            "kitchen"              -> 0xFF2E7D32L   // green-800
            else                   -> 0xFF616161L   // grey-700
        }
}

