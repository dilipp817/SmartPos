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
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    object Search : Screen("search")
    object Billing : Screen("billing")
    object Settings : Screen("settings")
}

/**
 * Navigation events that can be triggered from ViewModels
 */
sealed class NavigationEvent {
    data class NavigateToScreen(val screen: Screen) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    data class NavigateWithResult(val screen: Screen, val result: Any) : NavigationEvent()
}

