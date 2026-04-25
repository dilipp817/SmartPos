package com.autobill.smartpos.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.ui.theme.PrimaryBrand

/**
 * Settings Screen — Phase 7.3
 *
 * Sections:
 *  1. Profile   — username / email / role chip
 *  2. Restaurant — outlet name and currency
 *  3. Appearance — dark mode toggle
 *  4. Logout     — destructive action with confirmation dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onBack: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onLogout: () -> Unit,
    onSelectPrinterClick: () -> Unit,
    onPrinterSelected: (com.autobill.smartpos.domain.printer.PrinterDevice) -> Unit,
    onDismissPrinterPicker: () -> Unit,
    onTestPrint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Top bar ───────────────────────────────────────────────────────────
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Content ───────────────────────────────────────────────────────────
        LazyColumn(
            contentPadding      = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier            = Modifier.weight(1f).fillMaxWidth(),
        ) {

            // ── Profile section ─────────────────────────────────────────────
            item {
                SettingsSectionCard(
                    icon  = Icons.Default.Person,
                    title = "Profile",
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        // Avatar circle with initials
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimaryBrand),
                        ) {
                            Text(
                                text  = uiState.username.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = uiState.username.ifEmpty { "—" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (uiState.email.isNotEmpty()) {
                                Text(
                                    text  = uiState.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Role chip
                        if (uiState.role.isNotEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(uiState.roleChipColor))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text  = uiState.roleLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(uiState.roleChipTextColor),
                                )
                            }
                        }
                    }
                }
            }

            // ── Restaurant section ──────────────────────────────────────────
            if (uiState.restaurantName.isNotEmpty()) {
                item {
                    SettingsSectionCard(
                        icon  = Icons.Default.Restaurant,
                        title = "Outlet",
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text  = uiState.restaurantName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = uiState.currency,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // ── Printer section ─────────────────────────────────────────────
            item {
                SettingsSectionCard(
                    icon  = Icons.Default.Print,
                    title = "Printer",
                ) {
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = uiState.selectedPrinter?.name ?: "No printer selected",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (uiState.selectedPrinter != null) FontWeight.Medium else FontWeight.Normal,
                                color = if (uiState.selectedPrinter != null)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text  = "ESC/POS Bluetooth Thermal Printer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedButton(
                            onClick = onSelectPrinterClick,
                            shape   = RoundedCornerShape(8.dp),
                        ) {
                            Text(if (uiState.selectedPrinter != null) "Change" else "Select")
                        }
                    }
                    // Test Print button — visible only when a printer is saved
                    if (uiState.selectedPrinter != null) {
                        OutlinedButton(
                            onClick  = onTestPrint,
                            enabled  = !uiState.isTestPrinting,
                            shape    = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (uiState.isTestPrinting) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Printing…")
                            } else {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Print")
                            }
                        }
                    }
                }
            }

            // ── Appearance section ──────────────────────────────────────────
            item {
                SettingsSectionCard(
                    icon  = if (uiState.isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Appearance",
                ) {
                    Row(
                        modifier             = Modifier.fillMaxWidth(),
                        verticalAlignment    = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                text  = "Dark Mode",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text  = if (uiState.isDarkTheme) "Dark theme active"
                                        else "Light theme active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked         = uiState.isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = PrimaryBrand,
                            ),
                        )
                    }
                }
            }

            // ── App info section ────────────────────────────────────────────
            item {
                SettingsSectionCard(
                    icon  = null,
                    title = "About",
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = "SmartPos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text  = "v1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Logout ──────────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = { showLogoutDialog = true },
                    enabled  = !uiState.isLoggingOut,
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isLoggingOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logging out…")
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    // ── Logout confirmation dialog ─────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Log out?") },
            text    = { Text("You will need to enter your credentials again to access the app.") },
            confirmButton   = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Log out", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // ── Printer Picker Dialog ──────────────────────────────────────────────────
    if (uiState.showPrinterPickerDialog) {
        AlertDialog(
            onDismissRequest = onDismissPrinterPicker,
            title = { Text("Select Printer", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    if (!uiState.isBluetoothEnabled) {
                        Text(
                            text  = "Bluetooth is turned off.\n\nPlease enable Bluetooth in your device settings, then tap \"Select\" again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else if (uiState.pairedDevices.isEmpty()) {
                        Text(
                            text  = "No paired Bluetooth devices found.\n\nPair your thermal printer via Android Settings → Bluetooth, then return here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        uiState.pairedDevices.forEach { device ->
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = uiState.selectedPrinter?.macAddress == device.macAddress,
                                        onClick  = { onPrinterSelected(device) },
                                        role     = Role.RadioButton,
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected  = uiState.selectedPrinter?.macAddress == device.macAddress,
                                    onClick   = null,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text  = device.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissPrinterPicker) { Text("Close") }
            },
            shape          = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

// ── Settings section card ─────────────────────────────────────────────────────

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryBrand,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text  = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

