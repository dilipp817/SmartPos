package com.autobill.smartpos.feature.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Sales Report screen (Phase 8.1).
 * Wires [SalesReportViewModel] → [SalesReportScreen].
 */
@Composable
fun SalesReportRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: SalesReportViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SalesReportScreen(
        uiState              = uiState,
        onShowStartPicker    = viewModel::showStartPicker,
        onShowEndPicker      = viewModel::showEndPicker,
        onStartDateSelected  = viewModel::onStartDateSelected,
        onEndDateSelected    = viewModel::onEndDateSelected,
        onDismissStartPicker = viewModel::dismissStartPicker,
        onDismissEndPicker   = viewModel::dismissEndPicker,
        onLoadReport         = viewModel::loadReport,
        onDismissError       = viewModel::dismissError,
        modifier             = modifier,
    )
}

