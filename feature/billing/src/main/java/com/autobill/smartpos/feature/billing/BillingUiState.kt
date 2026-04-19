package com.autobill.smartpos.feature.billing

import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.BillStatus

/**
 * UI state for the Billing screen.
 *
 * Flow:
 *  1. User arrives with [orderId]. Screen shows a discount input + "Generate Bill" button.
 *  2. On submit → [isGenerating] = true.
 *  3. On success → [bill] is populated; discount input is hidden; bill summary shown.
 *  4. "Proceed to Payment" button becomes visible when [bill] is in ISSUED or PARTIAL status.
 *  5. [billAlreadyExists] — one-shot: true when server returns 409 (bill already existed).
 *     Route re-fetches the bill by orderId and populates [bill] so the user can still pay.
 *
 * [canCancelBill] — only ISSUED bills can be cancelled (PARTIAL/PAID cannot).
 */
data class BillingUiState(
    val orderId: Long = 0L,
    val tableId: Long = 0L,
    val restaurantId: Long = 0L,
    // Role-based permission — false for staff; drives discount field visibility.
    // Backend confirmed: POST .../generate-bill has no server-side role restriction —
    // this is a UI-only guard per backend review ❌ 2.6.
    val canApplyDiscounts: Boolean = false,
    // Discount input
    val discountInput: String = "",
    val discountError: String? = null,
    // Generate bill
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    // Bill result
    val bill: Bill? = null,
    val isLoadingExistingBill: Boolean = false,
    // Cancel bill
    val showCancelDialog: Boolean = false,
    val isCancelling: Boolean = false,
    // One-shot events
    val billAlreadyExists: Boolean = false,   // 409 — route re-fetches and populates bill
    val successMessage: String? = null,
    val navigateToPayment: Boolean = false,   // one-shot: route navigates to PaymentScreen
) {
    /** True when the bill can still accept payment (not yet fully paid / cancelled). */
    val canPay: Boolean
        get() = bill?.status == BillStatus.ISSUED || bill?.status == BillStatus.PARTIAL

    /** Only ISSUED bills can be cancelled. */
    val canCancelBill: Boolean
        get() = bill?.status == BillStatus.ISSUED
}

