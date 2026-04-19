package com.autobill.smartpos.feature.admin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.feature.admin.R

/**
 * Edit Outlet Info screen — contract §8.4.
 * Allows editing: outlet name, display name, manager name, and address.
 * Tax, tips, and print settings removed (post-production backlog).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    uiState: AdminSettingsUiState,
    onBack: () -> Unit,
    onOutletNameChange: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onOutletManagerChange: (String) -> Unit,
    onBuildingChange: (String) -> Unit,
    onStreetChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onZipCodeChange: (String) -> Unit,
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
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                             contentDescription = stringResource(R.string.cd_back))
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
                // ── Outlet Details ─────────────────────────────────────────
                Text(stringResource(R.string.settings_section_outlet),
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value         = uiState.outletName,
                    onValueChange = onOutletNameChange,
                    label         = { Text(stringResource(R.string.settings_field_outlet_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value         = uiState.displayName,
                    onValueChange = onDisplayNameChange,
                    label         = { Text(stringResource(R.string.settings_field_display_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value         = uiState.outletManager,
                    onValueChange = onOutletManagerChange,
                    label         = { Text(stringResource(R.string.settings_field_manager_name)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                HorizontalDivider()

                // ── Address ────────────────────────────────────────────────
                Text(stringResource(R.string.settings_section_address),
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value         = uiState.building,
                    onValueChange = onBuildingChange,
                    label         = { Text(stringResource(R.string.settings_field_building)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value         = uiState.street,
                    onValueChange = onStreetChange,
                    label         = { Text(stringResource(R.string.settings_field_street)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value         = uiState.location,
                    onValueChange = onLocationChange,
                    label         = { Text(stringResource(R.string.settings_field_location)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value         = uiState.zipCode,
                    onValueChange = onZipCodeChange,
                    label         = { Text(stringResource(R.string.settings_field_zip_code)) },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))

                // ── Save ───────────────────────────────────────────────────
                Button(
                    onClick  = onSave,
                    enabled  = uiState.isDirty && !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (uiState.isSaving)
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.settings_save_button))
                }
            }
        }
    }
}
