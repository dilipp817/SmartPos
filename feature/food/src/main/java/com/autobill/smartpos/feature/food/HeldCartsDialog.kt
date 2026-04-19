package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Dialog showing all currently held bills.
 * Cashier can Resume (swap with current cart) or Delete (cancel) each held bill.
 */
@Composable
fun HeldCartsDialog(
    heldCarts: List<HeldCart>,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.held_bills_dialog_title)) },
        text = {
            if (heldCarts.isEmpty()) {
                Text(
                    text = stringResource(R.string.held_bills_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(heldCarts, key = { it.id }) { held ->
                        HeldCartRow(
                            held = held,
                            onResume = { onResume(held.id) },
                            onDelete = { onDelete(held.id) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.held_bills_dialog_close))
            }
        },
    )
}

@Composable
private fun HeldCartRow(
    held: HeldCart,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = held.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
            )
            Text(
                text = stringResource(
                    R.string.held_bills_item_count,
                    held.items.sumOf { it.quantity },
                    String.format(Locale.US, "%.2f", held.totalAmount),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF757575),
            )
        }
        Row {
            TextButton(onClick = onResume) {
                Text(
                    text = stringResource(R.string.held_bills_resume),
                    color = Color(0xFF4CAF50),
                )
            }
            TextButton(onClick = onDelete) {
                Text(
                    text = stringResource(R.string.held_bills_delete),
                    color = Color(0xFFE33E3E),
                )
            }
        }
    }
}



