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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.model.UserRole
import com.autobill.smartpos.domain.model.canApplyDiscounts
import com.autobill.smartpos.domain.model.canCancelOrders
import com.autobill.smartpos.domain.model.canManageMenu
import com.autobill.smartpos.domain.model.canManageTables
import com.autobill.smartpos.domain.model.isSuperAdmin
import com.autobill.smartpos.feature.admin.R
import com.autobill.smartpos.ui.components.FullScreenLoading

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
                title = { Text(stringResource(R.string.staff_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        if (uiState.isLoading) {
            FullScreenLoading(modifier = Modifier.padding(padding))
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // ── Current Session User ───────────────────────────────────
                uiState.currentUser?.let { user ->
                    Text(
                        stringResource(R.string.staff_section_active_session),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    CurrentUserCard(user = user)

                    HorizontalDivider()

                    // ── Role Permissions ──────────────────────────────────
                    Text(
                        stringResource(R.string.staff_section_role_permissions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    RolePermissionsCard(user = user)

                    HorizontalDivider()
                }

                // ── Role Guide ─────────────────────────────────────────────
                Text(
                    stringResource(R.string.staff_section_role_guide),
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
                                stringResource(R.string.staff_backend_accounts_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.staff_backend_accounts_message),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
            } // Box
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
                            text = if (user.isSuperAdmin()) stringResource(R.string.staff_role_super_admin) else user.role.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }

            HorizontalDivider()

            ProfileRow(icon = Icons.Default.Email,       label = stringResource(R.string.staff_profile_email),       value = user.email)
            ProfileRow(icon = Icons.Default.AdminPanelSettings, label = stringResource(R.string.staff_profile_user_id),  value = stringResource(R.string.staff_profile_user_id_value, user.id))
            if (!user.restaurantId?.toString().isNullOrBlank()) {
                ProfileRow(icon = Icons.Default.Badge, label = stringResource(R.string.staff_profile_restaurant), value = stringResource(R.string.staff_profile_restaurant_value, user.restaurantId ?: 0L))
            }
            user.deviceId?.takeIf { it.isNotBlank() }?.let { deviceId ->
                ProfileRow(
                    icon  = Icons.Default.DevicesOther,
                    label = stringResource(R.string.staff_profile_device),
                    value = "$deviceId · ${user.deviceType ?: stringResource(R.string.staff_device_type_unknown)}",
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
                label   = stringResource(R.string.staff_perm_cancel_orders),
                granted = user.canCancelOrders(),
            )
            PermissionRow(
                icon    = Icons.Default.LocalOffer,
                label   = stringResource(R.string.staff_perm_apply_discounts),
                granted = user.canApplyDiscounts(),
            )
            PermissionRow(
                icon    = Icons.Default.RestaurantMenu,
                label   = stringResource(R.string.staff_perm_manage_menu),
                granted = user.canManageMenu(),
            )
            PermissionRow(
                icon    = Icons.Default.TableBar,
                label   = stringResource(R.string.staff_perm_manage_tables),
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
            contentDescription = if (granted) stringResource(R.string.staff_perm_granted_cd) else stringResource(R.string.staff_perm_denied_cd),
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
                description = stringResource(R.string.staff_role_counter_desc),
            )
            HorizontalDivider()
            RoleRow(
                role        = UserRole.MANAGER,
                description = stringResource(R.string.staff_role_manager_desc),
            )
            HorizontalDivider()
            RoleRow(
                role        = UserRole.ADMIN,
                description = stringResource(R.string.staff_role_admin_desc),
            )
            HorizontalDivider()
            RoleRow(
                role        = stringResource(R.string.staff_role_super_admin),
                description = stringResource(R.string.staff_role_super_admin_desc),
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

