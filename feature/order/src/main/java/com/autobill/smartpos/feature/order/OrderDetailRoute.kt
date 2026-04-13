package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Order Detail screen.
 *
 * [onBack] — called when the user taps Back or after the order is successfully cancelled.
 */
@Composable
fun OrderDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OrderDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot: navigate back after successful cancellation
    LaunchedEffect(uiState.orderCancelled) {
        if (uiState.orderCancelled) {
            viewModel.onOrderCancelledConsumed()
            onBack()
        }
    }

    OrderDetailScreen(
        uiState                         = uiState,
        modifier                        = modifier,
        onBack                          = onBack,
        onRefresh                       = viewModel::refresh,
        onStatusUpdate                  = viewModel::updateStatus,
        // Add Item
        onAddItemClick                  = viewModel::showAddItemDialog,
        onDismissAddItemDialog          = viewModel::dismissAddItemDialog,
        onAddItemFoodSearch             = viewModel::onAddItemFoodSearch,
        onAddItemFoodSelected           = viewModel::onAddItemFoodSelected,
        onAddItemQuantityChange         = viewModel::onAddItemQuantityChange,
        onAddItemSpecialRequestsChange  = viewModel::onAddItemSpecialRequestsChange,
        onConfirmAddItem                = viewModel::confirmAddItem,
        // Edit Item
        onEditItemClick                 = viewModel::showEditItemDialog,
        onDismissEditItemDialog         = viewModel::dismissEditItemDialog,
        onEditItemQuantityChange        = viewModel::onEditItemQuantityChange,
        onEditItemSpecialRequestsChange = viewModel::onEditItemSpecialRequestsChange,
        onConfirmEditItem               = viewModel::confirmEditItem,
        // Remove Item
        onRemoveItem                    = viewModel::removeItem,
        // Cancel Order
        onShowCancelDialog              = viewModel::showCancelDialog,
        onDismissCancelDialog           = viewModel::dismissCancelDialog,
        onConfirmCancelOrder            = viewModel::confirmCancelOrder,
        // One-shot events
        onSuccessMessageConsumed        = viewModel::onSuccessMessageConsumed,
        onConflictMessageConsumed       = viewModel::onConflictMessageConsumed,
        onErrorConsumed                 = viewModel::onErrorConsumed,
    )
}

