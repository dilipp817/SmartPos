package com.autobill.smartpos.feature.billing

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus
import com.autobill.smartpos.domain.usecase.ConfirmCardPaymentUseCase
import com.autobill.smartpos.domain.usecase.FreeTableUseCase
import com.autobill.smartpos.domain.usecase.GetBillByIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ProcessPaymentUseCase
import com.autobill.smartpos.domain.util.PaymentReferenceGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Payment screen.
 *
 * Receives [billId], [orderId], [tableId], [totalAmount], [remainingAmount] via [SavedStateHandle].
 *
 * ## Bill fetch on init (M-16)
 * GET /bills/{billId} is called on start to load full bill (billItems, cgst, sgst,
 * remainingAmount). Nav-arg values are used as fallback while the fetch is in flight.
 *
 * ## Payment flow
 * - CASH / UPI / WALLET: single call with autoProcess=true → SUCCESS → navigate out.
 * - CARD: two-step:
 *   1. POST /payments (autoProcess=false) → PENDING → show confirm dialog.
 *   2. User taps Confirm → PATCH /payments/{id}/process → SUCCESS → navigate out.
 *
 * ## After success
 * [FreeTableUseCase] marks the table AVAILABLE.
 * [PaymentUiState.paymentSuccess] one-shot triggers the Route to navigate back.
 *
 * ## Reference number
 * Generated once per ViewModel instance and reused on retries within the same session.
 * A cashier is always present — if the app is killed mid-payment they simply retry.
 * No DataStore persistence needed (that level of idempotency is for autonomous payment SDKs).
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val confirmCardPaymentUseCase: ConfirmCardPaymentUseCase,
    private val freeTableUseCase: FreeTableUseCase,
    private val getBillByIdUseCase: GetBillByIdUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    companion object {
        private const val TAG = "PaymentViewModel"
    }

    private val billId: Long            = checkNotNull(savedStateHandle["billId"])
    private val orderId: Long           = checkNotNull(savedStateHandle["orderId"])
    private val tableId: Long           = checkNotNull(savedStateHandle["tableId"])
    // totalAmount and remainingAmount are encoded as String nav args (NavType.StringType)
    private val totalAmount: Double     =
        savedStateHandle.get<String>("totalAmount")?.toDoubleOrNull() ?: 0.0
    private val remainingAmount: Double =
        savedStateHandle.get<String>("remainingAmount")?.toDoubleOrNull() ?: totalAmount

    private var restaurantId: Long? = null

    /**
     * Reference number lives in ViewModel memory only.
     * Reused on retries within the same screen session.
     * Regenerated only when the server returns FAILED or 409 conflict.
     */
    private var currentReferenceNumber: String = PaymentReferenceGenerator.generate()

    private val _uiState = MutableStateFlow(
        PaymentUiState(
            billId          = billId,
            orderId         = orderId,
            tableId         = tableId,
            totalAmount     = totalAmount,
            remainingAmount = remainingAmount,
        )
    )
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update { it.copy(errorMessage = context.getString(R.string.error_session_expired)) }
                return@launch
            }
            loadFullBill()
        }
    }

    // ── Full bill fetch (M-16) ───────────────────────────────────────────────

    /**
     * GET /bills/{billId} — loads the full bill with line items and accurate
     * remaining amount. Updates [totalAmount] and [remainingAmount] in the UI
     * state with server values (overrides nav-arg values).
     */
    private fun loadFullBill() {
        viewModelScope.launch {
            when (val result = getBillByIdUseCase(billId)) {
                is Result.Success -> {
                    val bill = result.data
                    _uiState.update {
                        it.copy(
                            // M-20: use server-computed remainingAmount directly
                            totalAmount     = bill.totalAmount,
                            remainingAmount = bill.remainingAmount,
                        )
                    }
                }
                is Result.Failure -> {
                    // Non-critical — nav-arg values remain as fallback
                    Log.w(TAG, "Full bill fetch failed — using nav-arg amounts as fallback", result.exception)
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── User interactions ────────────────────────────────────────────────────

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update {
            it.copy(
                selectedMethod      = method,
                amountTenderedInput = "",
                amountTenderedError = null,
                changeAmount        = 0.0,
                errorMessage        = null,
            )
        }
    }

    fun onAmountTenderedChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val tendered = filtered.toDoubleOrNull() ?: 0.0
        val effective = _uiState.value.effectiveAmount
        val change = if (tendered > effective) tendered - effective else 0.0
        _uiState.update {
            it.copy(
                amountTenderedInput = filtered,
                amountTenderedError = null,
                changeAmount        = change,
            )
        }
    }

    fun onNotesChange(value: String) = _uiState.update { it.copy(notesInput = value) }

    fun submitPayment() {
        val state = _uiState.value
        // Validate cash amount
        if (state.selectedMethod == PaymentMethod.CASH) {
            val tendered = state.amountTenderedInput.toDoubleOrNull()
            if (tendered == null || tendered < state.effectiveAmount) {
                _uiState.update {
                    it.copy(amountTenderedError = context.getString(R.string.error_amount_too_low, state.effectiveAmount))
                }
                return
            }
        }

        val autoProcess = state.selectedMethod != PaymentMethod.CARD
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = processPaymentUseCase(
                billId          = billId,
                orderId         = orderId,
                paymentMethod   = state.selectedMethod,
                amount          = state.effectiveAmount,
                referenceNumber = currentReferenceNumber,
                autoProcess     = autoProcess,
                changeAmount    = state.changeAmount,
                notes           = state.notesInput.takeIf { it.isNotBlank() },
            )) {
                is Result.Success -> {
                    val payment = result.data
                    when {
                        payment.status == PaymentStatus.SUCCESS -> {
                            freeTable()
                            _uiState.update { it.copy(isProcessing = false, paymentSuccess = payment) }
                        }
                        payment.status == PaymentStatus.PENDING -> {
                            // CARD two-step — reference stays in ViewModel until confirmed/failed
                            _uiState.update {
                                it.copy(
                                    isProcessing          = false,
                                    pendingPayment        = payment,
                                    showCardConfirmDialog  = true,
                                )
                            }
                        }
                        payment.status == PaymentStatus.FAILED -> {
                            currentReferenceNumber = PaymentReferenceGenerator.generate()
                            _uiState.update {
                                it.copy(isProcessing = false, errorMessage = context.getString(R.string.error_payment_declined))
                            }
                        }
                        else -> _uiState.update {
                            it.copy(isProcessing = false,
                                errorMessage = context.getString(R.string.error_unexpected_payment_status, payment.status.value))
                        }
                    }
                }
                is Result.Failure -> {
                    val isConflict = result.exception is HttpConflictException
                    if (isConflict) {
                        // M-12 / M-17: duplicate referenceNumber — generate fresh one for next attempt
                        currentReferenceNumber = PaymentReferenceGenerator.generate()
                        _uiState.update {
                            it.copy(isProcessing = false,
                                errorMessage = context.getString(R.string.error_payment_conflict))
                        }
                    } else {
                        // Network error — keep reference in ViewModel for retry
                        _uiState.update {
                            it.copy(isProcessing = false,
                                errorMessage = result.exception.message ?: context.getString(R.string.error_payment_failed))
                        }
                    }
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    // ── CARD two-step ────────────────────────────────────────────────────────

    fun dismissCardConfirmDialog() {
        _uiState.update { it.copy(showCardConfirmDialog = false) }
    }

    fun confirmCardPayment() {
        val paymentId = _uiState.value.pendingPayment?.id ?: return
        _uiState.update { it.copy(isConfirmingCard = true, showCardConfirmDialog = false) }
        viewModelScope.launch {
            when (val result = confirmCardPaymentUseCase(paymentId)) {
                is Result.Success -> {
                    freeTable()
                    _uiState.update { it.copy(isConfirmingCard = false, paymentSuccess = result.data) }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isConfirmingCard = false,
                        errorMessage = result.exception.message ?: context.getString(R.string.error_confirm_payment_failed))
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    // ── Free table (M-06 + M-07) ─────────────────────────────────────────────

    /**
     * Explicitly releases the table to AVAILABLE after every successful payment.
     * Backend does NOT auto-release tables on payment (contract §3.2).
     * Failure is logged but suppressed — payment already succeeded, cashier is present.
     */
    private suspend fun freeTable() {
        val rid = restaurantId ?: return
        val result = freeTableUseCase(rid, tableId)
        if (result is Result.Failure) {
            Log.w(TAG, "freeTable failed — best-effort, payment already succeeded", result.exception)
        }
    }

    // ── One-shot consumers ────────────────────────────────────────────────────

    fun onPaymentSuccessConsumed() = _uiState.update { it.copy(paymentSuccess = null) }
    fun onErrorConsumed() = _uiState.update { it.copy(errorMessage = null) }
}
