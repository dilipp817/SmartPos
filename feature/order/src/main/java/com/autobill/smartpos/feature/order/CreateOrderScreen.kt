package com.autobill.smartpos.feature.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.CartItem
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.model.Table
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.feature.order.R

/**
 * Create Order confirmation screen.
 *
 * Layout (landscape tablet):
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  ← Back          New Order                                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  ┌──────────────────────┐  ┌─────────────────────────────────┐  │
 * │  │ 🪑 Table T-3         │  │ Order Items (3 items)           │  │
 * │  │ Floor 1 · 4 seats   │  │  • Butter Chicken x2   ₹440.00 │  │
 * │  │ AVAILABLE           │  │  • Naan x1             ₹50.00  │  │
 * │  └──────────────────────┘  │                                 │  │
 * │                            │ Notes: _____________________   │  │
 * │  Order Type: [DINE IN]     │                                 │  │
 * │  (read-only badge)         │ Subtotal:            ₹490.00  │  │
 * │                            │ Est. GST (18%):       ₹88.20  │  │
 * │                            │ Est. Total:          ₹578.20  │  │
 * │                            │    [  Place Order  ]           │  │
 * │                            └─────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * Order type is **always read-only** on this screen — it was chosen on the food
 * selection screen and cannot be changed here without invalidating the table
 * reservation or losing the no-table contract. [NoTableOrderTypeBadge] is used
 * for both DINE_IN (table selected) and TAKEAWAY (no table) cases.
 */
@Composable
fun CreateOrderScreen(
    uiState: CreateOrderUiState,
    onNotesChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit,
    onReselectTable: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
    ) {
        // ── Header ───────────────────────────────────────────────────────────
        OrderHeader(onBack = onBack)
        HorizontalDivider(color = Color(0xFFE0E0E0))

        // ── Phase 9.2: Offline banner ─────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.isOffline,
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
                    text       = stringResource(R.string.create_order_offline_banner),
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // ── Body ─────────────────────────────────────────────────────────────
        when {
            uiState.isTableLoading -> CenteredLoading(stringResource(R.string.create_order_loading_table))
            uiState.tableConflict  -> TableConflictBanner(onReselectTable = onReselectTable)
            else -> OrderBody(
                uiState       = uiState,
                onNotesChange = onNotesChange,
                onPlaceOrder  = onPlaceOrder,
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun OrderHeader(onBack: () -> Unit) {
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
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = Color(0xFF212121),
            )
        }
        Text(
            text = stringResource(R.string.create_order_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

// ── Main body — two-column layout ────────────────────────────────────────────

@Composable
private fun OrderBody(
    uiState: CreateOrderUiState,
    onNotesChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
    Row(
        modifier = Modifier
            .widthIn(max = 1000.dp)
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Left column — table info + order type ─────────────────────────
        Column(
            modifier = Modifier.widthIn(min = 220.dp, max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.table?.let { TableInfoCard(table = it) }
            // Order type was chosen on the food screen and is always read-only here.
            // - isNoTable=true  → TAKEAWAY / counter-service (no table)
            // - isNoTable=false → DINE_IN with a selected table
            // In both cases switching order type here would put the order in an
            // inconsistent state (e.g. switching to DINE_IN without a table, or
            // switching to TAKEAWAY after a table was reserved).
            NoTableOrderTypeBadge(orderType = uiState.orderType)
        }

        // ── Right column — items + notes + totals + submit ────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.create_order_items_section, uiState.cartItems.sumOf { it.quantity }),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )

            // Cart items list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
            ) {
                items(uiState.cartItems, key = { it.foodId }) { item ->
                    CartItemRow(item = item)
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Notes field
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text(stringResource(R.string.create_order_table_notes_label)) },
                placeholder = { Text(stringResource(R.string.create_order_table_notes_placeholder)) },
                singleLine = false,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE33E3E),
                    focusedLabelColor = Color(0xFFE33E3E),
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = "⚠️ ${uiState.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC62828),
                )
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Bill preview
            BillPreviewSection(uiState = uiState)

            // Place Order button
            Button(
                onClick = onPlaceOrder,
                enabled = uiState.canPlaceOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE33E3E),
                    disabledContainerColor = Color(0xFFE0E0E0),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.create_order_place_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
    } // Box (max-width wrapper)
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun TableInfoCard(table: Table) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Default.TableBar,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.create_order_table_number, table.tableNumber),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = stringResource(R.string.create_order_table_floor_capacity, table.floor, table.capacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = table.status.value,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Read-only order type indicator shown when [CreateOrderUiState.isNoTable] is true.
 * The cashier already chose the order type on the food screen — it cannot be changed
 * here because switching to DINE_IN without a table would produce an invalid order.
 */
@Composable
private fun NoTableOrderTypeBadge(orderType: OrderType) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.create_order_type_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFFF3F3))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                text = orderType.value.replace("_", " "),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE33E3E),
            )
        }
    }
}


@Composable
private fun CartItemRow(item: CartItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.foodName,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "×${item.quantity}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF757575),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        Text(
            text = "₹${"%.2f".format(item.subtotal)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
        )
    }
}

@Composable
private fun BillPreviewSection(uiState: CreateOrderUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BillRow(stringResource(R.string.create_order_subtotal), "₹${"%.2f".format(uiState.subtotal)}")
        BillRow(
            stringResource(R.string.create_order_est_gst),
            "₹${"%.2f".format(uiState.estimatedTax)}",
            hint = true,
        )
        BillRow(
            stringResource(R.string.create_order_est_total),
            "₹${"%.2f".format(uiState.estimatedTotal)}",
            bold = true,
        )
        Text(
            text = stringResource(R.string.create_order_gst_hint),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9E9E9E),
        )
    }
}

@Composable
private fun BillRow(label: String, value: String, bold: Boolean = false, hint: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (bold) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.bodySmall,
            color = if (hint) Color(0xFF9E9E9E) else Color(0xFF616161),
        )
        Text(
            text = value,
            style = if (bold) MaterialTheme.typography.titleSmall
                    else MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (hint) Color(0xFF9E9E9E) else Color(0xFF212121),
        )
    }
}

// ── Special states ────────────────────────────────────────────────────────────

@Composable
private fun CenteredLoading(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFFE33E3E))
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF757575))
        }
    }
}

@Composable
private fun TableConflictBanner(onReselectTable: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = stringResource(R.string.create_order_table_conflict_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )
            Text(
                text = stringResource(R.string.create_order_table_conflict_message),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Button(
                onClick = onReselectTable,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE33E3E)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(stringResource(R.string.create_order_reselect_table), fontWeight = FontWeight.Bold)
            }
        }
    }
}

