package com.autobill.smartpos.feature.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.domain.model.ConnectionState
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderItem
import com.autobill.smartpos.feature.order.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenDisplayScreen(
    uiState: KitchenDisplayUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFilterSelect: (KitchenDisplayFilter) -> Unit,
    onUpdateItemStatus: (orderId: Long, itemId: Long, newStatus: ItemStatus) -> Unit,
    onSuccessMessageConsumed: () -> Unit,
    onErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // ── One-shot snackbars ────────────────────────────────────────────────────
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccessMessageConsumed()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorConsumed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F3F4)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Bar ───────────────────────────────────────────────────
            KitchenTopBar(
                connectionState = uiState.connectionState,
                onBack    = onBack,
                onRefresh = onRefresh,
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // ── Filter chips ──────────────────────────────────────────────
            KitchenFilterRow(
                selectedFilter = uiState.selectedFilter,
                orders         = uiState.orders,
                onFilterSelect = onFilterSelect,
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // ── Main content ──────────────────────────────────────────────
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading -> KitchenLoadingState()
                    uiState.errorMessage != null && uiState.orders.isEmpty() ->
                        KitchenErrorState(
                            message = uiState.errorMessage,
                            onRetry = onRefresh,
                        )
                    uiState.filteredOrders.isEmpty() ->
                        KitchenEmptyState(filter = uiState.selectedFilter)
                    else ->
                        KitchenOrderGrid(
                            orders          = uiState.filteredOrders,
                            updatingItemIds = uiState.updatingItemIds,
                            onUpdateStatus  = onUpdateItemStatus,
                        )
                }
            }
        }

        // ── Snackbar ──────────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) { data ->
            Snackbar(
                snackbarData       = data,
                containerColor     = Color(0xFF323232),
                contentColor       = Color.White,
                actionColor        = Color(0xFFE33E3E),
                shape              = RoundedCornerShape(8.dp),
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun KitchenTopBar(
    connectionState: ConnectionState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color.White)
            .padding(horizontal = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint               = Color(0xFF212121),
            )
        }

        // Kitchen icon emoji + title
        Text(
            text       = stringResource(R.string.kitchen_title),
            style      = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121),
            modifier   = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )

        // ── ⚡ / ⚠ connection state badge ─────────────────────────────────
        KitchenConnectionBadge(state = connectionState)

        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector        = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.cd_refresh),
                tint               = Color(0xFF757575),
            )
        }
    }
}

// ── Connection State Badge ────────────────────────────────────────────────────

/**
 * Compact chip that shows ⚡ LIVE (green) when the WebSocket is CONNECTED,
 * or ⚠ Reconnecting… (amber) when DISCONNECTED / RECONNECTING / CONNECTING.
 * Fades smoothly between the two states.
 */
