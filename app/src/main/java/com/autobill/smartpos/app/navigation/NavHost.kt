package com.autobill.smartpos.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autobill.smartpos.auth.LoginScreen
import com.autobill.smartpos.feature.food.FoodDetailRoute
import com.autobill.smartpos.feature.food.HomeRoute

/**
 * Application navigation graph.
 *
 * [startDestination] is set by [MainActivity] based on the resolved session state:
 *  - Session exists  → [Screen.FoodList]
 *  - No session      → [Screen.Login]
 *
 * [MainActivity] uses `key(isLoggedIn)` to recreate this NavHost when the auth
 * state flips, which fully resets the back-stack and prevents navigating past
 * the Login screen after logout.
 *
 * [onLogout] is called by any screen that has a logout action; it clears the
 * session in [MainViewModel], which causes [MainActivity] to recreate [AppNavHost]
 * with [Screen.Login] as the new start destination.
 */
@Composable
fun AppNavHost(
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
    ) {

        // ── Auth ────────────────────────────────────────────────────────────

        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.FoodList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Main App ────────────────────────────────────────────────────────

        composable(route = Screen.FoodList.route) {
            HomeRoute(
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onCheckoutClick = {
                    navController.navigate(Screen.Billing.route)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Food Detail Screen — foodId passed as Long nav argument
        composable(
            route = Screen.FoodDetail.route,
            arguments = listOf(navArgument("foodId") { type = NavType.LongType }),
        ) {
            FoodDetailRoute(
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Search Screen
        composable(route = Screen.Search.route) {
            // TODO: SearchRoute()
        }

        // Billing / Checkout Screen
        composable(route = Screen.Billing.route) {
            // TODO: BillingRoute(onBack = { navController.popBackStack() }, onLogout = onLogout)
        }

        // Settings Screen
        composable(route = Screen.Settings.route) {
            // TODO: SettingsRoute(onLogout = onLogout)
        }
    }
}
