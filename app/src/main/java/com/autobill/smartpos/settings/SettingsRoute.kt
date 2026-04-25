package com.autobill.smartpos.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.R
import kotlinx.coroutines.launch

/**
 * Navigation entry point for the Settings screen.
 *
 * Handles the [android.Manifest.permission.BLUETOOTH_CONNECT] runtime permission
 * (API 31+) before delegating to [SettingsScreen]. On older API levels the
 * permission is not needed at runtime.
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val btPermissionDeniedMsg = stringResource(R.string.bt_permission_denied)

    // One-shot: show test-print result as snackbar
    LaunchedEffect(uiState.testPrintResult) {
        val msg = uiState.testPrintResult
        if (msg != null) {
            viewModel.onTestPrintResultConsumed()
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
        }
    }

    // Request BLUETOOTH_CONNECT (API 31+) then open the picker only if granted.
    val btPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.openPrinterPicker()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message  = btPermissionDeniedMsg,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier     = modifier,
    ) { innerPadding ->
        SettingsScreen(
            uiState                = uiState,
            onBack                 = onBack,
            onToggleDarkTheme      = viewModel::toggleDarkTheme,
            onLogout               = viewModel::logout,
            onSelectPrinterClick   = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    btPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                } else {
                    viewModel.openPrinterPicker()
                }
            },
            onPrinterSelected      = viewModel::selectPrinter,
            onDismissPrinterPicker = viewModel::dismissPrinterPicker,
            onTestPrint            = viewModel::testPrint,
            modifier               = Modifier.padding(innerPadding),
        )
    }
}
