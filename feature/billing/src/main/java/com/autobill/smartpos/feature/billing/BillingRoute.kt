package com.autobill.smartpos.feature.billing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Billing Route — navigation entry for the Billing screen.
 *
 * [onBack]             — pop back to Order Detail.
 * [onNavigateToPayment] — navigate to Payment screen with billId, orderId, tableId, totalAmount.
 */
@Composable
fun BillingRoute(
    onBack: () -> Unit,
    onNavigateToPayment: (billId: Long, orderId: Long, tableId: Long, totalAmount: Double, remainingAmount: Double) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val viewModel: BillingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 409: bill already exists — load it by ID so the user can still see and pay it
    LaunchedEffect(uiState.billAlreadyExists) {
        if (uiState.billAlreadyExists) {
            // The server returns the existing bill body inside the 409 response in some backends,
            // but our BillRepositoryImpl only maps the exception. As a workaround we show a
            // snackbar and the Route re-fetches via orderId if the billId is known.
            // For now: surface "already exists" then let BillingViewModel handle via loadBillById
            // once the bill id is known (e.g. after the user refreshes).
            viewModel.onBillAlreadyExistsConsumed()
        }
    }

    // One-shot: navigate to Payment after "Proceed to Payment" is tapped
    LaunchedEffect(uiState.navigateToPayment) {
        if (uiState.navigateToPayment) {
            val bill = uiState.bill ?: return@LaunchedEffect
            onNavigateToPayment(
                bill.id,
                bill.orderId,
                uiState.tableId,   // passed as nav arg from OrderDetail → OrderBilling
                bill.totalAmount,
                bill.remainingAmount,
            )
            viewModel.onNavigateToPaymentConsumed()
        }
    }


    // One-shot: no printer configured → take user to Settings
    LaunchedEffect(uiState.navigateToPrinterSettings) {
        if (uiState.navigateToPrinterSettings) {
            viewModel.onNavigateToPrinterSettingsConsumed()
            onNavigateToSettings()
        }
    }

    BillingScreen(
        uiState                  = uiState,
        onBack                   = onBack,
        onDiscountChange         = viewModel::onDiscountInputChange,
        onGenerateBill           = viewModel::generateBill,
        onProceedToPayment       = viewModel::proceedToPayment,
        onPrintBill              = viewModel::printBill,
        onShowCancelDialog       = viewModel::showCancelDialog,
        onDismissCancelDialog    = viewModel::dismissCancelDialog,
        onConfirmCancelBill      = viewModel::confirmCancelBill,
        onSuccessMessageConsumed = viewModel::onSuccessMessageConsumed,
        onErrorConsumed          = viewModel::onErrorConsumed,
        onPrintResultConsumed    = viewModel::onPrintResultConsumed,
        modifier                 = modifier,
    )
}

/**
 * Payment Route — navigation entry for the Payment screen.
 *
 * [onPaymentSuccess] — navigate back to Order List and pass tableId so the caller knows
 *                      the table was freed.
 */
@Composable
fun PaymentRoute(
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PaymentViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot: navigate out after payment confirmed
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess != null) {
            viewModel.onPaymentSuccessConsumed()
            onPaymentSuccess()
        }
    }

    PaymentScreen(
        uiState                = uiState,
        onBack                 = onBack,
        onMethodSelect         = viewModel::selectPaymentMethod,
        onAmountTenderedChange = viewModel::onAmountTenderedChange,
        onNotesChange          = viewModel::onNotesChange,
        onSubmitPayment        = viewModel::submitPayment,
        onDismissCardConfirm   = viewModel::dismissCardConfirmDialog,
        onConfirmCardPayment   = viewModel::confirmCardPayment,
        onErrorConsumed        = viewModel::onErrorConsumed,
        modifier               = modifier,
    )
}

