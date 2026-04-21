package com.autobill.smartpos.feature.food

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.OfflineQueuedException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.printer.PrintError
import com.autobill.smartpos.domain.printer.PrintJobFactory
import com.autobill.smartpos.domain.printer.toPrintError
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.CreateOrderUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.PrintBillUseCase
import com.autobill.smartpos.feature.food.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel: Place Order (no-table)
 *
 * Single responsibility: submit a TAKEAWAY or counter-service order directly
 * from the home screen without navigating away.
 *
 * Reuses the same domain use cases as [CreateOrderViewModel] (DINE_IN flow) —
 * no logic is duplicated. The only difference is tableId is always null here
 * because no table selection is involved.
 *
 * Separation of concerns:
 *  - [CartViewModel]       → cart state (items, totals, held bills)
 *  - [PlaceOrderViewModel] → order submission (this class)
 *  - [FoodViewModel]       → food list pagination + feature flags
 */
@HiltViewModel
class PlaceOrderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val printBillUseCase: PrintBillUseCase,
    private val printJobFactory: PrintJobFactory,
) : ViewModel() {

    private val _state = MutableStateFlow(PlaceOrderState())
    val state: StateFlow<PlaceOrderState> = _state.asStateFlow()

    /**
     * Submit a no-table order (TAKEAWAY or TABLE_MANAGEMENT=false).
     * tableId is always null — no table reservation involved.
     *
     * On success  → cart is cleared; [PlaceOrderState.isSubmitting] resets to false.
     * On offline  → order enqueued; cart cleared; [PlaceOrderState.isSubmitting] resets.
     * On error    → [PlaceOrderState.errorMessage] set; cart unchanged.
     */
    fun placeOrder(orderType: OrderType) {
        if (_state.value.isSubmitting) return

        // Set isSubmitting synchronously — before any suspension point — so rapid
        // double-taps cannot launch a second coroutine while the first is in-flight.
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val restaurantId = getRestaurantIdUseCase() ?: run {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }
            val cartItems = getCartUseCase().first()
            if (cartItems.isEmpty()) {
                _state.update { it.copy(isSubmitting = false) }
                return@launch
            }

            val lineItems = cartItems.map { OrderLineItem(foodId = it.foodId, quantity = it.quantity) }

            when (val result = createOrderUseCase(
                restaurantId = restaurantId,
                tableId      = null,
                cartItems    = lineItems,
                orderType    = orderType,
                notes        = null,
            )) {
                is Result.Success -> {
                    clearCartUseCase()
                    _state.update { it.copy(isSubmitting = false, orderPlaced = true, lastOrder = result.data) }
                }
                is Result.Failure -> {
                    if (result.exception is OfflineQueuedException) {
                        clearCartUseCase()
                        _state.update { it.copy(isSubmitting = false, orderPlaced = true) }
                    } else {
                        _state.update {
                            it.copy(
                                isSubmitting = false,
                                errorMessage = result.exception.message
                                    ?: "Failed to place order. Please try again.",
                            )
                        }
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    /** Print a receipt for the most recently placed order. */
    fun printLastOrder() {
        val order = _state.value.lastOrder ?: return
        if (_state.value.isPrinting) return
        _state.update { it.copy(isPrinting = true) }
        viewModelScope.launch {
            val job = printJobFactory.fromOrder(order)
            when (val result = printBillUseCase(job)) {
                is Result.Success ->
                    _state.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_success)) }
                is Result.Failure -> {
                    when (val err = result.exception.toPrintError()) {
                        PrintError.NoPrinterConfigured ->
                            _state.update { it.copy(isPrinting = false, navigateToPrinterSettings = true) }
                        PrintError.BluetoothDisabled ->
                            _state.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_error_bluetooth_disabled)) }
                        PrintError.ConnectionFailed ->
                            _state.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_error_connection_failed)) }
                        is PrintError.Unknown ->
                            _state.update { it.copy(isPrinting = false, printResultMessage = err.message) }
                    }
                }
                else -> Unit
            }
        }
    }

    fun onOrderPlacedConsumed() = _state.update { it.copy(orderPlaced = false) }
    fun onPrintResultConsumed() = _state.update { it.copy(printResultMessage = null) }
    fun onNavigateToPrinterSettingsConsumed() = _state.update { it.copy(navigateToPrinterSettings = false) }
    fun clearError()            = _state.update { it.copy(errorMessage = null) }
}

/**
 * UI state for [PlaceOrderViewModel].
 * Cart clearing is the primary success signal; [orderPlaced] is a one-shot
 * used only to trigger the snackbar — not to show a blocking dialog.
 */
data class PlaceOrderState(
    /** True while the order POST is in-flight — disables the Place Order button. */
    val isSubmitting: Boolean = false,
    /** One-shot: true when order is successfully placed or queued offline. Triggers snackbar. */
    val orderPlaced:  Boolean = false,
    /** Non-null when the order fails — shown as an error snackbar. */
    val errorMessage: String? = null,
    /** The most recently placed order — kept for printing the receipt. */
    val lastOrder: com.autobill.smartpos.domain.model.Order? = null,
    /** True while print is in-flight. */
    val isPrinting: Boolean = false,
    /** One-shot: non-null after a print attempt. */
    val printResultMessage: String? = null,
    /** One-shot: true when print fails because no printer is configured → navigate to Settings. */
    val navigateToPrinterSettings: Boolean = false,
)

