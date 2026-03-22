package com.autobill.smartpos.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.autobill.smartpos.feature.food.FoodRoute

/**
 * Navigation graph for the entire application.
 * Defines all routes and their corresponding composables.
 * Uses type-safe navigation with sealed classes.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.FoodList.route,
        modifier = modifier,
    ) {
        // Food List Screen
        composable(route = Screen.FoodList.route) {
            FoodRoute()
        }

        // Food Detail Screen - TODO: Create FoodDetailScreen
        composable(route = Screen.FoodDetail.route) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable
            // TODO: FoodDetailRoute(foodId = foodId.toInt())
        }

        // Search Screen - TODO: Create SearchScreen
        composable(route = Screen.Search.route) {
            // TODO: SearchRoute()
        }

        // Billing Screen - TODO: Create BillingScreen
        composable(route = Screen.Billing.route) {
            // TODO: BillingRoute()
        }

        // Settings Screen - TODO: Create SettingsScreen
        composable(route = Screen.Settings.route) {
            // TODO: SettingsRoute()
        }
    }
}

