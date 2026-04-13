package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Kitchen Display Screen.
 *
 * [onBack] — called when the user taps the back arrow.
 */
@Composable
fun KitchenDisplayRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: KitchenDisplayViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    KitchenDisplayScreen(
        uiState                  = uiState,
        modifier                 = modifier,
        onBack                   = onBack,
        onRefresh                = viewModel::refresh,
        onFilterSelect           = viewModel::selectFilter,
        onUpdateItemStatus       = viewModel::updateItemStatus,
        onSuccessMessageConsumed = viewModel::onSuccessMessageConsumed,
        onErrorConsumed          = viewModel::onErrorConsumed,
    )
}
