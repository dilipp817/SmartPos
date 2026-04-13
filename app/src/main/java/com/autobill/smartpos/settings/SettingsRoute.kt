package com.autobill.smartpos.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Settings screen.
 *
 * [onBack] — pop back to the previous screen (drawer tap navigates here so
 *            back returns to wherever the user was before).
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState           = uiState,
        onBack            = onBack,
        onToggleDarkTheme = viewModel::toggleDarkTheme,
        onLogout          = viewModel::logout,
        modifier          = modifier,
    )
}

