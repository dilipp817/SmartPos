package com.autobill.smartpos.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autobill.smartpos.auth.LoginScreen
import com.autobill.smartpos.feature.food.FoodRoute

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
                    // Navigate to main app and remove Login from the back-stack
                    // so the user cannot go back to the login screen.
                    navController.navigate(Screen.FoodList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Main App ────────────────────────────────────────────────────────

        composable(route = Screen.FoodList.route) {
            // TODO: Replace with HomeRoute once all wiring is complete and tested.
            // HomeRoute is the ODRfast-styled full home screen.
            FoodRoute(modifier = Modifier.fillMaxSize())
        }

        // Food Detail Screen
        composable(route = Screen.FoodDetail.route) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable
            // TODO: FoodDetailRoute(foodId = foodId.toLong(), onBack = { navController.popBackStack() })
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
