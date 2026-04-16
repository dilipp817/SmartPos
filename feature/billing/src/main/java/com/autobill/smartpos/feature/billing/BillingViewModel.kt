package com.autobill.smartpos.feature.billing

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.CancelBillUseCase
import com.autobill.smartpos.domain.usecase.GenerateBillUseCase
import com.autobill.smartpos.domain.usecase.GetBillByIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
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
 * ViewModel for the Billing screen.
 *
 * Receives [orderId] via [SavedStateHandle].
 *
 * Responsibilities:
 *  1. Accept optional discount input from user.
 *  2. On [generateBill] → POST .../generate-bill?discount=... (server computes all tax).
 *  3. On 409 CONFLICT → bill already exists → [BillingUiState.billAlreadyExists] one-shot.
 *     Route then fetches the existing bill by ID and passes it back via [setBillFromExisting].
 *  4. On [cancelBill] → PATCH /bills/{id}/cancel (ISSUED only).
 *  5. On [proceedToPayment] → emits [BillingUiState.navigateToPayment] one-shot.
 */
@HiltViewModel
class BillingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val generateBillUseCase: GenerateBillUseCase,
    private val getBillByIdUseCase: GetBillByIdUseCase,
    private val cancelBillUseCase: CancelBillUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"]) {
        "orderId nav-arg is required for BillingViewModel"
    }
    private val tableId: Long = checkNotNull(savedStateHandle["tableId"]) {
        "tableId nav-arg is required for BillingViewModel"
    }

    private val _uiState = MutableStateFlow(BillingUiState(orderId = orderId, tableId = tableId))
    val uiState: StateFlow<BillingUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(errorMessage = context.getString(R.string.error_session_expired))
                }
            } else {
                _uiState.update { it.copy(restaurantId = restaurantId!!) }
            }
        }
    }

    // ── User interactions ────────────────────────────────────────────────────

    fun onDiscountInputChange(value: String) {
        // Allow empty string, digits, and a single decimal point
        val filtered = value.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(discountInput = filtered, discountError = null) }
    }

    fun generateBill() {
        val rid = restaurantId ?: run {
            _uiState.update { it.copy(errorMessage = context.getString(R.string.error_session_expired)) }
            return
        }
        val discount = _uiState.value.discountInput.toDoubleOrNull() ?: 0.0
        if (discount < 0) {
            _uiState.update { it.copy(discountError = context.getString(R.string.error_discount_negative)) }
            return
        }

        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = generateBillUseCase(rid, orderId, discount)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isGenerating = false, bill = result.data) }
                }
                is Result.Failure -> {
                    if (result.exception is HttpConflictException) {
                        _uiState.update { it.copy(isGenerating = false, billAlreadyExists = true) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                errorMessage = result.exception.message
                                    ?: context.getString(R.string.error_generate_bill_failed),
                            )
                        }
                    }
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    /** Called by the Route after it fetches the pre-existing bill on 409. */
    fun setBillFromExisting(bill: com.autobill.smartpos.domain.model.Bill) {
        _uiState.update { it.copy(bill = bill, billAlreadyExists = false) }
    }

    fun onBillAlreadyExistsConsumed() {
        _uiState.update { it.copy(billAlreadyExists = false) }
    }

    // ── Fetch existing bill by ID (e.g. re-entering the screen) ─────────────

    fun loadBillById(billId: Long) {
        _uiState.update { it.copy(isLoadingExistingBill = true) }
        viewModelScope.launch {
            when (val result = getBillByIdUseCase(billId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoadingExistingBill = false, bill = result.data)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isLoadingExistingBill = false,
                        errorMessage = result.exception.message ?: context.getString(R.string.error_load_bill_failed),
                    )
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    // ── Cancel bill ──────────────────────────────────────────────────────────

    fun showCancelDialog() = _uiState.update { it.copy(showCancelDialog = true) }
    fun dismissCancelDialog() = _uiState.update { it.copy(showCancelDialog = false) }

    fun confirmCancelBill() {
        val billId = _uiState.value.bill?.id ?: return
        _uiState.update { it.copy(isCancelling = true, showCancelDialog = false) }
        viewModelScope.launch {
            when (val result = cancelBillUseCase(billId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isCancelling = false,
                        bill = result.data,
                        successMessage = context.getString(R.string.billing_cancel_success),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isCancelling = false,
                        errorMessage = result.exception.message ?: context.getString(R.string.error_cancel_bill_failed),
                    )
                }
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    fun proceedToPayment() {
        _uiState.update { it.copy(navigateToPayment = true) }
    }

    fun onNavigateToPaymentConsumed() {
        _uiState.update { it.copy(navigateToPayment = false) }
    }

    // ── One-shot consumers ───────────────────────────────────────────────────

    fun onSuccessMessageConsumed() = _uiState.update { it.copy(successMessage = null) }
    fun onErrorConsumed() = _uiState.update { it.copy(errorMessage = null) }
}
