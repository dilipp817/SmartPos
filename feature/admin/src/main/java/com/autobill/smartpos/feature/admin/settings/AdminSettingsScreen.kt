package com.autobill.smartpos.feature.admin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    uiState: AdminSettingsUiState,
    onBack: () -> Unit,
    onTaxRateChange: (String) -> Unit,
    onEnableTipsChange: (Boolean) -> Unit,
    onDefaultTipPercentageChange: (String) -> Unit,
    onAutoPrintBillChange: (Boolean) -> Unit,
    onTaxInclusiveChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); onDismissError() }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); onDismissSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // ── Outlet info (read-only) ────────────────────────────────
                uiState.restaurant?.let { r ->
                    Text("Outlet", style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold)
                    Text(r.name, style = MaterialTheme.typography.bodyLarge)
                    Text("${r.address} · ${r.phone}",
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                }

                // ── Tax settings ───────────────────────────────────────────
                Text("Tax", style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value         = uiState.taxRate,
                    onValueChange = onTaxRateChange,
                    label         = { Text("Tax Rate (%)") },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier      = Modifier.fillMaxWidth(),
                )

                SettingToggleRow(
                    label   = "Tax Inclusive Pricing",
                    subtext = "Displayed prices already include tax",
                    checked = uiState.taxInclusive,
                    onCheckedChange = onTaxInclusiveChange,
                )

                HorizontalDivider()

                // ── Tips ───────────────────────────────────────────────────
                Text("Tips", style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold)

                SettingToggleRow(
                    label   = "Enable Tips",
                    subtext = "Show tip input on payment screen",
                    checked = uiState.enableTips,
                    onCheckedChange = onEnableTipsChange,
                )

                if (uiState.enableTips) {
                    OutlinedTextField(
                        value         = uiState.defaultTipPercentage,
                        onValueChange = onDefaultTipPercentageChange,
                        label         = { Text("Default Tip (%)") },
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier      = Modifier.fillMaxWidth(),
                    )
                }

                HorizontalDivider()

                // ── Printing ───────────────────────────────────────────────
                Text("Printing", style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold)

                SettingToggleRow(
                    label   = "Auto Print Bill",
                    subtext = "Automatically trigger print after bill generation",
                    checked = uiState.autoPrintBill,
                    onCheckedChange = onAutoPrintBillChange,
                )

                Spacer(Modifier.height(8.dp))

                // ── Save button ────────────────────────────────────────────
                Button(
                    onClick  = onSave,
                    enabled  = uiState.isDirty && !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (uiState.isSaving)
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text("Save Settings")
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    subtext: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(subtext, style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

