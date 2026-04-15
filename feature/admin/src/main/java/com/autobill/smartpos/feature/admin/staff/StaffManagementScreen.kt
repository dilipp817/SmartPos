package com.autobill.smartpos.feature.admin.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.model.UserRole
import com.autobill.smartpos.domain.model.canApplyDiscounts
import com.autobill.smartpos.domain.model.canCancelOrders
import com.autobill.smartpos.domain.model.canManageMenu
import com.autobill.smartpos.domain.model.canManageTables
import com.autobill.smartpos.domain.model.isSuperAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
    uiState: StaffManagementUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Management") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ── Current Session User ───────────────────────────────────
                uiState.currentUser?.let { user ->
                    Text(
                        "Active Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CurrentUserCard(user = user)

                    HorizontalDivider()

                    // ── Role Permissions ──────────────────────────────────
                    Text(
                        "Role Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    RolePermissionsCard(user = user)

                    HorizontalDivider()
                }

                // ── Role Guide ─────────────────────────────────────────────
                Text(
                    "Role Guide",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                RoleGuideCard()

                // ── Backend-Managed Note ───────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Backend-Managed Accounts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "User accounts for this outlet are created and managed by your " +
                                "system administrator directly on the backend. " +
                                "Each billing counter is assigned its own account " +
                                "(e.g. counter_1, counter_2) — all sharing the same restaurantId.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun CurrentUserCard(user: User) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = if (user.isSuperAdmin()) "super_admin" else user.role.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            HorizontalDivider()

            ProfileRow(icon = Icons.Default.Email,       label = "Email",       value = user.email)
            ProfileRow(icon = Icons.Default.AdminPanelSettings, label = "User ID",  value = "#${user.id}")
            if (!user.restaurantId?.toString().isNullOrBlank()) {
                ProfileRow(icon = Icons.Default.Badge, label = "Restaurant",  value = "ID ${user.restaurantId}")
            }
            user.deviceId?.takeIf { it.isNotBlank() }?.let { deviceId ->
                ProfileRow(
                    icon  = Icons.Default.DevicesOther,
                    label = "Device",
                    value = "$deviceId · ${user.deviceType ?: "unknown"}",
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RolePermissionsCard(user: User) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PermissionRow(
                icon    = Icons.Default.Cancel,
                label   = "Cancel Orders",
                granted = user.canCancelOrders(),
            )
            PermissionRow(
                icon    = Icons.Default.LocalOffer,
                label   = "Apply Discounts",
                granted = user.canApplyDiscounts(),
            )
            PermissionRow(
                icon    = Icons.Default.RestaurantMenu,
                label   = "Manage Menu",
                granted = user.canManageMenu(),
            )
            PermissionRow(
                icon    = Icons.Default.TableBar,
                label   = "Manage Tables",
                granted = user.canManageTables(),
            )
        }
    }
}

@Composable
private fun PermissionRow(icon: ImageVector, label: String, granted: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            if (granted) Icons.Default.Check else Icons.Default.Cancel,
            contentDescription = if (granted) "Granted" else "Denied",
            tint = if (granted) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun RoleGuideCard() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RoleRow(
                role        = UserRole.STAFF,
                description = "Counter billing staff — can browse menu, manage cart, " +
                              "place orders and process payments.",
            )
            HorizontalDivider()
            RoleRow(
                role        = UserRole.MANAGER,
                description = "All staff permissions + cancel orders, apply discounts, " +
                              "manage tables.",
            )
            HorizontalDivider()
            RoleRow(
                role        = UserRole.ADMIN,
                description = "All manager permissions + manage menu items, categories, " +
                              "and restaurant settings.",
            )
            HorizontalDivider()
            RoleRow(
                role        = "super_admin",
                description = "Cross-outlet access — all admin permissions across every " +
                              "outlet. Restaurant ID is null.",
            )
        }
    }
}

@Composable
private fun RoleRow(role: String, description: String) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected  = false,
            onClick   = {},
            label     = { Text(role.uppercase(), style = MaterialTheme.typography.labelSmall) },
            modifier  = Modifier.width(100.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            description,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

