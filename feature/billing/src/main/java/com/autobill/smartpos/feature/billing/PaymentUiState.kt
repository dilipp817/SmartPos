package com.autobill.smartpos.feature.billing

import com.autobill.smartpos.domain.model.Payment
import com.autobill.smartpos.domain.model.PaymentMethod

/**
 * UI state for the Payment screen.
 *
 * ## Flow
 * 1. User selects [selectedMethod] (CASH / CARD / UPI / WALLET).
 * 2. For CASH: user enters [amountTenderedInput] (≥ totalAmount) → change displayed.
 * 3. Tap "Pay" → [isProcessing] = true.
 * 4. CASH / UPI / WALLET → single call with autoProcess=true → [paymentSuccess] one-shot.
 *    CARD → two-step:
 *      a. POST /payments → creates PENDING → [showCardConfirmDialog] = true.
 *      b. User taps "Confirm Payment" → PATCH /payments/{id}/process → [paymentSuccess].
 * 5. After success → route calls [onPaymentSuccess] (tableId) → frees the table.
 *
 * [referenceNumber] is generated ONCE before the first API call and held in the ViewModel.
 * On network-error retry the SAME reference is reused. A new one is only generated if the
 * returned status is FAILED.
 */
data class PaymentUiState(
    val billId: Long = 0L,
    val orderId: Long = 0L,
    val tableId: Long = 0L,
    val totalAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    // Method selection
    val selectedMethod: PaymentMethod = PaymentMethod.CASH,
    // Cash-specific
    val amountTenderedInput: String = "",
    val amountTenderedError: String? = null,
    val changeAmount: Double = 0.0,
    // Optional note
    val notesInput: String = "",
    // In-flight
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    // CARD two-step confirmation
    val pendingPayment: Payment? = null,
    val showCardConfirmDialog: Boolean = false,
    val isConfirmingCard: Boolean = false,
    // Payments history (optional)
    val existingPayments: List<Payment> = emptyList(),
    // One-shot events
    val paymentSuccess: Payment? = null,  // non-null → navigate out + free table
    val successMessage: String? = null,
) {
    /** Effective amount to pay — remaining balance (for partial / split scenarios). */
    val effectiveAmount: Double
        get() = if (remainingAmount > 0.0) remainingAmount else totalAmount

    /** For CASH: change to return to customer (0 if tendered < effectiveAmount). */
    val computedChange: Double
        get() {
            val tendered = amountTenderedInput.toDoubleOrNull() ?: 0.0
            return if (tendered > effectiveAmount) tendered - effectiveAmount else 0.0
        }
}

