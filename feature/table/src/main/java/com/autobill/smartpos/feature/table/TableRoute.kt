package com.autobill.smartpos.feature.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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

    TableListScreen(
        uiState = uiState,
        onTableClick = { table ->
            // Only AVAILABLE tables are clickable — guard here for safety
            if (table.status == TableStatus.AVAILABLE) {
                onTableSelected(table.id)
            }
        },
        onFilterSelect = viewModel::selectFilter,
        onRefresh = viewModel::refresh,
        onBack = onBack,
        modifier = modifier,
    )
}

