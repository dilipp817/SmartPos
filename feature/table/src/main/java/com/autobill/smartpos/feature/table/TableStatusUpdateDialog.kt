package com.autobill.smartpos.feature.table

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.TableStatus

/**
 * Dialog for manually updating a table's status.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────┐
 * │  Change Table Status                                │
 * │  Table T-3 · Floor 1 · 👥 4                        │
 * │  ─────────────────────────────────────────────────  │
 * │  Current: [OCCUPIED]                               │
 * │                                                     │
 * │  Select new status:                                 │
 * │  ┌───────────┐  ┌───────────┐                      │
 * │  │ CLEANING  │  │MAINTENANCE│                      │
 * │  └───────────┘  └───────────┘                      │
 * │                                                     │
 * │  ⚠️ Error message (if any)                         │
 * │  ─────────────────────────────────────────────────  │
 * │               [Cancel]    [Confirm]                 │
 * └─────────────────────────────────────────────────────┘
 */
@Composable
fun TableStatusUpdateDialog(
    dialogState: StatusUpdateDialogState,
    isUpdating: Boolean,
    errorMessage: String?,
    onConfirm: (TableStatus) -> Unit,
    onDismiss: () -> Unit,
) {
    val table = dialogState.table
    var selectedStatus: TableStatus? by rememberSaveable { mutableStateOf(null) }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = stringResource(R.string.table_status_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.table_status_dialog_summary, table.tableNumber, table.floor, table.capacity),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Current status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.table_status_current_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF757575),
                    )
                    StatusBadge(status = table.status)
                }

                // New status options
                Text(
                    text = stringResource(R.string.table_status_select_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF424242),
                    fontWeight = FontWeight.SemiBold,
                )

                // Status chips grid — wrap into rows of 2
                val transitions = dialogState.availableTransitions
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transitions.chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            rowItems.forEach { status ->
                                val isSelected = selectedStatus == status
                                StatusOptionChip(
                                    status = status,
                                    isSelected = isSelected,
                                    enabled = !isUpdating,
                                    onClick = { selectedStatus = status },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Fill remainder with empty space if odd count
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Inline error message
                if (errorMessage != null) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                    )
                }
            }
        },
        confirmButton = {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color(0xFFE33E3E),
                    strokeWidth = 2.dp,
                )
            } else {
                TextButton(
                    onClick = { selectedStatus?.let(onConfirm) },
                    enabled = selectedStatus != null,
                ) {
                    Text(
                        text = stringResource(R.string.table_status_confirm),
                        color = if (selectedStatus != null) Color(0xFFE33E3E) else Color(0xFFBDBDBD),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating,
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = if (!isUpdating) Color(0xFF757575) else Color(0xFFBDBDBD),
                )
            }
        },
    )
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(status: TableStatus) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(status.containerColor()))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = status.value,
            style = MaterialTheme.typography.labelSmall,
            color = Color(status.contentColor()),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * A selectable status chip inside the dialog.
 * Selected state is highlighted with the status's own colour + a checkmark.
 */
@Composable
private fun StatusOptionChip(
    status: TableStatus,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (isSelected) Color(status.containerColor()) else Color(0xFFF5F5F5)
    val contentColor   = if (isSelected) Color(status.contentColor())   else Color(0xFF616161)
    val borderColor    = if (isSelected) Color(status.contentColor()).copy(alpha = 0.5f)
                         else Color(0xFFE0E0E0)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(containerColor)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = status.value,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

