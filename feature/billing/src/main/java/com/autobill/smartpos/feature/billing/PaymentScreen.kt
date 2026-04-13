package com.autobill.smartpos.feature.billing

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.PaymentMethod

/**
 * Payment Screen — Phase 6.3
 *
 * - Method selector: CASH | CARD | UPI | WALLET
 * - For CASH: shows amount-tendered field + change display
 * - For CARD: two-step confirm dialog after PENDING creation
 * - Amount due is the bill's remaining amount (or total if no partial payments yet)
 */
@Composable
fun PaymentScreen(
    uiState: PaymentUiState,
    onBack: () -> Unit,
    onMethodSelect: (PaymentMethod) -> Unit,
    onAmountTenderedChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitPayment: () -> Unit,
    onDismissCardConfirm: () -> Unit,
    onConfirmCardPayment: () -> Unit,
    onErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorConsumed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)),
        ) {
            // ── Top Bar ─────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, enabled = !uiState.isProcessing) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF212121))
                }
                Text(
                    text       = "Payment",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF212121),
                    modifier   = Modifier.weight(1f).padding(start = 4.dp),
                )
            }

            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.weight(1f).fillMaxWidth(),
            ) {
                // ── Amount Due Card ──────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE33E3E))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text  = "Amount Due",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = "₹%.2f".format(uiState.effectiveAmount),
                            style      = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White,
                        )
                        if (uiState.remainingAmount > 0 && uiState.remainingAmount < uiState.totalAmount) {
                            Text(
                                text  = "Remaining balance (total ₹%.2f)".format(uiState.totalAmount),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                // ── Method Selector ──────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Payment Method",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF212121),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(PaymentMethod.entries) { method ->
                                MethodChip(
                                    method     = method,
                                    isSelected = uiState.selectedMethod == method,
                                    onClick    = { onMethodSelect(method) },
                                )
                            }
                        }
                    }
                }

                // ── CASH fields ───────────────────────────────────────────────
                if (uiState.selectedMethod == PaymentMethod.CASH) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                "Cash Tendered",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFF212121),
                            )
                            OutlinedTextField(
                                value         = uiState.amountTenderedInput,
                                onValueChange = onAmountTenderedChange,
                                label         = { Text("Amount received from customer (₹)") },
                                isError       = uiState.amountTenderedError != null,
                                supportingText = uiState.amountTenderedError?.let {
                                    { Text(it, color = Color(0xFFB00020)) }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine    = true,
                                modifier      = Modifier.fillMaxWidth(),
                            )
                            if (uiState.changeAmount > 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        "Change to return",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF2E7D32),
                                    )
                                    Text(
                                        "₹%.2f".format(uiState.changeAmount),
                                        style      = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color      = Color(0xFF2E7D32),
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Optional Note ─────────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .padding(16.dp),
                    ) {
                        OutlinedTextField(
                            value         = uiState.notesInput,
                            onValueChange = onNotesChange,
                            label         = { Text("Note (optional)") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // ── Pay Button ────────────────────────────────────────────────
                item {
                    val buttonLabel = when (uiState.selectedMethod) {
                        PaymentMethod.CASH   -> "Collect ₹%.2f Cash".format(uiState.effectiveAmount)
                        PaymentMethod.CARD   -> "Charge ₹%.2f to Card".format(uiState.effectiveAmount)
                        PaymentMethod.UPI    -> "Pay ₹%.2f via UPI".format(uiState.effectiveAmount)
                        PaymentMethod.WALLET -> "Pay ₹%.2f via Wallet".format(uiState.effectiveAmount)
                    }
                    Button(
                        onClick  = onSubmitPayment,
                        enabled  = !uiState.isProcessing && !uiState.isConfirmingCard,
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE33E3E)),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color       = Color.White,
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Processing…", fontWeight = FontWeight.Bold)
                        } else {
                            Text(buttonLabel, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                item { Spacer(Modifier.height(32.dp)) }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        ) { data ->
            Snackbar(
                snackbarData   = data,
                containerColor = Color(0xFF323232),
                contentColor   = Color.White,
                shape          = RoundedCornerShape(12.dp),
            )
        }
    }

    // ── CARD Confirm Dialog ─────────────────────────────────────────────────
    if (uiState.showCardConfirmDialog) {
        AlertDialog(
            onDismissRequest = { onDismissCardConfirm() },
            title = { Text("Confirm Card Payment", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Card payment of ₹%.2f has been authorised.\n" +
                        "Tap Confirm once the terminal shows Approved.".format(uiState.effectiveAmount),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    HorizontalDivider()
                    Text(
                        text  = "Reference: ${uiState.pendingPayment?.referenceNumber.orEmpty()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E),
                    )
                }
            },
            confirmButton = {
                if (uiState.isConfirmingCard) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color       = Color(0xFF388E3C),
                    )
                } else {
                    Button(
                        onClick = onConfirmCardPayment,
                        colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Confirm Payment", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCardConfirm, enabled = !uiState.isConfirmingCard) {
                    Text("Cancel", color = Color(0xFF757575))
                }
            },
            containerColor = Color.White,
            shape          = RoundedCornerShape(16.dp),
        )
    }
}

// ── Method Chip ───────────────────────────────────────────────────────────────

@Composable
private fun MethodChip(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val label = when (method) {
        PaymentMethod.CASH   -> "💵 Cash"
        PaymentMethod.CARD   -> "💳 Card"
        PaymentMethod.UPI    -> "📱 UPI"
        PaymentMethod.WALLET -> "👛 Wallet"
    }
    val bg     = if (isSelected) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
    val border = if (isSelected) Color(0xFFE33E3E) else Color(0xFFE0E0E0)
    val text   = if (isSelected) Color(0xFFE33E3E) else Color(0xFF424242)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = text,
        )
    }
}

