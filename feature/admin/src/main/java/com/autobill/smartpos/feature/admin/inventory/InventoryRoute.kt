package com.autobill.smartpos.feature.admin.inventory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InventoryRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: InventoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    InventoryScreen(
        uiState              = uiState,
        onBack               = onBack,
        onRefresh            = viewModel::loadFoods,
        onSearchQueryChange  = viewModel::onSearchQueryChange,
        onCategorySelected   = viewModel::onCategorySelected,
        onToggleAvailability = viewModel::toggleAvailability,
        onDismissError       = viewModel::dismissError,
        onDismissSuccess     = viewModel::dismissSuccess,
        modifier             = modifier,
    )
}