@Composable
private fun KitchenConnectionBadge(state: ConnectionState) {
    val isLive = state == ConnectionState.CONNECTED
    AnimatedVisibility(
        visible = true,
        enter   = fadeIn(),
        exit    = fadeOut(),
    ) {
        val bgColor   = if (isLive) Color(0xFF1B5E20) else Color(0xFFE65100)
        val textColor = Color.White
        val label     = if (isLive) stringResource(R.string.kitchen_badge_live) else stringResource(R.string.kitchen_badge_reconnecting)

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(bgColor)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ── Filter Row ────────────────────────────────────────────────────────────────

@Composable
private fun KitchenFilterRow(
    selectedFilter: KitchenDisplayFilter,
    orders: List<Order>,
    onFilterSelect: (KitchenDisplayFilter) -> Unit,
) {
    // Compute badge counts per filter tab
    val pendingCount     = orders.sumOf { o -> o.items.count { it.itemStatus == ItemStatus.PENDING } }
    val inProgressCount  = orders.sumOf { o -> o.items.count { it.itemStatus == ItemStatus.IN_PROGRESS } }
    val readyCount       = orders.sumOf { o -> o.items.count { it.itemStatus == ItemStatus.READY } }

    LazyRow(
        modifier            = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(KitchenDisplayFilter.entries) { filter ->
            val count = when (filter) {
                KitchenDisplayFilter.ALL_ACTIVE  -> orders.size
                KitchenDisplayFilter.PENDING     -> pendingCount
                KitchenDisplayFilter.IN_PROGRESS -> inProgressCount
                KitchenDisplayFilter.READY       -> readyCount
            }
            FilterChip(
                selected = filter == selectedFilter,
                onClick  = { onFilterSelect(filter) },
                label    = {
                    val filterLabel = when (filter) {
                        KitchenDisplayFilter.ALL_ACTIVE  -> stringResource(R.string.kitchen_filter_all_active)
                        KitchenDisplayFilter.PENDING     -> stringResource(R.string.kitchen_filter_pending)
                        KitchenDisplayFilter.IN_PROGRESS -> stringResource(R.string.kitchen_filter_in_progress)
                        KitchenDisplayFilter.READY       -> stringResource(R.string.kitchen_filter_ready)
                    }
                    Text(
                        text       = if (count > 0) "$filterLabel  $count" else filterLabel,
                        fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor  = Color(0xFFE33E3E),
                    selectedLabelColor      = Color.White,
                ),
            )
        }
    }
}

// ── Order Grid ────────────────────────────────────────────────────────────────

@Composable
private fun KitchenOrderGrid(
    orders: List<Order>,
    updatingItemIds: Set<Long>,
    onUpdateStatus: (orderId: Long, itemId: Long, newStatus: ItemStatus) -> Unit,
) {
    LazyVerticalGrid(
        columns             = GridCells.Adaptive(minSize = 320.dp),
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement   = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier            = Modifier.fillMaxSize(),
    ) {
        items(orders, key = { it.id }) { order ->
            KitchenOrderCard(
                order           = order,
                updatingItemIds = updatingItemIds,
                onUpdateStatus  = { itemId, newStatus -> onUpdateStatus(order.id, itemId, newStatus) },
            )
        }
    }
}

// ── Order Card ────────────────────────────────────────────────────────────────

@Composable
private fun KitchenOrderCard(
    order: Order,
    updatingItemIds: Set<Long>,
    onUpdateStatus: (itemId: Long, newStatus: ItemStatus) -> Unit,
) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column {

            // ── Card Header ───────────────────────────────────────────────
            KitchenOrderCardHeader(order = order)

            HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp)

            // ── Items ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                order.items.forEach { item ->
                    KitchenItemRow(
                        item           = item,
                        isUpdating     = item.id in updatingItemIds,
                        onUpdateStatus = { newStatus -> onUpdateStatus(item.id, newStatus) },
                    )
                }
            }

            // ── Card Footer — elapsed time ────────────────────────────────
            KitchenOrderCardFooter(createdAt = order.createdAt)
        }
    }
}

@Composable
private fun KitchenOrderCardHeader(order: Order) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text       = order.orderNumber,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF212121),
            )
            Text(
                text  = stringResource(R.string.kitchen_table_label, order.tableNumber),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF757575),
            )
        }

        // Order type badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (order.orderType.value) {
                "TAKEAWAY"  -> Color(0xFFFFF3E0)
                "DELIVERY"  -> Color(0xFFE8F5E9)
                else        -> Color(0xFFE3F2FD)  // DINE_IN
            },
        ) {
            Text(
                text     = order.orderType.value.replace("_", " "),
                style    = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color    = when (order.orderType.value) {
                    "TAKEAWAY"  -> Color(0xFFE65100)
                    "DELIVERY"  -> Color(0xFF2E7D32)
                    else        -> Color(0xFF1565C0)
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun KitchenOrderCardFooter(createdAt: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text  = "🕐  ${formatOrderTime(createdAt)}",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E9E9E),
        )
    }
}

// ── Item Row ──────────────────────────────────────────────────────────────────

