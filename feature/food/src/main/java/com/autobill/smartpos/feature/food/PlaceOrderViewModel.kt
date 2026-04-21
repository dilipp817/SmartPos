package com.autobill.smartpos.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.OfflineQueuedException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.CreateOrderUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
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

        viewModelScope.launch {
            val restaurantId = getRestaurantIdUseCase() ?: return@launch
            val cartItems    = getCartUseCase().first()
            if (cartItems.isEmpty()) return@launch

            _state.update { it.copy(isSubmitting = true, errorMessage = null) }

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
                    _state.update { it.copy(isSubmitting = false, orderPlaced = true) }
                }
                is Result.Failure -> {
                    if (result.exception is OfflineQueuedException) {
                        clearCartUseCase()
                        _state.update { it.copy(isSubmitting = false, orderPlaced = true) }
                    } else {
                        _state.update {
                            it.copy(isSubmitting = false, errorMessage = result.exception.message)
                        }
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    fun onOrderPlacedConsumed() = _state.update { it.copy(orderPlaced = false) }
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
)

