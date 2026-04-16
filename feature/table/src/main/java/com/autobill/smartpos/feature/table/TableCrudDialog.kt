package com.autobill.smartpos.feature.table

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.autobill.smartpos.domain.model.Table

/**
 * Shared Create / Edit dialog for a table.
 *
 * When [editTable] is null   → Create mode ("Add Table")
 * When [editTable] is set    → Edit mode  ("Edit Table T-3")
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │  Add Table  /  Edit Table T-3               │
 * │  ─────────────────────────────────────────  │
 * │  Table Number  [_______________________]    │
 * │  Floor         [−]  1  [+]                  │
 * │  Capacity      [−]  4  [+]                  │
 * │  ⚠️ error (if any)                          │
 * │  ─────────────────────────────────────────  │
 * │                     [Cancel]  [Save]        │
 * └─────────────────────────────────────────────┘
 */
@Composable
fun TableCrudDialog(
    editTable: Table?,          // null = Create mode
    isInFlight: Boolean,
    errorMessage: String?,
    onConfirm: (tableNumber: String, floor: Int, capacity: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val isEditMode = editTable != null
    val title = if (isEditMode) {
        stringResource(R.string.table_crud_title_edit, editTable!!.tableNumber)
    } else {
        stringResource(R.string.table_crud_title_add)
    }

    var tableNumber by rememberSaveable { mutableStateOf(editTable?.tableNumber ?: "") }
    var floor       by rememberSaveable { mutableIntStateOf(editTable?.floor ?: 1) }
    var capacity    by rememberSaveable { mutableIntStateOf(editTable?.capacity ?: 4) }

    // Show the error only after the user has touched the field at least once,
    // so the dialog doesn't open with a red error state on first render.
    var tableNumberTouched by rememberSaveable { mutableStateOf(false) }

    val tableNumberError = tableNumber.isBlank()
    val showTableNumberError = tableNumberError && tableNumberTouched
    val canConfirm = !tableNumberError && !isInFlight

    AlertDialog(
        onDismissRequest = { if (!isInFlight) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isEditMode) stringResource(R.string.table_crud_subtitle_edit)
                           else stringResource(R.string.table_crud_subtitle_add),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // ── Table Number ───────────────────────────────────────────
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = {
                        tableNumber = it
                        tableNumberTouched = true
                    },
                    label = { Text(stringResource(R.string.table_crud_field_number)) },
                    placeholder = { Text(stringResource(R.string.table_crud_field_number_placeholder)) },
                    singleLine = true,
                    isError = showTableNumberError,
                    supportingText = if (showTableNumberError) {
                        { Text(stringResource(R.string.table_crud_field_number_error)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE33E3E),
                        focusedLabelColor = Color(0xFFE33E3E),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isInFlight,
                )

                // ── Floor Stepper ──────────────────────────────────────────
                StepperRow(
                    label = stringResource(R.string.table_crud_field_floor),
                    value = floor,
                    onDecrement = { if (floor > 1) floor-- },
                    onIncrement = { if (floor < 99) floor++ },
                    enabled = !isInFlight,
                )

                // ── Capacity Stepper ───────────────────────────────────────
                StepperRow(
                    label = stringResource(R.string.table_crud_field_capacity),
                    value = capacity,
                    onDecrement = { if (capacity > 1) capacity-- },
                    onIncrement = { if (capacity < 50) capacity++ },
                    enabled = !isInFlight,
                )

                // ── Inline error ───────────────────────────────────────────
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
            if (isInFlight) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(28.dp),
                    color = Color(0xFFE33E3E),
                    strokeWidth = 2.dp,
                )
            } else {
                TextButton(
                    onClick = { if (canConfirm) onConfirm(tableNumber, floor, capacity) },
                    enabled = canConfirm,
                ) {
                    Text(
                        text = stringResource(R.string.table_crud_save),
                        color = if (canConfirm) Color(0xFFE33E3E) else Color(0xFFBDBDBD),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isInFlight) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = if (!isInFlight) Color(0xFF757575) else Color(0xFFBDBDBD),
                )
            }
        },
    )
}

// ── Stepper helper ────────────────────────────────────────────────────────────

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onDecrement, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.table_crud_cd_decrease, label),
                    tint = if (enabled) Color(0xFFE33E3E) else Color(0xFFBDBDBD),
                )
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.width(36.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = onIncrement, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.table_crud_cd_increase, label),
                    tint = if (enabled) Color(0xFFE33E3E) else Color(0xFFBDBDBD),
                )
            }
        }
    }
}

