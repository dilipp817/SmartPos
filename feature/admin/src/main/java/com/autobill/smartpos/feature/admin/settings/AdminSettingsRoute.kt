package com.autobill.smartpos.feature.admin.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdminSettingsRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminSettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminSettingsScreen(
        uiState                      = uiState,
        onBack                       = onBack,
        onTaxRateChange              = viewModel::onTaxRateChange,
        onEnableTipsChange           = viewModel::onEnableTipsChange,
        onDefaultTipPercentageChange = viewModel::onDefaultTipPercentageChange,
        onAutoPrintBillChange        = viewModel::onAutoPrintBillChange,
        onTaxInclusiveChange         = viewModel::onTaxInclusiveChange,
        onSave                       = viewModel::saveSettings,
        onDismissError               = viewModel::dismissError,
        onDismissSuccess             = viewModel::dismissSuccess,
        modifier                     = modifier,
    )
}

