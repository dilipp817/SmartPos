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
import java.util.Locale

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
 * │  Order Type:               │                                 │  │
 * │  [DINE_IN] [TAKEAWAY]      │ Subtotal:            ₹490.00  │  │
 * │  [DELIVERY]                │ Est. GST (18%):       ₹88.20  │  │
 * │                            │ Est. Total:          ₹578.20  │  │
 * │                            │    [  Place Order  ]           │  │
 * │                            └─────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 */
@Composable
fun CreateOrderScreen(
    uiState: CreateOrderUiState,
    onOrderTypeSelect: (OrderType) -> Unit,
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
                    text       = "Offline — order will be queued and sent when connection restores",
                    style      = MaterialTheme.typography.labelMedium,
                    color      = Color(0xFFE65100),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // ── Body ─────────────────────────────────────────────────────────────
        when {
            uiState.isTableLoading -> CenteredLoading("Loading table details…")
            uiState.tableConflict  -> TableConflictBanner(onReselectTable = onReselectTable)
            else -> OrderBody(
                uiState           = uiState,
                onOrderTypeSelect = onOrderTypeSelect,
                onNotesChange     = onNotesChange,
                onPlaceOrder      = onPlaceOrder,
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
                contentDescription = "Back",
                tint = Color(0xFF212121),
            )
        }
        Text(
            text = "New Order",
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
    onOrderTypeSelect: (OrderType) -> Unit,
    onNotesChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Left column — table info + order type ─────────────────────────
        Column(
            modifier = Modifier.width(280.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            uiState.table?.let { TableInfoCard(table = it) }
            OrderTypeSelector(
                selected = uiState.orderType,
                onSelect = onOrderTypeSelect,
            )
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
                text = "Order Items (${uiState.cartItems.sumOf { it.quantity }} items)",
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
                label = { Text("Table Notes (optional)") },
                placeholder = { Text("e.g. Birthday table, no nuts") },
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
                        text = "Place Order",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
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
                    text = "Table ${table.tableNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = "Floor ${table.floor}  ·  👥 ${table.capacity} seats",
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

@Composable
private fun OrderTypeSelector(
    selected: OrderType,
    onSelect: (OrderType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Order Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF424242),
        )
        OrderType.entries.forEach { type ->
            OrderTypeChip(
                label = type.value.replace("_", " "),
                isSelected = selected == type,
                onClick = { onSelect(type) },
            )
        }
    }
}

@Composable
private fun OrderTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFFE33E3E) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(10.dp),
            )
            .background(if (isSelected) Color(0xFFFFF3F3) else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFFE33E3E) else Color(0xFF424242),
        )
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFFE33E3E),
                modifier = Modifier.size(18.dp),
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
            text = "₹${String.format(Locale.US, "%.2f", item.subtotal)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
        )
    }
}

@Composable
private fun BillPreviewSection(uiState: CreateOrderUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        BillRow("Subtotal", "₹${String.format(Locale.US, "%.2f", uiState.subtotal)}")
        BillRow(
            "Est. GST (18%)",
            "₹${String.format(Locale.US, "%.2f", uiState.estimatedTax)}",
            hint = true,
        )
        BillRow(
            "Est. Total",
            "₹${String.format(Locale.US, "%.2f", uiState.estimatedTotal)}",
            bold = true,
        )
        Text(
            text = "⚠ Actual GST calculated server-side when bill is generated",
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
                text = "Table No Longer Available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )
            Text(
                text = "This table was occupied by another order while you were reviewing your cart.\nPlease go back and select a different table.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Button(
                onClick = onReselectTable,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE33E3E)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Re-select Table", fontWeight = FontWeight.Bold)
            }
        }
    }
}

