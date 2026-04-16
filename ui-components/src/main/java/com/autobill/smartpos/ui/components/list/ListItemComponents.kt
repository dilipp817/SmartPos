package com.autobill.smartpos.ui.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.ui.R
import com.autobill.smartpos.ui.components.cards.QuantityControl
import com.autobill.smartpos.ui.components.theme.StatusColors

// Order Item List Item - Display individual item in order list
@Composable
fun OrderItemListItem(
    modifier: Modifier = Modifier,
    menuItemName: String,
    quantity: Int,
    subtotal: Double,
    variantName: String? = null,
    specialInstructions: String? = null,
    onRemove: () -> Unit = {},
    onQuantityChange: (Int) -> Unit = {},
    canModify: Boolean = false,
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menuItemName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (variantName != null) {
                    Text(
                        text = variantName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (specialInstructions != null) {
                    Text(
                        text = specialInstructions,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }

            if (canModify) {
                QuantityControl(
                    quantity = quantity,
                    onQuantityChange = onQuantityChange,
                )
            } else {
                Text(text = stringResource(R.string.list_item_quantity, quantity), style = MaterialTheme.typography.labelMedium)
            }

            Text(
                text = subtotal.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )

            if (canModify) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.list_item_cd_remove),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
    }
}

// Order Summary List Item - Compact order display
@Composable
fun OrderListItemCompact(
    modifier: Modifier = Modifier,
    orderNumber: String,
    itemCount: Int,
    total: Double,
    status: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = orderNumber,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$itemCount items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = total.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

// Payment History Item - Display payment transaction
@Composable
fun PaymentHistoryItem(
    modifier: Modifier = Modifier,
    amount: Double,
    paymentMethod: String,
    timestamp: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = paymentMethod.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = amount.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// Table List Item - Display table in list view
@Composable
fun TableListItem(
    modifier: Modifier = Modifier,
    tableNumber: String,
    capacity: Int,
    status: String,
    currentOrderId: Int? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tableNumber,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.list_item_capacity, capacity),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (currentOrderId != null) {
                Text(
                    text = stringResource(R.string.list_item_order_id, currentOrderId),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }

        Text(
            text = status.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = getStatusColor(status),
        )
    }
}

private fun getStatusColor(status: String): Color =
    StatusColors.tableStatusBackground(status)
