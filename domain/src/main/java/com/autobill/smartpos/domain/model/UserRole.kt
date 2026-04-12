package com.autobill.smartpos.domain.model

object UserRole {
    const val ADMIN = "admin"
    const val MANAGER = "manager"
    const val STAFF = "staff"

    /** All valid role strings returned by the backend. */
    val ALL = setOf(ADMIN, MANAGER, STAFF)
}

/** Extension helpers on User for readable role checks. */
fun User.isAdmin(): Boolean = role == UserRole.ADMIN
fun User.isManager(): Boolean = role == UserRole.MANAGER
fun User.isStaff(): Boolean = role == UserRole.STAFF

/**
 * True for users who have access across all restaurants.
 * Detected by restaurantId == null (no role string check — "super_admin" not in DB for v1).
 */
fun User.isSuperAdmin(): Boolean = restaurantId == null

/** True if user can cancel orders (admin or manager). */
fun User.canCancelOrders(): Boolean =
    role == UserRole.ADMIN || role == UserRole.MANAGER

/** True if user can apply discounts (admin or manager). */
fun User.canApplyDiscounts(): Boolean =
    role == UserRole.ADMIN || role == UserRole.MANAGER

/** True if user can manage the menu (admin or super_admin with null restaurantId). */
fun User.canManageMenu(): Boolean =
    role == UserRole.ADMIN || restaurantId == null

