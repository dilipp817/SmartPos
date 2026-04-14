package com.autobill.smartpos.feature.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.ui.components.badges.CountBadge

/**
 * Order List Screen.
 *
 * Displays all orders for the current restaurant, filterable by status
 * and searchable by order/table number.
 *
 * Tabs: All | Active | Pending | In Progress | Completed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    uiState: OrderUiState,
    onOrderClick: (Long) -> Unit,
    onBack: () -> Unit,
    onKdsClick: () -> Unit,
    onFilterSelect: (OrderFilter) -> Unit,
    onRefresh: () -> Unit,
    onSearchActiveToggle: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
    ) {
        // ── Header ────────────────────────────────────────────────────────
        OrderListHeader(
            pendingCount   = uiState.pendingCount,
            isSearchActive = uiState.isSearchActive,
            onBack         = onBack,
            onKdsClick     = onKdsClick,
            onRefresh      = onRefresh,
            onSearchToggle = { onSearchActiveToggle(!uiState.isSearchActive) },
        )

        // ── Search bar (collapsible) ───────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isSearchActive,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            OrderSearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
                onClose = { onSearchActiveToggle(false) },
            )
        }

        // ── Phase 9.1: connection banner ──────────────────────────────────
        AnimatedVisibility(
            visible = uiState.connectionState != ConnectionState.CONNECTED,
            enter   = expandVertically(),
            exit    = shrinkVertically(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF3E0))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = "⚠", style = MaterialTheme.typography.labelMedium)
                Text(
                    text  = "Live updates paused — reconnecting…",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // ── Filter chips ──────────────────────────────────────────────────
        OrderFilterRow(
            selectedFilter = uiState.selectedFilter,
            onFilterSelect = onFilterSelect,
        )

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // ── Main content ──────────────────────────────────────────────────
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> OrderLoadingState()
                uiState.errorMessage != null -> OrderErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRefresh,
                )
                uiState.orders.isEmpty() -> OrderEmptyState(
                    filter = uiState.selectedFilter,
                    searchQuery = uiState.searchQuery,
                )
                else -> OrderList(
                    orders = uiState.orders,
                    onOrderClick = onOrderClick,
                )
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun OrderListHeader(
    pendingCount: Int,
    isSearchActive: Boolean,
    onBack: () -> Unit,
    onKdsClick: () -> Unit,
    onRefresh: () -> Unit,
    onSearchToggle: () -> Unit,
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
            text = "Orders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        // Pending badge — shown in header next to search icon
        if (pendingCount > 0) {
            Box(modifier = Modifier.padding(end = 4.dp)) {
                CountBadge(count = pendingCount)
            }
        }

        // KDS navigation button
        TextButton(
            onClick = onKdsClick,
            colors  = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFFE33E3E),
            ),
        ) {
            Text(
                text       = "🍳 KDS",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        IconButton(onClick = onSearchToggle) {
            Icon(
                imageVector = if (isSearchActive) Icons.Default.Clear else Icons.Default.Search,
                contentDescription = if (isSearchActive) "Close search" else "Search orders",
                tint = Color(0xFF757575),
            )
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
private fun OrderSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Search by order no. or table…", style = MaterialTheme.typography.bodyMedium)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9E9E9E))
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF9E9E9E))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFE33E3E),
                unfocusedBorderColor = Color(0xFFE0E0E0),
            ),
        )
        TextButton(onClick = onClose) {
            Text("Cancel", color = Color(0xFFE33E3E))
        }
    }
}

@Composable
private fun OrderFilterRow(
    selectedFilter: OrderFilter,
    onFilterSelect: (OrderFilter) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(OrderFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelect(filter) },
                label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFE33E3E),
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun OrderList(
    orders: List<Order>,
    onOrderClick: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(orders, key = { it.id }) { order ->
            OrderItemCard(
                order = order,
                onClick = onOrderClick,
            )
        }
        // Bottom spacing so last card isn't hidden behind nav bar
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun OrderLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFFE33E3E),
            strokeWidth = 3.dp,
        )
    }
}

@Composable
private fun OrderEmptyState(
    filter: OrderFilter,
    searchQuery: String,
) {
    val message = when {
        searchQuery.isNotBlank() -> "No orders found for \"$searchQuery\""
        filter == OrderFilter.ACTIVE -> "No active orders right now"
        else -> "No ${filter.label.lowercase()} orders"
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "📋",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun OrderErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB00020),
                fontWeight = FontWeight.Medium,
            )
            TextButton(onClick = onRetry) {
                Text("Retry", color = Color(0xFFE33E3E))
            }
        }
    }
}

