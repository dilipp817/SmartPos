package com.autobill.smartpos.feature.billing

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Billing Screen — Phase 6.1 & 6.2
 *
 * States:
 *  1. No bill yet → shows optional discount input + "Generate Bill" button.
 *  2. Bill generated → shows [BillSummaryCard] + "Proceed to Payment" button.
 *  3. Bill PAID → shows bill summary in read-only state (no payment button).
 *  4. Bill CANCELLED → shows summary with a cancelled badge.
 *
 * The server computes all tax amounts — this screen only displays what comes back.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    uiState: BillingUiState,
    onBack: () -> Unit,
    onDiscountChange: (String) -> Unit,
    onGenerateBill: () -> Unit,
    onProceedToPayment: () -> Unit,
    onShowCancelDialog: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancelBill: () -> Unit,
    onSuccessMessageConsumed: () -> Unit,
    onErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccessMessageConsumed()
        }
    }
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
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color(0xFF212121))
                }
                Text(
                    text       = "Billing",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF212121),
                    modifier   = Modifier.weight(1f).padding(start = 4.dp),
                )
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint     = Color(0xFFE33E3E),
                    modifier = Modifier.padding(end = 12.dp).size(24.dp),
                )
            }

            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.weight(1f).fillMaxWidth(),
            ) {
                when {
                    // Loading existing bill
                    uiState.isLoadingExistingBill -> item {
                        BillingLoadingState()
                    }

                    // Bill already generated — show summary
                    uiState.bill != null -> {
                        item { BillSummaryCard(bill = uiState.bill) }

                        if (uiState.canPay) {
                            item {
                                Button(
                                    onClick  = onProceedToPayment,
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE33E3E)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                ) {
                                    Text(
                                        text       = "Proceed to Payment  ₹%.2f".format(
                                            if (uiState.bill.remainingAmount > 0)
                                                uiState.bill.remainingAmount
                                            else uiState.bill.totalAmount
                                        ),
                                        fontWeight = FontWeight.Bold,
                                        style      = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                            }
                        }

                        if (uiState.canCancelBill) {
                            item {
                                OutlinedButton(
                                    onClick  = onShowCancelDialog,
                                    shape    = RoundedCornerShape(10.dp),
                                    colors   = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFB00020)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Cancel Bill", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // No bill yet — show generate bill form
                    else -> {
                        item {
                            GenerateBillForm(
                                discountInput  = uiState.discountInput,
                                discountError  = uiState.discountError,
                                isGenerating   = uiState.isGenerating,
                                onDiscountChange = onDiscountChange,
                                onGenerate     = onGenerateBill,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
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

    if (uiState.showCancelDialog) {
        AlertDialog(
            onDismissRequest = { if (!uiState.isCancelling) onDismissCancelDialog() },
            title = { Text("Cancel Bill?", fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "Bill ${uiState.bill?.billNumber.orEmpty()} will be voided. " +
                    "This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                if (uiState.isCancelling) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color       = Color(0xFFB00020),
                    )
                } else {
                    TextButton(onClick = onConfirmCancelBill) {
                        Text("Cancel Bill", color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancelDialog, enabled = !uiState.isCancelling) {
                    Text("Keep Bill", color = Color(0xFF757575))
                }
            },
            containerColor = Color.White,
            shape          = RoundedCornerShape(16.dp),
        )
    }
}

// ── Generate Bill Form ────────────────────────────────────────────────────────

@Composable
private fun GenerateBillForm(
    discountInput: String,
    discountError: String?,
    isGenerating: Boolean,
    onDiscountChange: (String) -> Unit,
    onGenerate: () -> Unit,
) {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text       = "Generate Bill",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121),
        )
        Text(
            text  = "Tax (18% GST) is calculated automatically by the server.\n" +
                    "Enter an optional discount in rupees (e.g. 50 for ₹50 off).",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
        )

        OutlinedTextField(
            value         = discountInput,
            onValueChange = onDiscountChange,
            label         = { Text("Discount (₹) — optional") },
            placeholder   = { Text("0") },
            isError       = discountError != null,
            supportingText = discountError?.let { { Text(it, color = Color(0xFFB00020)) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine    = true,
            enabled       = !isGenerating,
            modifier      = Modifier.fillMaxWidth(),
        )

        Button(
            onClick  = onGenerate,
            enabled  = !isGenerating,
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFE33E3E)),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color       = Color.White,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Generating…", fontWeight = FontWeight.Bold)
            } else {
                Text("Generate Bill", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

// ── Loading ────────────────────────────────────────────────────────────────────

@Composable
private fun BillingLoadingState() {
    Box(
        modifier            = Modifier.fillMaxWidth().height(200.dp),
        contentAlignment    = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                modifier    = Modifier.size(44.dp),
                strokeWidth = 3.dp,
                color       = Color(0xFFE33E3E),
            )
            Text(
                "Loading bill…",
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
            )
        }
    }
}

