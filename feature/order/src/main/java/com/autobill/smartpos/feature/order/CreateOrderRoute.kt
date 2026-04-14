package com.autobill.smartpos.feature.order

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Navigation entry point for the Create Order screen.
 *
 * [tableId]         — passed as a nav arg; resolved by [CreateOrderViewModel] via SavedStateHandle.
 * [onOrderCreated]  — navigates to Order Detail once the order is successfully submitted.
 * [onBack]          — pops back to the Table List.
 * [onReselectTable] — same as [onBack]; called when a 409 conflict clears the table selection.
 * [onOrderQueued]   — called when the order was saved offline; navigate back + show confirmation.
 */
@Composable
fun CreateOrderRoute(
    onOrderCreated: (orderId: Long) -> Unit,
    onBack: () -> Unit,
    onReselectTable: () -> Unit,
    onOrderQueued: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: CreateOrderViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot navigation after successful order creation
    LaunchedEffect(uiState.orderCreated) {
        uiState.orderCreated?.let { orderId ->
            viewModel.onOrderCreatedConsumed()
            onOrderCreated(orderId)
        }
    }

    // One-shot navigation on table conflict (409) — send user back to re-select
    LaunchedEffect(uiState.tableConflict) {
        if (uiState.tableConflict) {
            viewModel.onTableConflictConsumed()
            onReselectTable()
        }
    }

    // Offline queued → navigate back (Phase 9.2)
    LaunchedEffect(uiState.orderQueued) {
        if (uiState.orderQueued) {
            viewModel.onOrderQueuedConsumed()
            onOrderQueued()
        }
    }

    CreateOrderScreen(
        uiState           = uiState,
        onOrderTypeSelect = viewModel::selectOrderType,
        onNotesChange     = viewModel::updateNotes,
        onPlaceOrder      = viewModel::placeOrder,
        onBack            = onBack,
        onReselectTable   = onReselectTable,
        modifier          = modifier,
    )
}
