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
import com.autobill.smartpos.feature.admin.AdminDashboardRoute
import com.autobill.smartpos.feature.admin.menu.MenuManagementRoute
import com.autobill.smartpos.feature.admin.settings.AdminSettingsRoute
import com.autobill.smartpos.feature.admin.staff.StaffManagementRoute
import com.autobill.smartpos.feature.admin.inventory.InventoryRoute
import com.autobill.smartpos.feature.admin.category.CategoryManagementRoute

@Composable
fun AppNavHost(
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    canAccessAdmin: Boolean = false,
    navController: NavHostController = rememberNavController(),
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route ?: startDestination
    val isAuthenticated = currentRoute != Screen.Login.route

    if (isAuthenticated) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    AppDrawerContent(
                        currentRoute   = currentRoute,
                        canAccessAdmin = canAccessAdmin,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = (route != Screen.OrderList.route)
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        onLogout = onLogout,
                    )
                }
            },
            modifier = modifier.fillMaxSize(),
        ) {
            AppNavGraph(
                navController    = navController,
                startDestination = startDestination,
                onLogout         = onLogout,
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
                onCheckoutClick = { orderType ->
                    // DINE_IN + TABLE_MANAGEMENT=true → go to table selection
                    navController.navigate(Screen.TableList.createRoute(orderType.value))
                },
                onLogout = onLogout,
                onNavigateToMenuManagement = {
                    navController.navigate(Screen.MenuManagement.route)
                },
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

        // Table List / Selection Screen — user selects an available table before creating a Dine-In order
        composable(
            route = Screen.TableList.route,
            arguments = listOf(navArgument("orderType") { type = NavType.StringType }),
        ) { entry ->
            val orderType = entry.arguments?.getString("orderType") ?: "DINE_IN"
            TableRoute(
                onTableSelected = { tableId ->
                    navController.navigate(Screen.CreateOrder.createRoute(tableId, orderType))
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Create Order — receives tableId + orderType; tableId=0 means no table (TAKEAWAY)
        composable(
            route = Screen.CreateOrder.route,
            arguments = listOf(
                navArgument("tableId")   { type = NavType.LongType },
                navArgument("orderType") { type = NavType.StringType },
            ),
        ) { _ ->
            CreateOrderRoute(
                onOrderCreated = { _ ->
                    // Return cashier to FoodList (main screen) so they can start a fresh order
                    navController.popBackStack(Screen.FoodList.route, inclusive = false)
                },
                onBack = { navController.popBackStack() },
                onReselectTable = {
                    // Table-based orders: pop back to TableList to re-select.
                    // No-table orders (TAKEAWAY): TableList is not in the back stack —
                    // fall back to FoodList. In practice a 409 should never fire for
                    // tableId=0, but we guard it here for safety.
                    // popBackStack returns true if TableList was found and popped,
                    // false if it wasn't in the stack (TAKEAWAY / no-table order).
                    val popped = navController.popBackStack(Screen.TableList.route, inclusive = false)
                    if (!popped) {
                        navController.popBackStack(Screen.FoodList.route, inclusive = false)
                    }
                },
                onOrderQueued = {
                    // Order saved offline — go back to FoodList (Phase 9.2)
                    navController.popBackStack(Screen.FoodList.route, inclusive = false)
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
                    navController.navigate(Screen.OrderBilling.createRoute(orderId, tableId ?: 0L))
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Search is embedded in the Food List screen via SearchFilterPanel — no separate route needed.

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

        // ── Phase 9.3 — Admin Dashboard ───────────────────────────────────────

        composable(route = Screen.AdminDashboard.route) { _ ->
            AdminDashboardRoute(
                onNavigateToMenuManagement = {
                    navController.navigate(Screen.MenuManagement.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.AdminSettings.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.SalesReport.route)
                },
                onNavigateToStaffManagement = {
                    navController.navigate(Screen.StaffManagement.route)
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.InventoryManagement.route)
                },
                onNavigateToCategoryManagement = {
                    navController.navigate(Screen.CategoryManagement.route)
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(route = Screen.MenuManagement.route) { _ ->
            MenuManagementRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(route = Screen.AdminSettings.route) { _ ->
            AdminSettingsRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(route = Screen.StaffManagement.route) { _ ->
            StaffManagementRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(route = Screen.InventoryManagement.route) { _ ->
            InventoryRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }

        composable(route = Screen.CategoryManagement.route) { _ ->
            CategoryManagementRoute(
                onBack   = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
