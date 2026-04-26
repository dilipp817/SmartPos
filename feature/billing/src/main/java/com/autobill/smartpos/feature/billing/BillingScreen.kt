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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.feature.billing.R

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
    onPrintBill: () -> Unit,
    onShowCancelDialog: () -> Unit,
    onDismissCancelDialog: () -> Unit,
    onConfirmCancelBill: () -> Unit,
    onSuccessMessageConsumed: () -> Unit,
    onErrorConsumed: () -> Unit,
    onPrintResultConsumed: () -> Unit,
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
    LaunchedEffect(uiState.printResultMessage) {
        uiState.printResultMessage?.let {
            snackbarHostState.showSnackbar(it)
            onPrintResultConsumed()
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back), tint = Color(0xFF212121))
                }
                Text(
                    text       = stringResource(R.string.billing_title),
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

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.TopCenter,
            ) {
            LazyColumn(
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth(),
            ) {
                when {
                    // Loading existing bill
                    uiState.isLoadingExistingBill -> item {
                        BillingLoadingState()
                    }

                    // Bill already generated — show summary
                    uiState.bill != null -> {
                        item { BillSummaryCard(bill = uiState.bill) }

                        // Print receipt button — always shown when a bill exists
                        item {
                            OutlinedButton(
                                onClick  = onPrintBill,
                                enabled  = !uiState.isPrinting,
                                shape    = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (uiState.isPrinting) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.billing_printing))
                                } else {
                                    Icon(
                                        Icons.Default.Print,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.billing_print_button_label), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

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
                                        text       = stringResource(R.string.billing_proceed_to_payment,
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
                                    Text(stringResource(R.string.billing_cancel_bill_button), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // No bill yet — show generate bill form
                    else -> {
                        item {
                            GenerateBillForm(
                                discountInput    = uiState.discountInput,
                                discountError    = uiState.discountError,
                                isGenerating     = uiState.isGenerating,
                                canApplyDiscounts = uiState.canApplyDiscounts,
                                onDiscountChange = onDiscountChange,
                                onGenerate       = onGenerateBill,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
            } // Box (max-width wrapper)
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
            title = { Text(stringResource(R.string.billing_cancel_dialog_title), fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    stringResource(R.string.billing_cancel_dialog_message, uiState.bill?.billNumber.orEmpty()),
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
                        Text(stringResource(R.string.billing_cancel_dialog_confirm), color = Color(0xFFB00020), fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancelDialog, enabled = !uiState.isCancelling) {
                    Text(stringResource(R.string.billing_cancel_dialog_dismiss), color = Color(0xFF757575))
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
    canApplyDiscounts: Boolean,
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
            text       = stringResource(R.string.billing_generate_bill_button),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF212121),
        )
        Text(
            text  = stringResource(R.string.billing_tax_info),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF757575),
        )

        // Discount input — hidden for staff role (backend review ❌ 2.6).
        // Backend does not enforce role restriction on generate-bill; UI-side guard only.
        if (canApplyDiscounts) {
            OutlinedTextField(
                value         = discountInput,
                onValueChange = onDiscountChange,
                label         = { Text(stringResource(R.string.billing_field_discount)) },
                placeholder   = { Text("0") },
                isError       = discountError != null,
                supportingText = discountError?.let { { Text(it, color = Color(0xFFB00020)) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                enabled       = !isGenerating,
                modifier      = Modifier.fillMaxWidth(),
            )
        }

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
                Text(stringResource(R.string.billing_generating), fontWeight = FontWeight.Bold)
            } else {
                Text(stringResource(R.string.billing_generate_bill_button), fontWeight = FontWeight.Bold,
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
                stringResource(R.string.billing_loading_bill),
                style     = MaterialTheme.typography.bodyMedium,
                color     = Color(0xFF9E9E9E),
                textAlign = TextAlign.Center,
            )
        }
    }
}
