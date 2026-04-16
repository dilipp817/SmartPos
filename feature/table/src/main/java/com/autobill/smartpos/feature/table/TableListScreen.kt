package com.autobill.smartpos.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Table List / Selection Screen.
 *
 * For admin / manager — shows:
 *  - FAB "Add Table" (bottom-right)
 *  - ⋮ overflow on each card with Edit / Delete options
 *
 * For staff — unchanged behaviour (tap available to select, tap others to change status).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableListScreen(
    uiState: TableUiState,
    // ── Navigation ────────────────────────────────────────────────────────
    onTableClick: (Table) -> Unit,
    onBack: () -> Unit,
    // ── Filter / Refresh ──────────────────────────────────────────────────
    onFilterSelect: (TableFilter) -> Unit,
    onRefresh: () -> Unit,
    // ── Status update ─────────────────────────────────────────────────────
    onChangeTableStatus: (Table) -> Unit,
    onStatusConfirmed: (Table, TableStatus) -> Unit,
    onStatusDialogDismiss: () -> Unit,
    onStatusUpdateSuccessConsumed: () -> Unit,
    // ── CRUD (admin / manager) ────────────────────────────────────────────
    onAddTable: () -> Unit,
    onEditTable: (Table) -> Unit,
    onDeleteTable: (Table) -> Unit,
    onCrudConfirmCreate: (String, Int, Int) -> Unit,
    onCrudConfirmEdit: (Table, String, Int, Int) -> Unit,
    onCrudConfirmDelete: (Table) -> Unit,
    onCrudDialogDismiss: () -> Unit,
    onCrudSuccessConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Status update success (green snackbar)
    val statusUpdatedMsg = stringResource(R.string.table_status_updated_snackbar)
    LaunchedEffect(uiState.statusUpdateSuccess) {
        if (uiState.statusUpdateSuccess) {
            snackbarHostState.showSnackbar(statusUpdatedMsg)
            onStatusUpdateSuccessConsumed()
        }
    }
    // CRUD success (green snackbar — reuses the same host)
    LaunchedEffect(uiState.crudSuccessMessage) {
        uiState.crudSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onCrudSuccessConsumed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
        ) {
            TableHeader(
                availableCount = uiState.availableCount,
                onBack = onBack,
                onRefresh = onRefresh,
            )
            HorizontalDivider(color = Color(0xFFE0E0E0))
            TableFilterRow(
                selectedFilter = uiState.selectedFilter,
                onFilterSelect = onFilterSelect,
            )
            HorizontalDivider(color = Color(0xFFE0E0E0))

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading -> TableLoadingGrid()
                    uiState.errorMessage != null -> TableErrorState(
                        message = uiState.errorMessage,
                        onRetry = onRefresh,
                    )
                    uiState.tables.isEmpty() -> TableEmptyState(filter = uiState.selectedFilter)
                    else -> TableGrid(
                        tables = uiState.tables,
                        canManageTables = uiState.canManageTables,
                        onTableClick = onTableClick,
                        onChangeTableStatus = onChangeTableStatus,
                        onEditTable = onEditTable,
                        onDeleteTable = onDeleteTable,
                    )
                }
            }
        }

        // ── FAB — Add Table (admin / manager only) ───────────────────────
        if (uiState.canManageTables) {
            ExtendedFloatingActionButton(
                onClick = onAddTable,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.table_fab_add)) },
                containerColor = Color(0xFFE33E3E),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
            )
        }

        // ── Snackbar (success) ───────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }

    // ── Status update dialog ──────────────────────────────────────────────────
    if (uiState.statusUpdateDialog != null) {
        TableStatusUpdateDialog(
            dialogState = uiState.statusUpdateDialog,
            isUpdating = uiState.isUpdatingStatus,
            errorMessage = uiState.statusUpdateError,
            onConfirm = { newStatus ->
                onStatusConfirmed(uiState.statusUpdateDialog.table, newStatus)
            },
            onDismiss = onStatusDialogDismiss,
        )
    }

    // ── CRUD dialogs ──────────────────────────────────────────────────────────
    when (val dialog = uiState.crudDialog) {
        is TableCrudDialogState.Create -> TableCrudDialog(
            editTable = null,
            isInFlight = uiState.isCrudInFlight,
            errorMessage = uiState.crudError,
            onConfirm = { num, floor, cap -> onCrudConfirmCreate(num, floor, cap) },
            onDismiss = onCrudDialogDismiss,
        )
        is TableCrudDialogState.Edit -> TableCrudDialog(
            editTable = dialog.table,
            isInFlight = uiState.isCrudInFlight,
            errorMessage = uiState.crudError,
            onConfirm = { num, floor, cap -> onCrudConfirmEdit(dialog.table, num, floor, cap) },
            onDismiss = onCrudDialogDismiss,
        )
        is TableCrudDialogState.DeleteConfirm -> TableDeleteConfirmDialog(
            table = dialog.table,
            isInFlight = uiState.isCrudInFlight,
            errorMessage = uiState.crudError,
            onConfirm = { onCrudConfirmDelete(dialog.table) },
            onDismiss = onCrudDialogDismiss,
        )
        null -> Unit
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun TableHeader(
    availableCount: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = Color(0xFF212121),
            )
        }

        Text(
            text = stringResource(R.string.table_list_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        // Available count badge
        if (availableCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50)),
                )
                Text(
                    text = stringResource(R.string.table_available_count, availableCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.cd_refresh),
                tint = Color(0xFF757575),
            )
        }
    }
}

@Composable
private fun TableFilterRow(
    selectedFilter: TableFilter,
    onFilterSelect: (TableFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TableFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelect(filter) },
                label = {
                    val filterLabel = when (filter) {
                        TableFilter.ALL       -> stringResource(R.string.table_filter_all)
                        TableFilter.AVAILABLE -> stringResource(R.string.table_filter_available)
                        TableFilter.OCCUPIED  -> stringResource(R.string.table_filter_occupied)
                    }
                    Text(filterLabel, style = MaterialTheme.typography.labelMedium)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE33E3E),
                    selectedLabelColor = Color.White,
                ),
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun TableGrid(
    tables: List<Table>,
    canManageTables: Boolean,
    onTableClick: (Table) -> Unit,
    onChangeTableStatus: (Table) -> Unit,
    onEditTable: (Table) -> Unit,
    onDeleteTable: (Table) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 180.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(tables, key = { it.id }) { table ->
            TableGridCard(
                table = table,
                onClick = { onTableClick(table) },
                onChangeStatus = { onChangeTableStatus(table) },
                canManageTables = canManageTables,
                onEdit = { onEditTable(table) },
                onDelete = { onDeleteTable(table) },
            )
        }
    }
}

@Composable
private fun TableLoadingGrid() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFE33E3E))
            Spacer(modifier = Modifier.height(12.dp))
            Text(stringResource(R.string.table_list_loading), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
        }
    }
}

@Composable
private fun TableEmptyState(filter: TableFilter) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("🪑", style = MaterialTheme.typography.displayMedium)
            Text(
                text = when (filter) {
                    TableFilter.ALL       -> stringResource(R.string.table_empty_all)
                    TableFilter.AVAILABLE -> stringResource(R.string.table_empty_available)
                    TableFilter.OCCUPIED  -> stringResource(R.string.table_empty_occupied)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575),
            )
        }
    }
}

@Composable
private fun TableErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("⚠️", style = MaterialTheme.typography.displayMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry), color = Color(0xFFE33E3E))
            }
        }
    }
}
