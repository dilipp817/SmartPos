package com.autobill.smartpos.feature.food

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Cart Summary Sidebar - ODRfast Design
 * Right sidebar with invoice header, cart items, and checkout actions
 */
@Composable
fun CartSummaryFooter(
    data: CartSummaryData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(360.dp)
            .fillMaxHeight()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Invoice Header Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.invoice_number_label, data.invoice.invoiceNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                    )
                    Text(
                        text = stringResource(R.string.table_number_label, data.invoice.tableNumber),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF757575),
                    )
                }
                Button(
                    onClick = data.invoice.onChangeInvoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE33E3E),
                    ),
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(
                        text = stringResource(R.string.change_invoice),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Text(
                text = data.invoice.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF757575),
            )
        }

        // Cart Items List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = data.items,
                key = { it.id },
            ) { item ->
                CartItemRow(
                    item = item,
                    onIncrease = { data.onQuantityIncrease(item.id) },
                    onDecrease = { data.onQuantityDecrease(item.id) },
                )
            }
        }

        // Bill Breakdown
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BillRow(label = stringResource(R.string.subtotal), value = data.subtotal, isRegular = true)
            // Est. tax shown for preview only — actual GST computed server-side via generate-bill (Phase 6)
            BillRow(label = stringResource(R.string.est_gst), value = data.tax, isRegular = true)
            if (data.discount != "₹0.00") {
                BillRow(label = stringResource(R.string.discount), value = data.discount, isRegular = true)
            }
            // Apply Discount button — only visible for manager / admin / super_admin
            if (data.canApplyDiscount) {
                OutlinedButton(
                    onClick = data.onApplyDiscountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE33E3E),
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.apply_discount),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.total_amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = data.total,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
            }
        }

        // Accept Payment Button
        Button(
            onClick = data.onAcceptPayment,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE33E3E),
            ),
        ) {
            Text(
                text = stringResource(R.string.accept_payment),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = data.onClear,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF212121),
                ),
            ) {
                Text(
                    text = stringResource(R.string.clear),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = data.onReset,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF212121),
                ),
            ) {
                Text(
                    text = stringResource(R.string.reset),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = data.onPrint,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF212121),
                ),
            ) {
                Text(
                    text = stringResource(R.string.print),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItemUI,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFBDBDBD),
            )
        }

        // Item Name
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // Quantity Picker
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier.size(28.dp),
            ) {
                Box(
                    modifier = Modifier.size(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "−", // Minus sign
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF757575),
                    )
                }
            }
            Text(
                text = item.quantity.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121),
                modifier = Modifier.width(24.dp),
            )
            IconButton(
                onClick = onIncrease,
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_increase),
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        // Subtotal
        Text(
            text = item.subtotal,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            modifier = Modifier.width(60.dp),
        )
    }
}

@Composable
private fun BillRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isRegular: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = if (isRegular) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
            color = Color(0xFF757575),
        )
        Text(
            text = value,
            style = if (isRegular) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium,
            fontWeight = if (isRegular) FontWeight.Normal else FontWeight.Bold,
            color = Color(0xFF212121),
        )
    }
}
