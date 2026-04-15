package com.autobill.smartpos.feature.admin.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MenuManagementRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: MenuManagementViewModel = hiltViewModel()
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    MenuManagementScreen(
        uiState             = uiState,
        formState           = formState,
        filteredFoods       = viewModel.filteredFoods,
        onBack              = onBack,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onCreateClick       = viewModel::openCreateDialog,
        onEditClick         = viewModel::openEditDialog,
        onDeleteClick       = viewModel::requestDelete,
        onConfirmDelete     = viewModel::confirmDelete,
        onCancelDelete      = viewModel::cancelDelete,
        onSaveFood          = viewModel::saveFood,
        onCloseDialog       = viewModel::closeDialog,
        onNameChange        = viewModel::onNameChange,
        onPriceChange       = viewModel::onPriceChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onImageUrlChange    = viewModel::onImageUrlChange,
        onCategoryChange    = viewModel::onCategoryChange,
        onVegetarianChange  = viewModel::onVegetarianChange,
        onSpicyChange       = viewModel::onSpicyChange,
        onAvailableChange   = viewModel::onAvailableChange,
        onPrepTimeChange    = viewModel::onPrepTimeChange,
        onAllergensChange   = viewModel::onAllergensChange,
        onCaloriesChange    = viewModel::onCaloriesChange,
        onDismissError      = viewModel::dismissError,
        onDismissSuccess    = viewModel::dismissSuccess,
        modifier            = modifier,
    )
}

