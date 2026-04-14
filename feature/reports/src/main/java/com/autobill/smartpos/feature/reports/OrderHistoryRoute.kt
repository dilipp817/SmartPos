package com.autobill.smartpos.feature.reports

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Order History screen (Phase 8.2).
 * Wires [OrderHistoryViewModel] → [OrderHistoryScreen].
 */
@Composable
fun OrderHistoryRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: OrderHistoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderHistoryScreen(
        uiState              = uiState,
        onShowStartPicker    = viewModel::showStartPicker,
        onShowEndPicker      = viewModel::showEndPicker,
        onStartDateSelected  = viewModel::onStartDateSelected,
        onEndDateSelected    = viewModel::onEndDateSelected,
        onDismissStartPicker = viewModel::dismissStartPicker,
        onDismissEndPicker   = viewModel::dismissEndPicker,
        onLoadOrders         = { viewModel.loadOrders(isRefresh = false) },
        onFilterSelected     = viewModel::onFilterSelected,
        onRefresh            = { viewModel.loadOrders(isRefresh = true) },
        onDismissError       = viewModel::dismissError,
        modifier             = modifier,
    )
}

