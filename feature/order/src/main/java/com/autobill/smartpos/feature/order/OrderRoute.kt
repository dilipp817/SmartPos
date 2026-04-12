package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Order List screen.
 *
 * [onOrderClick]        — called with orderId when user taps a card; navigates to OrderDetail.
 * [onBack]              — pops back to the previous screen.
 */
@Composable
fun OrderRoute(
    onOrderClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OrderViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrderListScreen(
        uiState = uiState,
        modifier = modifier,
        onOrderClick = onOrderClick,
        onBack = onBack,
        onFilterSelect = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        onSearchActiveToggle = viewModel::onSearchActiveToggle,
        onSearchQueryChange = viewModel::onSearchQueryChange,
    )
}

