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
import com.autobill.smartpos.feature.order.CreateOrderRoute
import com.autobill.smartpos.feature.order.OrderDetailScreen
import com.autobill.smartpos.feature.order.OrderRoute
import com.autobill.smartpos.feature.table.TableRoute

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
                    // Cart → Select Table → Create Order (Phase 5)
                    navController.navigate(Screen.TableList.route)
                },
                onLogout = onLogout,
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

        // Table List / Selection Screen — user selects an available table before creating an order
        composable(route = Screen.TableList.route) {
            TableRoute(
                onTableSelected = { tableId ->
                    navController.navigate(Screen.CreateOrder.createRoute(tableId))
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Create Order — receives tableId from TableList
        composable(
            route = Screen.CreateOrder.route,
            arguments = listOf(navArgument("tableId") { type = NavType.LongType }),
        ) {
            CreateOrderRoute(
                onOrderCreated = { orderId ->
                    // Navigate to Order List after placing an order; clear back-stack up to FoodList
                    navController.navigate(Screen.OrderList.route) {
                        popUpTo(Screen.FoodList.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() },
                onReselectTable = {
                    // Pop back to TableList so user can pick a different table
                    navController.popBackStack(Screen.TableList.route, inclusive = false)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Order List Screen — entry point for staff to monitor all orders
        composable(route = Screen.OrderList.route) {
            OrderRoute(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Order Detail — Phase 5.3 (stub with back navigation wired)
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
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
