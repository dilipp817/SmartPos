package com.autobill.smartpos.feature.table

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Table

/**
 * Simple confirmation dialog before permanently deleting a table.
 *
 * Shows key info so the admin is sure they're deleting the right table.
 * The backend will reject the request if the table is OCCUPIED / RESERVED.
 */
@Composable
fun TableDeleteConfirmDialog(
    table: Table,
    isInFlight: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isInFlight) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Delete Table?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to permanently remove:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF424242),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Table ${table.tableNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Text(
                    text = "Floor ${table.floor}  ·  👥 ${table.capacity} seats  ·  ${table.status.value}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This action cannot be undone. Only tables that are not currently occupied or reserved can be deleted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9E9E9E),
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                    )
                }
            }
        },
        confirmButton = {
            if (isInFlight) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(28.dp),
                    color = Color(0xFFC62828),
                    strokeWidth = 2.dp,
                )
            } else {
                TextButton(
                    onClick = onConfirm,
                    enabled = !isInFlight,
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isInFlight) {
                Text(
                    text = "Cancel",
                    color = if (!isInFlight) Color(0xFF757575) else Color(0xFFBDBDBD),
                )
            }
        },
    )
}

