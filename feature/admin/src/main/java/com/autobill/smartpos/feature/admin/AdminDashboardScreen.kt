package com.autobill.smartpos.feature.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    uiState: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onNavigateToMenuManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStaffManagement: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Dashboard", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (uiState.isSuperAdmin) "Super Admin · All Outlets"
                                   else "${uiState.restaurant?.name ?: ""} · ${uiState.role.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        if (uiState.isSuperAdmin) {
            SuperAdminBanner(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Stats row ──────────────────────────────────────────────
                item {
                    Text(
                        "Live Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        StatCard(
                            icon   = Icons.Default.BarChart,
                            label  = "Today's Revenue",
                            value  = "₹${"%.0f".format(uiState.stats.todayRevenue)}",
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon   = Icons.Default.ShoppingCart,
                            label  = "Active Orders",
                            value  = uiState.stats.activeOrders.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon   = Icons.Default.TableBar,
                            label  = "Free Tables",
                            value  = uiState.stats.availableTables.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            icon   = Icons.Default.CloudOff,
                            label  = "Offline Queue",
                            value  = uiState.stats.offlineQueueCount.toString(),
                            highlight = uiState.stats.offlineQueueCount > 0,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // ── Unavailable foods alert ────────────────────────────────
                if (uiState.stats.unavailableFoodCount > 0) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.RestaurantMenu,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${uiState.stats.unavailableFoodCount} menu item(s) marked unavailable",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                    }
                }

                // ── Quick actions ──────────────────────────────────────────
                item {
                    Text(
                        "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    // Row 1: core management actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ActionCard(
                            icon    = Icons.Default.MenuBook,
                            label   = "Menu\nManagement",
                            onClick = onNavigateToMenuManagement,
                            modifier = Modifier.weight(1f),
                        )
                        ActionCard(
                            icon    = Icons.Default.Settings,
                            label   = "Restaurant\nSettings",
                            onClick = onNavigateToSettings,
                            modifier = Modifier.weight(1f),
                        )
                        ActionCard(
                            icon    = Icons.Default.BarChart,
                            label   = "Sales\nReports",
                            onClick = onNavigateToReports,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    // Row 2: staff & inventory
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        ActionCard(
                            icon    = Icons.Default.People,
                            label   = "Staff\nManagement",
                            onClick = onNavigateToStaffManagement,
                            modifier = Modifier.weight(1f),
                        )
                        ActionCard(
                            icon    = Icons.Default.Inventory2,
                            label   = "Inventory\nTracking",
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(1f),
                        )
                        // Spacer card to keep consistent 3-column grid width
                        Spacer(Modifier.weight(1f))
                    }
                }

                // ── Outlet info ────────────────────────────────────────────
                uiState.restaurant?.let { r ->
                    item {
                        Text(
                            "Outlet Info",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(8.dp))
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp),
                                   verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutletInfoRow("Name",     r.name)
                                OutletInfoRow("Address",  r.address)
                                OutletInfoRow("Phone",    r.phone)
                                OutletInfoRow("Currency", r.currency)
                                OutletInfoRow("Tax Rate", "${r.taxRate}%")
                                OutletInfoRow("Tips",     if (r.settings.enableTips) "Enabled" else "Disabled")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
) {
    val containerColor = if (highlight)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
        ) {
            Icon(icon, contentDescription = null,
                 tint = if (highlight) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                 modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(value,
                 style = MaterialTheme.typography.titleLarge,
                 fontWeight = FontWeight.Bold,
                 color = if (highlight) MaterialTheme.colorScheme.error
                         else MaterialTheme.colorScheme.onSurface)
            Text(label,
                 style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier.height(88.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun OutletInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label,
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             modifier = Modifier.width(80.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SuperAdminBanner(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
               verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Business, contentDescription = null,
                 modifier = Modifier.size(64.dp),
                 tint = MaterialTheme.colorScheme.primary)
            Text("Super Admin View", style = MaterialTheme.typography.headlineSmall,
                 fontWeight = FontWeight.Bold)
            Text("You have cross-outlet access.\nSelect an outlet from Settings to view its dashboard.",
                 style = MaterialTheme.typography.bodyMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                 textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

