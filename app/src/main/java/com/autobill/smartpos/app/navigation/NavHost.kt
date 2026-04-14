package com.autobill.smartpos.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autobill.smartpos.auth.LoginScreen
import com.autobill.smartpos.feature.billing.BillingRoute
import com.autobill.smartpos.feature.billing.PaymentRoute
import com.autobill.smartpos.feature.food.FoodDetailRoute
import com.autobill.smartpos.feature.food.HomeRoute
import com.autobill.smartpos.feature.order.CreateOrderRoute
import com.autobill.smartpos.feature.order.KitchenDisplayRoute
import com.autobill.smartpos.feature.order.OrderDetailRoute
import com.autobill.smartpos.feature.order.OrderRoute
import com.autobill.smartpos.feature.reports.OrderHistoryRoute
import com.autobill.smartpos.feature.reports.SalesReportRoute
import com.autobill.smartpos.feature.table.TableRoute
import com.autobill.smartpos.settings.SettingsRoute

@Composable
fun AppNavHost(
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // currentBackStackEntryAsState() is null for one frame before the first
    // destination is pushed. Fall back to startDestination so the drawer
    // does not flicker in/out on initial composition.
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: startDestination
    val isAuthenticated = currentRoute != Screen.Login.route

    if (isAuthenticated) {
        PermanentNavigationDrawer(
            drawerContent = {
                // 240 dp is the Material 3 standard drawer width.
                // On a 10" landscape tablet (~1280 dp wide) this leaves
                // ~1040 dp for content — ideal for a POS layout.
                PermanentDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    AppDrawerContent(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                // Pop back to the authenticated root so
                                // drawer taps never build a deep back-stack.
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        },
                        onLogout = onLogout,
                    )
                }
            },
            modifier = modifier.fillMaxSize(),
        ) {
            AppNavGraph(
                navController = navController,
                startDestination = startDestination,
                onLogout = onLogout,
            )
        }
    } else {
        // Login screen — full width, no drawer chrome
        AppNavGraph(
            navController = navController,
            startDestination = startDestination,
            onLogout = onLogout,
            modifier = modifier.fillMaxSize(),
        )
    }
}

// ── Private nav graph ─────────────────────────────────────────────────────────

@Composable
private fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.fillMaxSize(),
    ) {

        // ── Auth ────────────────────────────────────────────────────────────

        composable(route = Screen.Login.route) { _ ->
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.FoodList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Main App ────────────────────────────────────────────────────────

        composable(route = Screen.FoodList.route) { _ ->
            HomeRoute(
                onFoodClick = { foodId ->
                    navController.navigate(Screen.FoodDetail.createRoute(foodId))
                },
                onCheckoutClick = {
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
        ) { _ ->
            FoodDetailRoute(
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Table List / Selection Screen — user selects an available table before creating an order
        composable(route = Screen.TableList.route) { _ ->
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
        ) { _ ->
            CreateOrderRoute(
                onOrderCreated = { _ ->
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
                onOrderQueued = {
                    // Order saved offline — go back to TableList (Phase 9.2)
                    navController.popBackStack(Screen.TableList.route, inclusive = false)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Order List Screen — entry point for staff to monitor all orders
        composable(route = Screen.OrderList.route) { _ ->
            OrderRoute(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onKdsClick = {
                    navController.navigate(Screen.KitchenDisplay.route)
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Kitchen Display Screen (KDS) — Phase 5.4
        composable(route = Screen.KitchenDisplay.route) { _ ->
            KitchenDisplayRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Order Detail — Phase 5.3
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType }),
        ) { _ ->
            OrderDetailRoute(
                onBack = { navController.popBackStack() },
                onBillingClick = { orderId, tableId ->
                    navController.navigate(Screen.OrderBilling.createRoute(orderId, tableId))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Search Screen
        composable(route = Screen.Search.route) { _ ->
            // TODO: SearchRoute()
        }

        // Billing overview — accessible from the drawer.
        // Full bills history will be added in Phase 7.
        // Actual bill generation is accessed from Order Detail → "Generate Bill".
        composable(route = Screen.Billing.route) { _ ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "To generate a bill, open an order\nand tap \"Generate Bill\".",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
        }

        // Order-specific bill generation — Phase 6.1 / 6.2
        composable(
            route = Screen.OrderBilling.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.LongType },
                navArgument("tableId") { type = NavType.LongType },
            ),
        ) { _ ->
            BillingRoute(
                onBack = { navController.popBackStack() },
                onNavigateToPayment = { billId, orderId, tableId, totalAmount, remainingAmount ->
                    navController.navigate(
                        Screen.Payment.createRoute(billId, orderId, tableId, totalAmount, remainingAmount)
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Payment — Phase 6.3
        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("billId")          { type = NavType.LongType },
                navArgument("orderId")         { type = NavType.LongType },
                navArgument("tableId")         { type = NavType.LongType },
                navArgument("totalAmount")     { type = NavType.StringType },
                navArgument("remainingAmount") { type = NavType.StringType },
            ),
        ) { _ ->
            PaymentRoute(
                onBack = { navController.popBackStack() },
                onPaymentSuccess = {
                    // Payment confirmed → go back to Order List, clear billing back-stack
                    navController.navigate(Screen.OrderList.route) {
                        popUpTo(Screen.FoodList.route) { inclusive = false }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Settings Screen — Phase 7.3
        composable(route = Screen.Settings.route) { _ ->
            SettingsRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ── Phase 8 — Reports & Analytics ────────────────────────────────────

        // Sales Report Screen — date range picker + metrics + top items
        composable(route = Screen.SalesReport.route) { _ ->
            SalesReportRoute(modifier = Modifier.fillMaxSize())
        }

        // Order History Screen — date range picker + status filter + order cards
        composable(route = Screen.OrderHistory.route) { _ ->
            OrderHistoryRoute(modifier = Modifier.fillMaxSize())
        }
    }
}
