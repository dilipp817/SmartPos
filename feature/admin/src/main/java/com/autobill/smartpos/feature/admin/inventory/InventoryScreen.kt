package com.autobill.smartpos.feature.admin.inventory

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
import androidx.compose.foundation.layout.widthIn
import com.autobill.smartpos.ui.components.layout.LayoutTokens
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.feature.admin.R
import com.autobill.smartpos.ui.components.FullScreenLoading
import com.autobill.smartpos.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    uiState: InventoryUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    onToggleAvailability: (Long) -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onDismissSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.inventory_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        if (uiState.isLoading)
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        if (uiState.isLoading && uiState.foods.isEmpty()) {
            FullScreenLoading(modifier = Modifier.padding(padding))
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = LayoutTokens.MAX_WIDTH_CONTENT)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── Stats row ──────────────────────────────────────────────
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        StatChip(
                            icon  = Icons.Default.CheckCircle,
                            label = stringResource(R.string.inventory_filter_available),
                            count = uiState.availableCount,
                            tintOk = true,
                            modifier = Modifier.weight(1f),
                        )
                        StatChip(
                            icon  = Icons.Default.Close,
                            label = stringResource(R.string.inventory_filter_unavailable),
                            count = uiState.unavailableCount,
                            tintOk = uiState.unavailableCount == 0,
                            modifier = Modifier.weight(1f),
                        )
                        StatChip(
                            icon  = Icons.Default.Inventory2,
                            label = stringResource(R.string.inventory_filter_total),
                            count = uiState.foods.size,
                            tintOk = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // ── Search ─────────────────────────────────────────────────
                item {
                    SearchBar(
                        query         = uiState.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        placeholder   = stringResource(R.string.inventory_search_placeholder),
                        modifier      = Modifier.fillMaxWidth(),
                    )
                }

                // ── Category filter chips ──────────────────────────────────
                if (uiState.categories.isNotEmpty()) {
                    item {
                        CategoryFilterRow(
                            categories         = uiState.categories,
                            selectedCategoryId = uiState.selectedCategoryId,
                            onCategorySelected = onCategorySelected,
                        )
                    }
                }

                // ── Food list ──────────────────────────────────────────────
                val foods = uiState.filteredFoods
                if (foods.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                stringResource(R.string.inventory_no_items_match),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    // Unavailable items first so they're immediately visible
                    val sorted = foods.sortedBy { it.isAvailable }
                    items(sorted, key = { it.id }) { food ->
                        FoodAvailabilityRow(
                            food              = food,
                            isToggling        = uiState.togglingFoodId == food.id,
                            onToggle          = { onToggleAvailability(food.id) },
                        )
                    }
                }
            }
            } // Box
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    tintOk: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (tintOk) MaterialTheme.colorScheme.primaryContainer
                         else MaterialTheme.colorScheme.errorContainer
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (tintOk) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        item {
            FilterChip(
                selected  = selectedCategoryId == null,
                onClick   = { onCategorySelected(null) },
                label     = { Text(stringResource(R.string.inventory_filter_all)) },
            )
        }
        items(categories, key = { it.id }) { category ->
            FilterChip(
                selected  = selectedCategoryId == category.id,
                onClick   = { onCategorySelected(category.id) },
                label     = { Text(category.name) },
            )
        }
    }
}

@Composable
private fun FoodAvailabilityRow(
    food: Food,
    isToggling: Boolean,
    onToggle: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Availability indicator dot
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (food.isAvailable)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        if (food.isAvailable) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (food.isAvailable) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${"%.2f".format(food.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    food.categoryId?.let {
                        Text(
                            " · ${food.categoryName ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            if (isToggling) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Switch(
                    checked         = food.isAvailable,
                    onCheckedChange = { onToggle() },
                )
            }
        }
    }
}
