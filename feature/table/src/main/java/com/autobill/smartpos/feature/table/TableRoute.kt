package com.autobill.smartpos.feature.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Navigation entry point for the Table List screen.
 *
 * [onTableSelected] — called with the selected tableId when the user taps
 *                     an AVAILABLE table. Navigates to Create Order (Phase 5).
 * [onBack]          — pops back to the previous screen (Food/Cart screen).
 */
@Composable
fun TableRoute(
    onTableSelected: (tableId: Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: TableViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // M-09: 30s polling — start on resume, stop on pause
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE)  { viewModel.onPause() }

    TableListScreen(
        uiState = uiState,
        modifier = modifier,
        // ── Navigation ──────────────────────────────────────────────────
        onTableClick = { table ->
            if (table.status == TableStatus.AVAILABLE) onTableSelected(table.id)
        },
        onBack = onBack,
        // ── Filter / Refresh ─────────────────────────────────────────────
        onFilterSelect = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        // ── Status update ────────────────────────────────────────────────
        onChangeTableStatus = viewModel::showStatusUpdateDialog,
        onStatusConfirmed = viewModel::confirmStatusUpdate,
        onStatusDialogDismiss = viewModel::dismissStatusUpdateDialog,
        onStatusUpdateSuccessConsumed = viewModel::onStatusUpdateSuccessConsumed,
        // ── CRUD (admin / manager) ───────────────────────────────────────
        onAddTable = viewModel::showCreateDialog,
        onEditTable = viewModel::showEditDialog,
        onDeleteTable = viewModel::showDeleteConfirm,
        onCrudConfirmCreate = viewModel::confirmCreate,
        onCrudConfirmEdit = viewModel::confirmEdit,
        onCrudConfirmDelete = viewModel::confirmDelete,
        onCrudDialogDismiss = viewModel::dismissCrudDialog,
        onCrudSuccessConsumed = viewModel::onCrudSuccessConsumed,
    )
}
