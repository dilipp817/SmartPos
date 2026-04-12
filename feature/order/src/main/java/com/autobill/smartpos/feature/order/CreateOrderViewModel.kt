package com.autobill.smartpos.feature.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.CreateOrderUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetTableByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Create Order confirmation screen.
 *
 * Receives [tableId] via [SavedStateHandle] (nav arg).
 *
 * Responsibilities:
 *  1. Fetch table details from cache ([GetTableByIdUseCase]).
 *  2. Observe local cart ([GetCartUseCase]).
 *  3. On [placeOrder] → POST /orders → clear cart → emit [CreateOrderUiState.orderCreated].
 *  4. On 409 → set [CreateOrderUiState.tableConflict] so the Route navigates back to TableList.
 */
@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getTableByIdUseCase: GetTableByIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    private val tableId: Long = checkNotNull(savedStateHandle["tableId"]) {
        "tableId nav-arg is required for CreateOrderViewModel"
    }

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null

    init {
        // Observe cart items live — so quantity changes on HomeScreen are reflected here
        getCartUseCase()
            .onEach { items -> _uiState.update { it.copy(cartItems = items) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isTableLoading = false,
                        errorMessage = "Session error — please log in again.",
                    )
                }
                return@launch
            }
            loadTable()
        }
    }

    // ── User interactions ────────────────────────────────────────────────────

    fun selectOrderType(type: OrderType) {
        _uiState.update { it.copy(orderType = type, errorMessage = null) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value) }
    }

    /**
     * POST /restaurants/{restaurantId}/orders
     *
     * On success  → [ClearCartUseCase] then set [CreateOrderUiState.orderCreated].
     * On 409      → set [tableConflict] = true (user must go back and pick a new table).
     * Other error → show [errorMessage] inline.
     */
    fun placeOrder() {
        val rid = restaurantId ?: return
        val state = _uiState.value
        if (!state.canPlaceOrder) return

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null, tableConflict = false) }

        viewModelScope.launch {
            val lineItems = state.cartItems.map {
                OrderLineItem(foodId = it.foodId, quantity = it.quantity)
            }
            when (val result = createOrderUseCase(
                restaurantId = rid,
                tableId      = tableId,
                cartItems    = lineItems,
                orderType    = state.orderType,
                notes        = state.notes.trim().takeIf { it.isNotEmpty() },
            )) {
                is Result.Success -> {
                    clearCartUseCase()
                    _uiState.update { it.copy(isSubmitting = false, orderCreated = result.data.id) }
                }
                is Result.Failure -> {
                    val isConflict = result.exception is HttpConflictException
                    _uiState.update {
                        it.copy(
                            isSubmitting  = false,
                            tableConflict = isConflict,
                            errorMessage  = if (isConflict) null
                                           else result.exception.message
                                               ?: "Failed to place order. Please try again.",
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    /** Consume the one-shot orderCreated event after navigation has fired. */
    fun onOrderCreatedConsumed() {
        _uiState.update { it.copy(orderCreated = null) }
    }

    /** Consume the tableConflict event after navigation back to TableList. */
    fun onTableConflictConsumed() {
        _uiState.update { it.copy(tableConflict = false) }
    }

    // ── Private ──────────────────────────────────────────────────────────────

    private suspend fun loadTable() {
        val rid = restaurantId ?: return
        when (val result = getTableByIdUseCase(rid, tableId)) {
            is Result.Success -> _uiState.update {
                it.copy(table = result.data, isTableLoading = false)
            }
            is Result.Failure -> _uiState.update {
                it.copy(
                    isTableLoading = false,
                    errorMessage = "Could not load table details: ${result.exception.message}",
                )
            }
            Result.Loading -> Unit
        }
    }
}





