package com.autobill.smartpos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.ui.R

/**
 * Reusable Loading & Dialog Components for SmartPos
 * Tablet-optimized with proper sizing and animations
 */

/**
 * Full Screen Loading Indicator
 * Shows centered spinner with message
 * 
 * @param modifier Modifier for styling
 * @param message Loading message
 */
@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.loading_default_message),
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

/**
 * Small Loading Indicator - for buttons
 * Compact circular progress
 */
@Composable
fun SmallProgressIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = MaterialTheme.colorScheme.onPrimary,
        strokeWidth = 2.dp,
    )
}

/**
 * Linear Loading Bar
 * Shows at top of screen for overall loading state
 * 
 * @param modifier Modifier for styling
 */
@Composable
fun LinearLoadingBar(
    modifier: Modifier = Modifier,
) {
    LinearProgressIndicator(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.outlineVariant,
    )
}

/**
 * Confirmation Dialog
 * Two-button dialog for user confirmation
 * 
 * @param modifier Modifier for styling
 * @param title Dialog title
 * @param message Dialog message
 * @param confirmText Confirmation button text
 * @param dismissText Dismiss button text
 * @param onConfirm Callback when confirmed
 * @param onDismiss Callback when dismissed
 * @param isDangerous Whether this is a dangerous action
 */
@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.dialog_confirm_default),
    dismissText: String = stringResource(R.string.dialog_dismiss_default),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDangerous: Boolean = false,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            PrimaryButton(
                onClick = onConfirm,
                text = confirmText,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        dismissButton = {
            if (isDangerous) {
                DangerButton(
                    onClick = onConfirm,
                    text = confirmText,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                SecondaryButton(
                    onClick = onDismiss,
                    text = dismissText,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

/**
 * Alert Dialog - For showing alerts/messages
 * 
 * @param modifier Modifier for styling
 * @param title Alert title
 * @param message Alert message
 * @param buttonText Button text
 * @param onDismiss Callback when dismissed
 */
@Composable
fun SimpleAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    buttonText: String = stringResource(R.string.dialog_ok_default),
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            PrimaryButton(
                onClick = onDismiss,
                text = buttonText,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

/**
 * Error Banner - Inline error display
 * Shows error message in red banner
 * 
 * @param modifier Modifier for styling
 * @param message Error message
 * @param onDismiss Optional dismiss callback
 */
@Composable
fun ErrorBanner(
    modifier: Modifier = Modifier,
    message: String,
    onDismiss: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
            )
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
            )
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(16.dp))
                SmartPosTextButton(
                    onClick = onDismiss,
                    text = stringResource(R.string.dialog_dismiss_snackbar),
                )
            }
        }
    }
}

/**
 * Success Banner - Inline success display
 * Shows success message in green banner
 * 
 * @param modifier Modifier for styling
 * @param message Success message
 */
@Composable
fun SuccessBanner(
    modifier: Modifier = Modifier,
    message: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFC8E6C9),  // Light green
                shape = RoundedCornerShape(8.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF2E7D32),  // Dark green
        )
    }
}

/**
 * Info Banner - Inline info display
 * Shows info message in blue banner
 * 
 * @param modifier Modifier for styling
 * @param message Info message
 */
@Composable
fun InfoBanner(
    modifier: Modifier = Modifier,
    message: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFBBDEFB),  // Light blue
                shape = RoundedCornerShape(8.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF1565C0),  // Dark blue
        )
    }
}
