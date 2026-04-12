package com.autobill.smartpos.domain.model

data class RolePermissions(
    /** Can cancel an in-progress order (manager / admin / super_admin). */
    val canCancelOrders: Boolean = false,
    /** Can apply a rupee discount before billing (manager / admin / super_admin). */
    val canApplyDiscounts: Boolean = false,
    /** Can create / edit / delete menu items (admin / super_admin only). */
    val canManageMenu: Boolean = false,
    /** Can create / edit / delete tables (manager / admin / super_admin). */
    val canManageTables: Boolean = false,
) {
    companion object {
        /**
         * Safe default — no elevated permissions.
         * Used as the initial StateFlow value before the session loads,
         * so the UI never accidentally shows privileged controls during startup.
         */
        val NONE = RolePermissions()
    }
}

