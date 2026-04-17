package com.autobill.smartpos.domain.model

/**
 * Role string constants.
 *
 * ⚠️ Backend confirmed (pending — see backend Q&A below): role is returned as lowercase.
 * All comparisons use [User.normalizedRole] (lowercased) for safety, so the app works
 * correctly whether backend returns "admin", "ADMIN", or "Admin".
 */
object UserRole {
    const val ADMIN   = "admin"
    const val MANAGER = "manager"
    const val STAFF   = "staff"

    /** All valid role strings returned by the backend. */
    val ALL = setOf(ADMIN, MANAGER, STAFF)
}

/** Lowercase-normalised role — safe for comparison regardless of backend casing. */
val User.normalizedRole: String get() = role.lowercase()

/** Extension helpers on User for readable role checks. */
fun User.isAdmin(): Boolean    = normalizedRole == UserRole.ADMIN
fun User.isManager(): Boolean  = normalizedRole == UserRole.MANAGER
fun User.isStaff(): Boolean    = normalizedRole == UserRole.STAFF

/**
 * True for users who have access across all restaurants.
 * Detected by restaurantId == null (no role string check — "super_admin" not in DB for v1).
 */
fun User.isSuperAdmin(): Boolean = restaurantId == null

/** True if user can cancel orders (admin or manager). */
fun User.canCancelOrders(): Boolean = isAdmin() || isManager()

/** True if user can apply discounts (admin or manager). */
fun User.canApplyDiscounts(): Boolean = isAdmin() || isManager()

/** True if user can manage the menu (admin or super_admin with null restaurantId). */
fun User.canManageMenu(): Boolean = isAdmin() || restaurantId == null

/** True if user can create / edit / delete tables (manager or admin or super_admin). */
fun User.canManageTables(): Boolean = isManager() || isAdmin() || restaurantId == null

