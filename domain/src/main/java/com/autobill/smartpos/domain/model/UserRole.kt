package com.autobill.smartpos.domain.model

/**
 * Role constants for the SmartPos app.
 *
 * ⚠️ Backend confirmed (April 11, 2026): roles are stored and returned as LOWERCASE strings.
 * Always compare using these constants — never use "ADMIN", "STAFF", etc.
 *
 * Role hierarchy:
 *   admin   > manager   > staff
 *
 * Super-admin concept:
 *   There is no "super_admin" role string in the database for v1.
 *   A user with restaurantId == null is treated as having cross-restaurant access.
 *   Check restaurantId == null instead of role == "super_admin".
 *
 * Role capabilities:
 *   admin   — Full access within own restaurant (menu, users, orders, reports)
 *   manager — Can cancel orders, apply discounts, view all orders
 *   staff   — Create orders, take payments, view own outlet orders
 */
object UserRole {
    const val ADMIN = "admin"
    const val MANAGER = "manager"
    const val STAFF = "staff"

    /** All valid role strings returned by the backend. */
    val ALL = setOf(ADMIN, MANAGER, STAFF)
}

/** Extension helpers on User for readable role checks. */
fun com.autobill.smartpos.domain.model.User.isAdmin(): Boolean = role == UserRole.ADMIN
fun com.autobill.smartpos.domain.model.User.isManager(): Boolean = role == UserRole.MANAGER
fun com.autobill.smartpos.domain.model.User.isStaff(): Boolean = role == UserRole.STAFF

/**
 * True for users who have access across all restaurants.
 * Detected by restaurantId == null (no role string check — "super_admin" not in DB for v1).
 */
fun com.autobill.smartpos.domain.model.User.isSuperAdmin(): Boolean = restaurantId == null

/** True if user can cancel orders (admin or manager). */
fun com.autobill.smartpos.domain.model.User.canCancelOrders(): Boolean =
    role == UserRole.ADMIN || role == UserRole.MANAGER

/** True if user can apply discounts (admin or manager). */
fun com.autobill.smartpos.domain.model.User.canApplyDiscounts(): Boolean =
    role == UserRole.ADMIN || role == UserRole.MANAGER

/** True if user can manage the menu (admin only). */
fun com.autobill.smartpos.domain.model.User.canManageMenu(): Boolean = role == UserRole.ADMIN

