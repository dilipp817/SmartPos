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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Table List / Selection Screen.
 *
 * Layout (landscape tablet):
 * ┌──────────────────────────────────────────┐
 * │  ← Back   Select a Table   🟢 4 available│
 * ├──────────────────────────────────────────┤
 * │  [All Tables] [Available] [Occupied]     │
 * ├──────────────────────────────────────────┤
 * │  ┌───┐  ┌───┐  ┌───┐  ┌───┐  ┌───┐     │
 * │  │T-1│  │T-2│  │T-3│  │T-4│  │T-5│     │
 * │  └───┘  └───┘  └───┘  └───┘  └───┘     │
 * │  ┌───┐  ┌───┐  ...                      │
 * └──────────────────────────────────────────┘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableListScreen(
    uiState: TableUiState,
    onTableClick: (Table) -> Unit,
    onFilterSelect: (TableFilter) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        TableHeader(
            availableCount = uiState.availableCount,
            onBack = onBack,
            onRefresh = onRefresh,
        )

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // ── Filter tabs ─────────────────────────────────────────────────────
        TableFilterRow(
            selectedFilter = uiState.selectedFilter,
            onFilterSelect = onFilterSelect,
        )

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // ── Content ─────────────────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> TableLoadingGrid()
                uiState.errorMessage != null -> TableErrorState(message = uiState.errorMessage, onRetry = onRefresh)
                uiState.tables.isEmpty() -> TableEmptyState(filter = uiState.selectedFilter)
                else -> TableGrid(
                    tables = uiState.tables,
                    onTableClick = onTableClick,
                )
            }
        }
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
                contentDescription = "Back",
                tint = Color(0xFF212121),
            )
        }

        Text(
            text = "Select a Table",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f).padding(start = 4.dp),
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
                    text = "$availableCount available",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
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
                label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
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
    onTableClick: (Table) -> Unit,
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
            )
        }
    }
}

@Composable
private fun TableLoadingGrid() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFE33E3E))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Loading tables…",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
            )
        }
    }
}

@Composable
private fun TableEmptyState(filter: TableFilter) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "🪑", style = MaterialTheme.typography.displayMedium)
            Text(
                text = when (filter) {
                    TableFilter.ALL       -> "No tables found"
                    TableFilter.AVAILABLE -> "No available tables right now"
                    TableFilter.OCCUPIED  -> "No tables are currently occupied"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575),
            )
        }
    }
}

@Composable
private fun TableErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "⚠️", style = MaterialTheme.typography.displayMedium)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
            )
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text("Retry", color = Color(0xFFE33E3E))
            }
        }
    }
}

