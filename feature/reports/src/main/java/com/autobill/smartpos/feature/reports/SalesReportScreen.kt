package com.autobill.smartpos.feature.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.SalesReport
import com.autobill.smartpos.domain.model.TopSellingItem

/**
 * Sales Report Screen — Phase 8.1
 *
 * Layout:
 *  ┌─────────────────────────────────────────────────────────┐
 *  │  [BarChart] Sales Report            [Refresh]           │
 *  │  ─────────────────────────────────────────────────────  │
 *  │  Start: 13 Apr 2026 [▼]   End: 13 Apr 2026 [▼]  [Load]│
 *  │  ─────────────────────────────────────────────────────  │
 *  │  ┌─────────────┐ ┌────────────┐ ┌────────────────────┐ │
 *  │  │ ₹12,400     │ │ 24 Orders  │ │ ₹516 Avg           │ │
 *  │  │ Total Rev.  │ │ (18 deliv) │ │ Avg Order Value    │ │
 *  │  └─────────────┘ └────────────┘ └────────────────────┘ │
 *  │  Top Selling Items                                      │
 *  │  #  Name              Qty   Revenue                    │
 *  │  1. Butter Chicken    42    ₹18,480                    │
 *  └─────────────────────────────────────────────────────────┘
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    uiState: SalesReportUiState,
    onShowStartPicker: () -> Unit,
    onShowEndPicker: () -> Unit,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    onDismissStartPicker: () -> Unit,
    onDismissEndPicker: () -> Unit,
    onLoadReport: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onDismissError()
        }
    }

    // ── Start date picker dialog ──────────────────────────────────────────────
    if (uiState.showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.startDateMs)
        DatePickerDialog(
            onDismissRequest = onDismissStartPicker,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onStartDateSelected) ?: onDismissStartPicker()
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = onDismissStartPicker) { Text("Cancel") }
            },
        ) { DatePicker(state = state) }
    }

    // ── End date picker dialog ────────────────────────────────────────────────
    if (uiState.showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.endDateMs)
        DatePickerDialog(
            onDismissRequest = onDismissEndPicker,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onEndDateSelected) ?: onDismissEndPicker()
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = onDismissEndPicker) { Text("Cancel") }
            },
        ) { DatePicker(state = state) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
        ) {
            // ── Top Bar ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector        = Icons.Default.BarChart,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = "Sales Report",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onLoadReport, enabled = !uiState.isLoading) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh report")
                }
            }

            HorizontalDivider()

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                // ── Date range selector ───────────────────────────────────────
                item {
                    DateRangeSelector(
                        startDateMs    = uiState.startDateMs,
                        endDateMs      = uiState.endDateMs,
                        isLoading      = uiState.isLoading,
                        onStartClick   = onShowStartPicker,
                        onEndClick     = onShowEndPicker,
                        onLoadClick    = onLoadReport,
                    )
                }

                // ── Loading / empty ───────────────────────────────────────────
                if (uiState.isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (uiState.report == null) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text      = "Select a date range and tap Load.",
                                style     = MaterialTheme.typography.bodyLarge,
                                color     = Color(0xFF757575),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    val report = uiState.report

                    // ── Stats row ─────────────────────────────────────────────
                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                    SalesMetricCard(
                        icon        = Icons.AutoMirrored.Filled.TrendingUp,
                        iconTint    = Color(0xFF2E7D32),
                        label       = "Total Revenue",
                        value       = "₹%.2f".format(report.totalRevenue),
                        modifier    = Modifier.weight(1f),
                    )
                            SalesMetricCard(
                                icon        = Icons.Default.ShoppingCart,
                                iconTint    = Color(0xFF1565C0),
                                label       = "Orders (${report.deliveredCount} delivered)",
                                value       = "${report.orderCount}",
                                modifier    = Modifier.weight(1f),
                            )
                            SalesMetricCard(
                                icon        = Icons.Default.BarChart,
                                iconTint    = Color(0xFF6A1B9A),
                                label       = "Avg Order Value",
                                value       = "₹%.2f".format(report.averageOrderValue),
                                modifier    = Modifier.weight(1f),
                            )
                        }
                    }

                    // ── Top Selling Items ─────────────────────────────────────
                    item {
                        TopSellingItemsSection(items = report.topSellingItems)
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) { data -> Snackbar(snackbarData = data) }
    }
}

// ── Date Range Selector ───────────────────────────────────────────────────────

@Composable
private fun DateRangeSelector(
    startDateMs: Long,
    endDateMs: Long,
    isLoading: Boolean,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onLoadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector        = Icons.Default.CalendarToday,
            contentDescription = null,
            tint               = Color(0xFF757575),
            modifier           = Modifier.size(18.dp),
        )
        OutlinedButton(
            onClick  = onStartClick,
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(8.dp),
        ) {
            Text(startDateMs.toDisplayDate(), style = MaterialTheme.typography.bodyMedium)
        }
        Text("→", color = Color(0xFF757575))
        OutlinedButton(
            onClick  = onEndClick,
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(8.dp),
        ) {
            Text(endDateMs.toDisplayDate(), style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick  = onLoadClick,
            enabled  = !isLoading,
            shape    = RoundedCornerShape(8.dp),
        ) {
            Text("Load")
        }
    }
}

// ── Sales Metric Card ─────────────────────────────────────────────────────────

@Composable
private fun SalesMetricCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier              = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(24.dp),
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121),
        )
        Text(
            text      = label,
            style     = MaterialTheme.typography.bodySmall,
            color     = Color(0xFF757575),
            maxLines  = 2,
            overflow  = TextOverflow.Ellipsis,
        )
    }
}

// ── Top Selling Items ─────────────────────────────────────────────────────────

@Composable
private fun TopSellingItemsSection(
    items: List<TopSellingItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.primary,
                modifier           = Modifier.size(20.dp),
            )
            Text(
                text       = "Top Selling Items",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (items.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text      = "No items found in this date range.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color(0xFF757575),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
        } else {
            Spacer(Modifier.height(12.dp))

            // Header row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("#", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), modifier = Modifier.width(28.dp))
                Text("Item", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), modifier = Modifier.weight(1f))
                Text("Qty", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                Text("Revenue", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            items.forEachIndexed { idx, item ->
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        text  = "${idx + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.width(28.dp),
                    )
                    Text(
                        text     = item.foodName,
                        style    = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text     = "${item.quantitySold}",
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End,
                    )
                    Text(
                        text      = "₹%.0f".format(item.revenue),
                        style     = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color     = Color(0xFF2E7D32),
                        modifier  = Modifier.width(80.dp),
                        textAlign = TextAlign.End,
                    )
                }
                if (idx < items.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                }
            }
        }
    }
}





