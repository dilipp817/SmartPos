package com.autobill.smartpos.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.ui.R

// Confirm Dialog - Confirm destructive actions (delete order, cancel bill)
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.dialog_confirm_default),
    dismissText: String = stringResource(R.string.dialog_dismiss_default),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    confirmText,
                    color = if (isDestructive) Color(0xFFB00020) else Color.Unspecified,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}

// Alert Dialog - Display important messages
@Composable
fun SmartPosAlertDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    dismissText: String = stringResource(R.string.dialog_ok_default),
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}

// Loading Dialog - Full-screen loading overlay
@Composable
fun LoadingDialog(
    message: String = stringResource(R.string.loading_default_message),
) {
    AlertDialog(
        onDismissRequest = {}, // Can't dismiss
        title = null,
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                CircularProgressIndicator()
                Spacer(
                    modifier = Modifier.height(16.dp),
                )
                Text(message)
            }
        },
        confirmButton = {},
    )
}

// Input Dialog - Get user input (discount reason, special instructions)
@Composable
fun InputDialog(
    title: String,
    initialValue: String = "",
    placeholder: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = stringResource(R.string.dialog_ok_default),
    dismissText: String = stringResource(R.string.dialog_dismiss_default),
) {
    val inputValue = remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TextField(
                value = inputValue.value,
                onValueChange = { inputValue.value = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(inputValue.value) },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
    )
}
