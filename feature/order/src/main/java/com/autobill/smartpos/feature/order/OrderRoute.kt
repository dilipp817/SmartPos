package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Order List screen.
 *
 * [onOrderClick]        — called with orderId when user taps a card; navigates to OrderDetail.
 * [onKdsClick]          — navigates to the Kitchen Display Screen.
 * [onBack]              — pops back to the previous screen.
 */
@Composable
fun OrderRoute(
    onOrderClick: (Long) -> Unit,
    onKdsClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OrderViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // M-09: 15s polling — start on resume, stop on pause
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE)  { viewModel.onPause() }

    OrderListScreen(
        uiState = uiState,
        modifier = modifier,
        onOrderClick = onOrderClick,
        onKdsClick = onKdsClick,
        onBack = onBack,
        onFilterSelect = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        onSearchActiveToggle = viewModel::onSearchActiveToggle,
        onSearchQueryChange = viewModel::onSearchQueryChange,
    )
}
