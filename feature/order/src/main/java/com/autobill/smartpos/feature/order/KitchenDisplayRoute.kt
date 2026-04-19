package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
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

    // M-09: start/stop fallback polling on lifecycle transitions
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE)  { viewModel.onPause() }

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
