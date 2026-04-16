package com.autobill.smartpos.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderStatus

/**
 * Order History Screen — Phase 8.2
 *
 * Shows a filterable list of orders for a selected date range.
 * Filters: All | Delivered | Cancelled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    uiState: OrderHistoryUiState,
    onShowStartPicker: () -> Unit,
    onShowEndPicker: () -> Unit,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit,
    onDismissStartPicker: () -> Unit,
    onDismissEndPicker: () -> Unit,
    onLoadOrders: () -> Unit,
    onFilterSelected: (OrderHistoryFilter) -> Unit,
    onRefresh: () -> Unit,
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

    // ── Start date picker ─────────────────────────────────────────────────────
    if (uiState.showStartPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.startDateMs)
        DatePickerDialog(
            onDismissRequest = onDismissStartPicker,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onStartDateSelected) ?: onDismissStartPicker()
                }) { Text(stringResource(R.string.date_picker_ok)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissStartPicker) { Text(stringResource(R.string.date_picker_cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    // ── End date picker ───────────────────────────────────────────────────────
    if (uiState.showEndPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = uiState.endDateMs)
        DatePickerDialog(
            onDismissRequest = onDismissEndPicker,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let(onEndDateSelected) ?: onDismissEndPicker()
                }) { Text(stringResource(R.string.date_picker_ok)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissEndPicker) { Text(stringResource(R.string.date_picker_cancel)) }
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
                        imageVector        = Icons.Default.History,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(26.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text       = stringResource(R.string.order_history_title),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onRefresh, enabled = !uiState.isLoading && !uiState.isRefreshing) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh))
                }
            }

            HorizontalDivider()

            // ── Date selector + filter chips (outside pull-to-refresh) ────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Date range row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
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
                        onClick  = onShowStartPicker,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(8.dp),
                    ) {
                        Text(uiState.startDateMs.toDisplayDate(), style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("→", color = Color(0xFF757575))
                    OutlinedButton(
                        onClick  = onShowEndPicker,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(8.dp),
                    ) {
                        Text(uiState.endDateMs.toDisplayDate(), style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = onLoadOrders,
                        enabled = !uiState.isLoading && !uiState.isRefreshing,
                        shape   = RoundedCornerShape(8.dp),
                    ) { Text(stringResource(R.string.order_history_load_button)) }
                }

                // Status filter chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OrderHistoryFilter.entries) { filter ->
                        FilterChip(
                            selected = uiState.selectedFilter == filter,
                            onClick  = { onFilterSelected(filter) },
                            label    = {
                                val filterLabel = when (filter) {
                                    OrderHistoryFilter.ALL       -> stringResource(R.string.history_filter_all)
                                    OrderHistoryFilter.DELIVERED -> stringResource(R.string.history_filter_delivered)
                                    OrderHistoryFilter.CANCELLED -> stringResource(R.string.history_filter_cancelled)
                                }
                                Text(filterLabel)
                            },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }

            HorizontalDivider()

            // ── List ──────────────────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.isLoading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }

                    uiState.orders.isEmpty() -> Box(
                        Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text      = stringResource(R.string.order_history_empty),
                            style     = MaterialTheme.typography.bodyLarge,
                            color     = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                        )
                    }

                    else -> LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(uiState.orders, key = { it.id }) { order ->
                            OrderHistoryCard(order = order)
                        }
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

// ── Order History Card ────────────────────────────────────────────────────────

/**
 * Card showing order summary — order number, table, item count, total, date, status.
 *
 * Layout:
 * ┌───────────────────────────────────────────────────────────┐
 * │  ORD-001 · Table T-3             [DELIVERED badge]        │
 * │  3 items · DINE_IN               13 Apr, 10:32 AM        │
 * │  ─────────────────────────────────────────────────────── │
 * │  Subtotal: ₹423.73   Tax (18%): ₹76.27   Total: ₹500.00 │
 * └───────────────────────────────────────────────────────────┘
 */
@Composable
private fun OrderHistoryCard(
    order: Order,
    modifier: Modifier = Modifier,
) {
    val (containerColor, accentColor) = when (order.status) {
        OrderStatus.DELIVERED   -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.CANCELLED   -> Color(0xFFFFEBEE) to Color(0xFFB71C1C)
        OrderStatus.PENDING     -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        OrderStatus.IN_PROGRESS -> Color(0xFFFFF8E1) to Color(0xFFE65100)
        OrderStatus.COMPLETED   -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        OrderStatus.HOLD        -> Color(0xFFFBE9E7) to Color(0xFFBF360C)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Row 1: order number + status badge
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text       = order.orderNumber,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF212121),
                )
                Text(
                    text  = stringResource(R.string.order_history_card_summary, order.tableNumber, order.items.size, order.orderType.value.replace("_", " ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161),
                )
            }
            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text       = order.status.value.replace("_", " "),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = accentColor,
                )
            }
        }

        HorizontalDivider(color = accentColor.copy(alpha = 0.15f))

        // Row 2: financial summary
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text  = stringResource(R.string.order_history_subtotal, order.subtotal),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161),
                )
                val tax = order.totalAmount - order.subtotal
                if (tax > 0.01) {
                    Text(
                        text  = stringResource(R.string.order_history_tax, tax),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF616161),
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text       = "₹%.2f".format(order.totalAmount),
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor,
                )
                Text(
                    text  = historyFormatDate(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                )
            }
        }
    }
}

/** Formats ISO-8601 to "13 Apr, 10:32 AM" — same logic as OrderItemCard in :feature:order. */
private fun historyFormatDate(createdAt: String): String {
    return try {
        val parts = createdAt.split("T")
        if (parts.size < 2) return createdAt
        val dateSections = parts[0].split("-")
        if (dateSections.size < 3) return createdAt
        val day   = dateSections[2].toIntOrNull() ?: return createdAt
        val month = arrayOf("","Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
                            .getOrNull(dateSections[1].toIntOrNull() ?: 0) ?: return createdAt
        val timePart  = parts[1].take(5)
        val timeHour  = timePart.split(":")[0].toIntOrNull() ?: return createdAt
        val timeMin   = timePart.split(":").getOrNull(1) ?: "00"
        val amPm      = if (timeHour < 12) "AM" else "PM"
        val hour12    = when { timeHour == 0 -> 12; timeHour > 12 -> timeHour - 12; else -> timeHour }
        "$day $month, $hour12:$timeMin $amPm"
    } catch (_: Exception) { createdAt }
}

