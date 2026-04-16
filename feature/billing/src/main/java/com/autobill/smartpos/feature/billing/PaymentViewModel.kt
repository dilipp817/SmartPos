package com.autobill.smartpos.feature.billing

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PaymentMethod
import com.autobill.smartpos.domain.model.PaymentStatus
import com.autobill.smartpos.domain.usecase.ConfirmCardPaymentUseCase
import com.autobill.smartpos.domain.usecase.FreeTableUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ProcessPaymentUseCase
import com.autobill.smartpos.domain.util.PaymentReferenceGenerator
import com.autobill.smartpos.feature.billing.R
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
 * ## Payment flow
 * - CASH / UPI / WALLET: single call with autoProcess=true → SUCCESS → navigate out.
 * - CARD: two-step:
 *   1. POST /payments (autoProcess=false) → PENDING → show confirm dialog.
 *   2. User taps Confirm → PATCH /payments/{id}/process → SUCCESS → navigate out.
 *
 * ## After success
 * [FreeTableUseCase] is called to mark the table AVAILABLE.
 * [PaymentUiState.paymentSuccess] one-shot triggers the Route to navigate back.
 *
 * ## Idempotency
 * A [referenceNumber] is generated once at payment initiation and reused on retries.
 * A new one is only generated when the returned status is FAILED.
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val confirmCardPaymentUseCase: ConfirmCardPaymentUseCase,
    private val freeTableUseCase: FreeTableUseCase,
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

    /** Persisted reference number for the current payment attempt. */
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
                        // Auto-processed (CASH/UPI/WALLET) — already SUCCESS
                        payment.status == PaymentStatus.SUCCESS -> {
                            freeTable()
                            _uiState.update { it.copy(isProcessing = false, paymentSuccess = payment) }
                        }
                        // CARD — PENDING — show confirm dialog
                        payment.status == PaymentStatus.PENDING -> {
                            _uiState.update {
                                it.copy(
                                    isProcessing         = false,
                                    pendingPayment       = payment,
                                    showCardConfirmDialog = true,
                                )
                            }
                        }
                        // FAILED — generate new reference for next attempt
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
                is Result.Failure -> _uiState.update {
                    it.copy(isProcessing = false,
                        errorMessage = result.exception.message ?: context.getString(R.string.error_payment_failed))
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

    // ── Free table ────────────────────────────────────────────────────────────

    private suspend fun freeTable() {
        val rid = restaurantId ?: return
        try {
            freeTableUseCase(rid, tableId)
        } catch (e: Exception) {
            Log.w(TAG, "freeTable failed — best-effort, payment already succeeded", e)
        }
    }

    // ── One-shot consumers ────────────────────────────────────────────────────

    fun onPaymentSuccessConsumed() = _uiState.update { it.copy(paymentSuccess = null) }
    fun onErrorConsumed() = _uiState.update { it.copy(errorMessage = null) }
}