@Composable
private fun KitchenItemRow(
    item: OrderItem,
    isUpdating: Boolean,
    onUpdateStatus: (newStatus: ItemStatus) -> Unit,
) {
    val isLocked    = item.itemStatus.isLocked()
    val bgColor     = Color(item.itemStatus.containerColor())
    val accentColor = Color(item.itemStatus.accentColor())
    val nextStatus  = item.itemStatus.kdsNextStatus()
    val canCancel   = item.itemStatus.kdsCancelable()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .alpha(if (isLocked) 0.65f else 1f)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        // ── Row 1: food name + qty + status chip ──────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.foodName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color(0xFF212121),
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text  = "× ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161),
                )
            }

            // Status chip
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = accentColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text       = item.itemStatus.value.replace("_", " "),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor,
                    modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                )
            }
        }

        // Special requests (if any)
        if (!item.specialRequests.isNullOrBlank()) {
            Text(
                text      = "📝 ${item.specialRequests}",
                style     = MaterialTheme.typography.bodySmall,
                color     = Color(0xFF757575),
                fontStyle = FontStyle.Italic,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis,
                modifier  = Modifier.padding(top = 2.dp),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // ── Row 2: action buttons ─────────────────────────────────────────
        when {
            isUpdating -> {
                // Spinner while PATCH is in-flight
                Box(
                    modifier          = Modifier.fillMaxWidth(),
                    contentAlignment  = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color       = accentColor,
                    )
                }
            }

            isLocked -> {
                // Locked — show lock icon, no buttons
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Icon(
                        imageVector        = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.cd_locked),
                        tint               = Color(0xFF9E9E9E),
                        modifier           = Modifier.size(16.dp),
                    )
                }
            }

            else -> {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    // Cancel button (PENDING / IN_PROGRESS only)
                    if (canCancel) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(ItemStatus.CANCELLED) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFB00020),
                            ),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text  = stringResource(R.string.kitchen_cancel_button),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Primary forward-progression button
                    if (nextStatus != null) {
                        Button(
                            onClick = { onUpdateStatus(nextStatus) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor   = Color.White,
                            ),
                            shape = RoundedCornerShape(6.dp),
                        ) {
                            Text(
                                text  = when (item.itemStatus) {
                                    ItemStatus.PENDING     -> stringResource(R.string.kitchen_action_start)
                                    ItemStatus.IN_PROGRESS -> stringResource(R.string.kitchen_action_ready)
                                    ItemStatus.READY       -> stringResource(R.string.kitchen_action_served)
                                    else                   -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Loading / Error / Empty States ───────────────────────────────────────────

@Composable
private fun KitchenLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier    = Modifier.size(48.dp),
                color       = Color(0xFFE33E3E),
                strokeWidth = 3.dp,
            )
            Text(
                text  = stringResource(R.string.kitchen_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
            )
        }
    }
}

@Composable
private fun KitchenErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("⚠️", style = MaterialTheme.typography.displaySmall)
            Text(
                text       = message,
                style      = MaterialTheme.typography.bodyMedium,
                color      = Color(0xFFB00020),
                fontWeight = FontWeight.Medium,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry), color = Color(0xFFE33E3E), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KitchenEmptyState(filter: KitchenDisplayFilter) {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("✅", style = MaterialTheme.typography.displaySmall)
            Text(
                text       = when (filter) {
                    KitchenDisplayFilter.ALL_ACTIVE  -> stringResource(R.string.kitchen_empty_all_active)
                    KitchenDisplayFilter.PENDING     -> stringResource(R.string.kitchen_empty_pending)
                    KitchenDisplayFilter.IN_PROGRESS -> stringResource(R.string.kitchen_empty_in_progress)
                    KitchenDisplayFilter.READY       -> stringResource(R.string.kitchen_empty_ready)
                },
                style      = MaterialTheme.typography.titleMedium,
                color      = Color(0xFF424242),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text  = stringResource(R.string.kitchen_pull_to_refresh),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
            )
        }
    }
}
