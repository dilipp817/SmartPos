package com.autobill.smartpos.app.navigation

/**
 * Type-safe navigation routes for the application.
 * Using sealed classes ensures compile-time safety for navigation.
 * Each route represents a distinct screen in the app.
 */
sealed class Screen(val route: String) {
    object FoodList : Screen("food_list")
    object FoodDetail : Screen("food_detail/{foodId}") {
        fun createRoute(foodId: Int) = "food_detail/$foodId"
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

