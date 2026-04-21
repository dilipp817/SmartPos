package com.autobill.smartpos.app.navigation

/**
 * Type-safe navigation routes for the application.
 * Using sealed classes ensures compile-time safety for navigation.
 * Each route represents a distinct screen in the app.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object FoodList : Screen("food_list")
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: Long) = "food_detail/$foodId"
    }
    object TableList : Screen("table_list/{orderType}") {
        fun createRoute(orderType: String) = "table_list/$orderType"
    }
    object CreateOrder : Screen("create_order/{tableId}/{orderType}") {
        /**
         * [tableId] = 0L means no table (TAKEAWAY or TABLE_MANAGEMENT=false).
         * [orderType] is the OrderType.value string (e.g. "DINE_IN", "TAKEAWAY").
         */
        fun createRoute(tableId: Long, orderType: String) = "create_order/$tableId/$orderType"
    }
    object OrderList : Screen("order_list")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    object KitchenDisplay : Screen("kitchen_display")

    /**
     * Drawer-accessible billing overview (Phase 7 will add a full bills-history list here).
     * For now shows a helper message directing the user to generate bills from Order Detail.
     * Has NO nav args — safe to navigate from the drawer without an orderId.
     */
    object Billing : Screen("billing")

    /**
     * Order-specific bill generation screen.
     * Navigate here from Order Detail → "Generate Bill".
     * [tableId] = -1L means no table (TAKEAWAY / TABLE_MANAGEMENT=false) — table will NOT be freed after payment.
     */
    object OrderBilling : Screen("order_billing/{orderId}/{tableId}") {
        /** Pass [tableId] = -1L for TAKEAWAY / no-table orders. */
        fun createRoute(orderId: Long, tableId: Long) = "order_billing/$orderId/$tableId"
    }

    /**
     * Payment screen.
     * [totalAmount] and [remainingAmount] are passed as String because
     * NavType does not support Double — PaymentViewModel parses them back.
     * [tableId] = -1L means no table — [PaymentViewModel] will skip [FreeTableUseCase].
     */
    object Payment : Screen("payment/{billId}/{orderId}/{tableId}/{totalAmount}/{remainingAmount}") {
        fun createRoute(
            billId: Long,
            orderId: Long,
            tableId: Long,
            totalAmount: Double,
            remainingAmount: Double,
        ) = "payment/$billId/$orderId/$tableId/$totalAmount/$remainingAmount"
    }

    object Settings : Screen("settings")

    // ── Phase 8 — Reports & Analytics ────────────────────────────────────────

    /** Daily sales report with date-range picker and top-selling items. */
    object SalesReport : Screen("sales_report")

    /** Filterable order history for a selected date range (DELIVERED / CANCELLED / ALL). */
    object OrderHistory : Screen("order_history")

    // ── Phase 9.3 — Admin Dashboard ───────────────────────────────────────────

    /** Admin overview: live stats + quick actions. Visible to admin / manager / super_admin. */
    object AdminDashboard : Screen("admin_dashboard")

    /** CRUD interface for food menu items (admin only). */
    object MenuManagement : Screen("menu_management")

    /** Restaurant settings editor (tax rate, tips, print). */
    object AdminSettings : Screen("admin_settings")

    /** Current session user profile + role guide (admin / manager). */
    object StaffManagement : Screen("staff_management")

    /** Food availability toggle — inventory tracking (admin / manager). */
    object InventoryManagement : Screen("inventory_management")

    /** Admin-only: Create, edit, delete food categories. */
    object CategoryManagement : Screen("category_management")
}



