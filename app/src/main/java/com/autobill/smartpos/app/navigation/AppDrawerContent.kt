package com.autobill.smartpos.app.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Permanent navigation drawer content for the authenticated shell.
 *
 * Design decisions:
 *  - [PermanentNavigationDrawer] is used (not Modal) because this is a
 *    tablet-first landscape app — the drawer is always visible and never
 *    overlays content. This is the Material 3 recommendation for
 *    expanded-window-size layouts.
 *  - The drawer is only mounted for authenticated routes (see [AppNavHost]).
 *    The Login screen gets full-width with no chrome.
 *  - [activeRoutes] maps sub-screens back to their parent section so the
 *    correct item stays highlighted when drilling into details
 *    (e.g. OrderDetail highlights "Orders", FoodDetail highlights "Menu").
 *  - Logout sits at the very bottom, separated by a divider, so staff
 *    cannot accidentally tap it while navigating.
 *
 * Future extensions:
 *  - Role-based visibility: hide "Settings" for non-admin roles.
 *  - Badge counts: add a badge on "Orders" / "Kitchen" for pending items.
 *  - Outlet switcher: add a header dropdown for multi-outlet super-admin.
 */

// ── Model ─────────────────────────────────────────────────────────────────────

private data class DrawerNavItem(
    val icon: ImageVector,
    val label: String,
    val navigateTo: String,
    /** All route patterns where this item should appear selected. */
    val activeRoutes: Set<String>,
)

private val drawerNavItems = listOf(
    DrawerNavItem(
        icon = Icons.Default.RestaurantMenu,
        label = "Menu",
        navigateTo = Screen.FoodList.route,
        activeRoutes = setOf(Screen.FoodList.route, Screen.FoodDetail.route),
    ),
    DrawerNavItem(
        icon = Icons.Default.TableBar,
        label = "Tables",
        navigateTo = Screen.TableList.route,
        activeRoutes = setOf(Screen.TableList.route, Screen.CreateOrder.route),
    ),
    DrawerNavItem(
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        label = "Orders",
        navigateTo = Screen.OrderList.route,
        activeRoutes = setOf(Screen.OrderList.route, Screen.OrderDetail.route),
    ),
    DrawerNavItem(
        icon = Icons.Default.Kitchen,
        label = "Kitchen",
        navigateTo = Screen.KitchenDisplay.route,
        activeRoutes = setOf(Screen.KitchenDisplay.route),
    ),
    DrawerNavItem(
        icon = Icons.Default.CreditCard,
        label = "Billing",
        navigateTo = Screen.Billing.route,
        activeRoutes = setOf(
            Screen.Billing.route,
            Screen.OrderBilling.route,
            Screen.Payment.route,
        ),
    ),
    DrawerNavItem(
        icon = Icons.Default.Settings,
        label = "Settings",
        navigateTo = Screen.Settings.route,
        activeRoutes = setOf(Screen.Settings.route),
    ),
)

// ── UI ────────────────────────────────────────────────────────────────────────

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp),
    ) {

        // ── Header ────────────────────────────────────────────────────────
        Text(
            text = "SmartPos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        // ── Navigation items ──────────────────────────────────────────────
        drawerNavItems.forEach { item ->
            val selected = currentRoute in item.activeRoutes
            NavigationDrawerItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = {
                    // Don't re-navigate if already on this section
                    if (!selected) onNavigate(item.navigateTo)
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        // Push logout to the bottom
        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))

        // ── Logout ────────────────────────────────────────────────────────
        NavigationDrawerItem(
            icon = {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            label = {
                Text(
                    "Logout",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

