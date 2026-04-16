package com.autobill.smartpos.feature.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.model.OrderItem
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.feature.order.R
import com.autobill.smartpos.ui.components.badges.OrderStatusBadge

/**
 * Order Detail Screen — Phase 5.3.
 *
 * Features:
 *  - Full order display: items, subtotal, notes
 *  - Status transition chip bar (→ IN_PROGRESS, → COMPLETED, etc.)
 *  - Add item dialog with food search + quantity stepper
 *  - Edit item dialog (qty + special requests) — locked items shown read-only
 *  - Per-row remove with spinner while in-flight
 *  - Cancel Order button (role-gated, non-final orders only)
 *  - 409 conflict feedback via snackbar
 *  - Pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onStatusUpdate: (OrderStatus) -> Unit,
    // Billing
    onBillingClick: () -> Unit,
    // Add Item
    onAddItemClick: () -> Unit,
    onDismissAddItemDialog: () -> Unit,
    onAddItemFoodSearch: (String) -> Unit,
    onAddItemFoodSelected: (Food) -> Unit,
    onAddItemQuantityChange: (Int) -> Unit,
    onAddItemSpecialRequestsChange: (String) -> Unit,
    onConfirmAddItem: () -> Unit,
    // Edit Item
    onEditItemClick: (OrderItem) -> Unit,
    onDismissEditItemDialog: () -> Unit,
    onEditItemQuantityChange: (Int) -> Unit,
    onEditItemSpecialRequestsChange: (String) -> Unit,
    onConfirmEditItem: () -> Unit,
    // Remove / Cancel
    onRemoveItem: (Long) -> Unit,
    onShowCancelDialog: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancelOrder: () -> Unit,
    // One-shot events
    onSuccessMessageConsumed: () -> Unit,
    onConflictMessageConsumed: () -> Unit,
    onErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccessMessageConsumed()
        }
    }
    LaunchedEffect(uiState.conflictMessage) {
        uiState.conflictMessage?.let {
            snackbarHostState.showSnackbar(it)
            onConflictMessageConsumed()
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorConsumed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
        ) {
            OrderDetailTopBar(
                order            = uiState.order,
                isLoading        = uiState.isLoading,
                isUpdatingStatus = uiState.isUpdatingStatus,
                onBack           = onBack,
                onRefresh        = onRefresh,
            )
            HorizontalDivider(color = Color(0xFFE0E0E0))

            if (uiState.allowedStatusTransitions.isNotEmpty()) {
                StatusTransitionRow(
                    transitions      = uiState.allowedStatusTransitions,
                    isUpdating       = uiState.isUpdatingStatus,
                    onStatusSelected = onStatusUpdate,
                )
                HorizontalDivider(color = Color(0xFFE0E0E0))
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh    = onRefresh,
                modifier     = Modifier.weight(1f).fillMaxWidth(),
            ) {
                when {
                    uiState.isLoading      -> OrderDetailLoadingState()
                    uiState.order == null  -> OrderDetailErrorState(
                        message = uiState.errorMessage ?: stringResource(R.string.order_detail_error_message),
                        onRetry = onRefresh,
                    )
                    else -> OrderDetailContent(
                        uiState       = uiState,
                        onAddItem     = onAddItemClick,
                        onEditItem    = onEditItemClick,
                        onRemoveItem  = onRemoveItem,
                        onCancelOrder = onShowCancelDialog,
                        onBillingClick = onBillingClick,
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        ) { data ->
            Snackbar(
                snackbarData   = data,
                containerColor = Color(0xFF323232),
                contentColor   = Color.White,
                shape          = RoundedCornerShape(12.dp),
            )
        }
    }

    uiState.addItemDialog?.let { dialog ->
        AddItemDialog(
            dialog                  = dialog,
            isAdding                = uiState.isAddingItem,
            errorMessage            = uiState.addItemError,
            onFoodSearch            = onAddItemFoodSearch,
            onFoodSelected          = onAddItemFoodSelected,
            onQuantityChange        = onAddItemQuantityChange,
            onSpecialRequestsChange = onAddItemSpecialRequestsChange,
            onConfirm               = onConfirmAddItem,
            onDismiss               = onDismissAddItemDialog,
        )
    }

    uiState.editItemDialog?.let { dialog ->
        EditItemDialog(
            dialog                  = dialog,
            isEditing               = uiState.isEditingItem,
            errorMessage            = uiState.editItemError,
            onQuantityChange        = onEditItemQuantityChange,
            onSpecialRequestsChange = onEditItemSpecialRequestsChange,
            onConfirm               = onConfirmEditItem,
            onDismiss               = onDismissEditItemDialog,
        )
    }

    if (uiState.showCancelDialog) {
        CancelOrderDialog(
            orderNumber  = uiState.order?.orderNumber ?: "",
            isCancelling = uiState.isCancelling,
            onConfirm    = onConfirmCancelOrder,
            onDismiss    = onDismissCancelDialog,
        )
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun OrderDetailTopBar(
    order: com.autobill.smartpos.domain.model.Order?,
    isLoading: Boolean,
    isUpdatingStatus: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = Color(0xFF212121))
            }
            Text(
                text       = order?.orderNumber ?: stringResource(R.string.order_detail_default_title),
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFF212121),
                modifier   = Modifier.weight(1f).padding(start = 4.dp),
            )
            if (isUpdatingStatus) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = Color(0xFFE33E3E),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            order?.let { OrderStatusBadge(status = it.status.value) }
            IconButton(onClick = onRefresh, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, stringResource(R.string.cd_refresh), tint = Color(0xFF757575))
            }
        }
        if (order != null) {
            Row(
                modifier = Modifier.padding(start = 52.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.order_detail_table, order.tableNumber), style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242), fontWeight = FontWeight.SemiBold)
                Text("·", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                Text(order.orderType.value.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
                Text("·", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                Text(formatOrderTime(order.createdAt),
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
            }
        }
    }
}

// ── Status Transition Chips ───────────────────────────────────────────────────

@Composable
private fun StatusTransitionRow(
    transitions: List<OrderStatus>,
    isUpdating: Boolean,
    onStatusSelected: (OrderStatus) -> Unit,
) {
    LazyRow(
        modifier              = Modifier.fillMaxWidth().background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(transitions) { status ->
            val accent = Color(status.accentColor())
            Surface(
                onClick  = { if (!isUpdating) onStatusSelected(status) },
                shape    = RoundedCornerShape(20.dp),
                color    = Color(status.containerColor()),
                modifier = Modifier
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .alpha(if (isUpdating) 0.5f else 1f),
            ) {
                Text(
                    text       = "→ ${status.value.replace("_", " ")}",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = accent,
                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun OrderDetailContent(
    uiState: OrderDetailUiState,
    onAddItem: () -> Unit,
    onEditItem: (OrderItem) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onCancelOrder: () -> Unit,
    onBillingClick: () -> Unit,
) {
    val order = uiState.order ?: return
    LazyColumn(
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier            = Modifier.fillMaxSize(),
    ) {
        item { OrderSummaryCard(order = order) }

        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.order_detail_items_section, order.items.size), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                if (uiState.canAddItems) {
                    OutlinedButton(
                        onClick        = onAddItem,
                        shape          = RoundedCornerShape(8.dp),
                        colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE33E3E)),
                        border         = BorderStroke(1.dp, Color(0xFFE33E3E)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.order_detail_add_item_button), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        if (order.items.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.order_detail_no_items), style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E))
                }
            }
        } else {
            items(order.items, key = { it.id }) { item ->
                val locked   = item.itemStatus.isLocked()
                val removing = item.id in uiState.removingItemIds
                OrderDetailItemRow(
                    item       = item,
                    isLocked   = locked,
                    isRemoving = removing,
                    onEdit     = { if (!locked && !removing) onEditItem(item) },
                    onRemove   = { if (!locked && !removing) onRemoveItem(item.id) },
                )
            }
        }

        if (uiState.canBill) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick  = onBillingClick,
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.order_detail_generate_bill), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (uiState.canCancelOrders && !uiState.isOrderFinal) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick  = onCancelOrder,
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020)),
                    border   = BorderStroke(1.dp, Color(0xFFB00020)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.order_detail_cancel_order), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ── Order Summary Card ────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryCard(order: com.autobill.smartpos.domain.model.Order) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.order_detail_subtotal), style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
            Text("₹%.2f".format(order.subtotal), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium)
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.order_detail_total), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("₹%.2f".format(order.totalAmount), style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        }
        val notes = order.notes
        if (!notes.isNullOrBlank()) {
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(stringResource(R.string.order_detail_note_label), style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                Text(notes, style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF424242), fontStyle = FontStyle.Italic)
            }
        }
    }
}

// ── Order Item Row ────────────────────────────────────────────────────────────

@Composable
private fun OrderDetailItemRow(
    item: OrderItem,
    isLocked: Boolean,
    isRemoving: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    val bgColor     = Color(item.itemStatus.containerColor())
    val accentColor = Color(item.itemStatus.accentColor())

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(bgColor).alpha(if (isLocked) 0.7f else 1f)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.foodName, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF212121),
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            val sr = item.specialRequests
            if (!sr.isNullOrBlank()) {
                Text(sr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575),
                    fontStyle = FontStyle.Italic, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Surface(shape = RoundedCornerShape(6.dp), color = accentColor.copy(alpha = 0.12f)) {
            Text(
                text       = item.itemStatus.value.replace("_", " "),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color      = accentColor,
                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${item.quantity} × ₹%.0f".format(item.unitPrice),
                style = MaterialTheme.typography.bodySmall, color = Color(0xFF757575))
            Text("₹%.2f".format(item.subtotal), style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
        }
        when {
            isRemoving -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFFE33E3E))
            isLocked   -> Icon(Icons.Default.Lock, stringResource(R.string.cd_locked),
                tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp))
            else       -> {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, stringResource(R.string.cd_edit), tint = Color(0xFF1565C0),
                        modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, stringResource(R.string.cd_remove), tint = Color(0xFFB00020),
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Add Item Dialog ────────────────────────────────────────────────────────────

@Composable
private fun AddItemDialog(
    dialog: AddItemDialogState,
    isAdding: Boolean,
    errorMessage: String?,
    onFoodSearch: (String) -> Unit,
    onFoodSelected: (Food) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onSpecialRequestsChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!isAdding) onDismiss() }) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
            modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.order_add_item_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value           = dialog.searchQuery,
                    onValueChange   = onFoodSearch,
                    modifier        = Modifier.fillMaxWidth(),
                    placeholder     = { Text(stringResource(R.string.order_add_item_search_placeholder)) },
                    leadingIcon     = { Icon(Icons.Default.Search, null, tint = Color(0xFF9E9E9E)) },
                    trailingIcon    = {
                        if (dialog.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onFoodSearch("") }) {
                                Icon(Icons.Default.Clear, stringResource(R.string.cd_clear), tint = Color(0xFF9E9E9E))
                            }
                        }
                    },
                    singleLine      = true,
                    shape           = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color(0xFFE33E3E),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                    ),
                )

                if (dialog.isSearching) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp, color = Color(0xFFE33E3E))
                    }
                } else if (dialog.foodResults.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5))) {
                        dialog.foodResults.take(5).forEachIndexed { index, food ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (dialog.selectedFood?.id == food.id) Color(0xFFFFEBEE)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(food.name, style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f))
                                TextButton(onClick = { onFoodSelected(food) }) {
                                    Text("₹%.0f".format(food.price), color = Color(0xFFE33E3E),
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (index < dialog.foodResults.size - 1) {
                                HorizontalDivider(color = Color(0xFFEEEEEE))
                            }
                        }
                    }
                }

                if (dialog.selectedFood != null) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(dialog.selectedFood.name, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f),
                            overflow = TextOverflow.Ellipsis, maxLines = 1)
                        QuantityStepper(quantity = dialog.quantity, onQuantityChange = onQuantityChange)
                    }
                    OutlinedTextField(
                        value         = dialog.specialRequests,
                        onValueChange = onSpecialRequestsChange,
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text(stringResource(R.string.order_add_item_note_placeholder)) },
                        maxLines      = 2,
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Color(0xFFE33E3E),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                        ),
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB00020))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onDismiss, enabled = !isAdding) {
                        Text(stringResource(R.string.cancel), color = Color(0xFF757575))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isAdding) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp, color = Color(0xFFE33E3E))
                    } else {
                        TextButton(onClick = onConfirm, enabled = dialog.selectedFood != null) {
                            Text(stringResource(R.string.order_add_item_confirm),
                                color = if (dialog.selectedFood != null) Color(0xFFE33E3E) else Color(0xFFBDBDBD),
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ── Edit Item Dialog ───────────────────────────────────────────────────────────

@Composable
private fun EditItemDialog(
    dialog: EditItemDialogState,
    isEditing: Boolean,
    errorMessage: String?,
    onQuantityChange: (Int) -> Unit,
    onSpecialRequestsChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isEditing) onDismiss() },
        title       = { Text(stringResource(R.string.order_edit_item_title), fontWeight = FontWeight.Bold) },
        text        = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(dialog.item.foodName, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.order_edit_item_quantity_label), style = MaterialTheme.typography.bodyMedium)
                    QuantityStepper(quantity = dialog.quantity, onQuantityChange = onQuantityChange)
                }
                OutlinedTextField(
                    value         = dialog.specialRequests,
                    onValueChange = onSpecialRequestsChange,
                    modifier      = Modifier.fillMaxWidth(),
                    label         = { Text(stringResource(R.string.order_edit_item_note_label)) },
                    placeholder   = { Text(stringResource(R.string.order_edit_item_note_placeholder)) },
                    maxLines      = 2,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Color(0xFFE33E3E),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                    ),
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = Color(0xFFB00020))
                }
            }
        },
        confirmButton = {
            if (isEditing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp, color = Color(0xFFE33E3E))
            } else {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.order_edit_item_save), color = Color(0xFFE33E3E), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isEditing) {
                Text(stringResource(R.string.cancel), color = Color(0xFF757575))
            }
        },
        containerColor = Color.White,
        shape          = RoundedCornerShape(16.dp),
    )
}

// ── Cancel Confirm Dialog ──────────────────────────────────────────────────────

@Composable
private fun CancelOrderDialog(
    orderNumber: String,
    isCancelling: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isCancelling) onDismiss() },
        title       = { Text(stringResource(R.string.order_cancel_dialog_title), fontWeight = FontWeight.Bold) },
        text        = {
            Text(
                text  = stringResource(R.string.order_cancel_dialog_message, orderNumber),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            if (isCancelling) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp, color = Color(0xFFB00020))
            } else {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.order_cancel_dialog_confirm), color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCancelling) {
                Text(stringResource(R.string.order_cancel_dialog_dismiss), color = Color(0xFF757575))
            }
        },
        containerColor = Color.White,
        shape          = RoundedCornerShape(16.dp),
    )
}

// ── Quantity Stepper ───────────────────────────────────────────────────────────

@Composable
private fun QuantityStepper(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
            onClick  = { onQuantityChange(-1) },
            enabled  = quantity > 1,
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(if (quantity > 1) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)),
        ) {
            Text("−", style = MaterialTheme.typography.titleMedium,
                color = if (quantity > 1) Color(0xFFE33E3E) else Color(0xFFBDBDBD))
        }
        Text("$quantity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
        IconButton(
            onClick  = { onQuantityChange(1) },
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFFEBEE)),
        ) {
            Text("+", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE33E3E))
        }
    }
}

// ── Loading / Error states ─────────────────────────────────────────────────────

@Composable
private fun OrderDetailLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp),
            color = Color(0xFFE33E3E), strokeWidth = 3.dp)
    }
}

@Composable
private fun OrderDetailErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("⚠️", style = MaterialTheme.typography.displaySmall)
            Text(message, style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB00020), fontWeight = FontWeight.Medium)
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry), color = Color(0xFFE33E3E)) }
        }
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
// formatOrderTime() is defined as internal in OrderItemCard.kt (same module)

