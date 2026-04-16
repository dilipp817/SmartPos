package com.autobill.smartpos.feature.order

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.OfflineQueuedException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderType
import com.autobill.smartpos.domain.repository.OrderLineItem
import com.autobill.smartpos.domain.usecase.ClearCartUseCase
import com.autobill.smartpos.domain.usecase.CreateOrderUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetTableByIdUseCase
import com.autobill.smartpos.domain.usecase.ObserveConnectivityUseCase
import com.autobill.smartpos.feature.order.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
 * Phase 9.2 additions:
 *  - Observes [ObserveConnectivityUseCase] → drives [CreateOrderUiState.isOffline] banner.
 *  - On [placeOrder] offline → [CreateOrderUiState.orderQueued] triggers "queued" state.
 */
@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getTableByIdUseCase: GetTableByIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val observeConnectivityUseCase: ObserveConnectivityUseCase,
) : ViewModel() {

    private val tableId: Long = checkNotNull(savedStateHandle["tableId"]) {
        "tableId nav-arg is required for CreateOrderViewModel"
    }

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null

    init {
        // Observe cart items live
        getCartUseCase()
            .onEach { items -> _uiState.update { it.copy(cartItems = items) } }
            .launchIn(viewModelScope)

        // Phase 9.2: observe connectivity for the offline banner
        observeConnectivityUseCase()
            .onEach { isOnline -> _uiState.update { it.copy(isOffline = !isOnline) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isTableLoading = false,
                        errorMessage = context.getString(R.string.error_session_expired),
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
     * POST /restaurants/{restaurantId}/orders  (online)
     * or  → offline queue                      (offline)
     *
     * On success     → [ClearCartUseCase] then [CreateOrderUiState.orderCreated].
     * On queued      → [ClearCartUseCase] then [CreateOrderUiState.orderQueued].
     * On 409         → [tableConflict] = true.
     * Other error    → [errorMessage] inline.
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
                    when (result.exception) {
                        is OfflineQueuedException -> {
                            // Order saved locally — clear cart and show "queued" state
                            clearCartUseCase()
                            _uiState.update { it.copy(isSubmitting = false, orderQueued = true) }
                        }
                        is HttpConflictException -> {
                            _uiState.update {
                                it.copy(isSubmitting = false, tableConflict = true)
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = result.exception.message
                                        ?: context.getString(R.string.error_place_order_failed),
                                )
                            }
                        }
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

    /** Consume the orderQueued event — called by Route before navigating back. */
    fun onOrderQueuedConsumed() {
        _uiState.update { it.copy(orderQueued = false) }
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
                    errorMessage = context.getString(R.string.error_load_table_details, result.exception.message ?: ""),
                )
            }
            Result.Loading -> Unit
        }
    }
}







