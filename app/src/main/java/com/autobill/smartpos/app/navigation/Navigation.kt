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
    object TableList : Screen("table_list")
    object CreateOrder : Screen("create_order/{tableId}") {
        fun createRoute(tableId: Long) = "create_order/$tableId"
    }
    object OrderList : Screen("order_list")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    object KitchenDisplay : Screen("kitchen_display")
    object Search : Screen("search")

    /**
     * Drawer-accessible billing overview (Phase 7 will add a full bills-history list here).
     * For now shows a helper message directing the user to generate bills from Order Detail.
     * Has NO nav args — safe to navigate from the drawer without an orderId.
     */
    object Billing : Screen("billing")

    /**
     * Order-specific bill generation screen.
     * Navigate here from Order Detail → "Generate Bill".
     * Requires [orderId] and [tableId] so the table can be freed after payment.
     */
    object OrderBilling : Screen("order_billing/{orderId}/{tableId}") {
        fun createRoute(orderId: Long, tableId: Long) = "order_billing/$orderId/$tableId"
    }

    /**
     * Payment screen.
     * [totalAmount] and [remainingAmount] are passed as String because
     * NavType does not support Double — PaymentViewModel parses them back.
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
}

/**
 * Navigation events that can be triggered from ViewModels
 */
sealed class NavigationEvent {
    data class NavigateToScreen(val screen: Screen) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    data class NavigateWithResult(val screen: Screen, val result: Any) : NavigationEvent()
}

