package com.autobill.smartpos.feature.order

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.HttpConflictException
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.OrderItem
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.domain.model.RealTimeEvent
import com.autobill.smartpos.domain.usecase.AddItemToOrderUseCase
import com.autobill.smartpos.domain.usecase.CancelOrderUseCase
import com.autobill.smartpos.domain.usecase.GetOrderByIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.ObserveOrderEventsUseCase
import com.autobill.smartpos.domain.usecase.ObserveRolePermissionsUseCase
import com.autobill.smartpos.domain.usecase.PrintBillUseCase
import com.autobill.smartpos.domain.printer.PrintJobFactory
import com.autobill.smartpos.domain.printer.PrintError
import com.autobill.smartpos.domain.printer.toPrintError
import com.autobill.smartpos.domain.usecase.RemoveItemFromOrderUseCase
import com.autobill.smartpos.domain.usecase.SearchFoodsUseCase
import com.autobill.smartpos.domain.usecase.UpdateOrderItemUseCase
import com.autobill.smartpos.domain.usecase.UpdateOrderStatusUseCase
import com.autobill.smartpos.feature.order.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Order Detail screen.
 *
 * Responsibilities:
 *  - Fetch full order (with items) via [GetOrderByIdUseCase]
 *  - Update order status with optimistic UI update + rollback on failure
 *  - Add / edit / remove items (PENDING or HOLD orders only)
 *  - Cancel order (role-gated via canCancelOrders in OrderDetailUiState)
 *  - Handle 409 CONFLICT: re-fetch fresh order → retry once → conflict message if still failing
 *  - Expose role permissions from [ObserveRolePermissionsUseCase]
 *
 * [orderId] is injected via [SavedStateHandle] from the nav argument.
 */
@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val addItemToOrderUseCase: AddItemToOrderUseCase,
    private val updateOrderItemUseCase: UpdateOrderItemUseCase,
    private val removeItemFromOrderUseCase: RemoveItemFromOrderUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    private val searchFoodsUseCase: SearchFoodsUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val printBillUseCase: PrintBillUseCase,
    private val printJobFactory: PrintJobFactory,
    observeRolePermissionsUseCase: ObserveRolePermissionsUseCase,
    private val observeOrderEventsUseCase: ObserveOrderEventsUseCase,
) : ViewModel() {

    private val orderId: Long = checkNotNull(savedStateHandle["orderId"]) {
        "orderId nav-arg is required for OrderDetailViewModel"
    }

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null
    private var foodSearchJob: Job? = null

    init {
        observeRolePermissionsUseCase()
            .onEach { perms ->
                _uiState.update { it.copy(canCancelOrders = perms.canCancelOrders) }
            }
            .launchIn(viewModelScope)

        // Phase 9.1: keep the detail screen in sync with KDS item-status changes
        observeRealTimeEvents()

        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = context.getString(R.string.error_no_restaurant_session),
                    )
                }
                return@launch
            }
            loadOrder()
        }
    }

    // ── Phase 9.1: Real-time order updates ───────────────────────────────────

    /**
     * Subscribes to WebSocket ORDER_UPDATED and ORDER_ITEM_UPDATED events.
     *
     * The update is applied in-place only when:
     *  - The event is for THIS order (matched by [orderId])
     *  - No local mutation (add/edit/remove item or status update) is in-flight
     *    — avoids overwriting optimistic state mid-operation.
     *
     * This keeps the detail screen live when a kitchen display marks items
     * READY or another device changes the order status.
     */
    private fun observeRealTimeEvents() {
        observeOrderEventsUseCase()
            .onEach { event ->
                val updatedOrder = when (event) {
                    is RealTimeEvent.OrderUpdated     -> event.order
                    is RealTimeEvent.OrderItemUpdated -> event.order
                    else                              -> return@onEach
                }
                if (updatedOrder.id != orderId) return@onEach

                val state = _uiState.value
                val mutationInFlight = state.isUpdatingStatus ||
                    state.isAddingItem  ||
                    state.isEditingItem ||
                    state.removingItemIds.isNotEmpty() ||
                    state.isCancelling
                if (mutationInFlight) return@onEach

                _uiState.update { it.copy(order = updatedOrder) }
            }
            .launchIn(viewModelScope)
    }

    // ── Fetch ────────────────────────────────────────────────────────────────

    /** Pull-to-refresh — keeps existing order visible while re-fetching. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            loadOrder()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ── Status update ────────────────────────────────────────────────────────

    /**
     * PATCH /orders/{orderId}/status
     *
     * Optimistically updates the displayed status, then confirms with the server.
     * On 409 → calls [loadOrder] (gets fresh version) → retries once.
     * If the retry also 409s → rolls back + sets conflictMessage in OrderDetailUiState.
     */
    fun updateStatus(newStatus: OrderStatus) {
        val rid = restaurantId ?: return
        val currentOrder = _uiState.value.order ?: return
        _uiState.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
        // Optimistic update
        _uiState.update { it.copy(order = it.order?.copy(status = newStatus)) }

        viewModelScope.launch {
            val result = withConflictRetry {
                updateOrderStatusUseCase(rid, currentOrder.id, newStatus)
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        order            = result.data,
                        isUpdatingStatus = false,
                        successMessage   = context.getString(R.string.order_status_updated_to, result.data.status.value),
                    )
                }
                is Result.Failure -> {
                    // Roll back optimistic update
                    _uiState.update {
                        it.copy(
                            order            = it.order?.copy(status = currentOrder.status),
                            isUpdatingStatus = false,
                            errorMessage     = result.exception.message
                                ?: context.getString(R.string.error_update_order_status_failed),
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── Add Item dialog ──────────────────────────────────────────────────────

    fun showAddItemDialog() {
        _uiState.update { it.copy(addItemDialog = AddItemDialogState(), addItemError = null) }
    }

    fun dismissAddItemDialog() {
        foodSearchJob?.cancel()
        _uiState.update { it.copy(addItemDialog = null, addItemError = null) }
    }

    /** Debounced 300 ms food search; clears selection when the query changes. */
    fun onAddItemFoodSearch(query: String) {
        _uiState.update { state ->
            state.copy(
                addItemDialog = state.addItemDialog?.copy(
                    searchQuery  = query,
                    selectedFood = null,
                    foodResults  = emptyList(),
                ),
            )
        }
        foodSearchJob?.cancel()
        if (query.isBlank()) return
        foodSearchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { state ->
                state.copy(addItemDialog = state.addItemDialog?.copy(isSearching = true))
            }
            when (val result = searchFoodsUseCase(query.trim(), restaurantId)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        addItemDialog = state.addItemDialog?.copy(
                            foodResults = result.data,
                            isSearching = false,
                        ),
                    )
                }
                is Result.Failure -> _uiState.update { state ->
                    state.copy(
                        addItemDialog = state.addItemDialog?.copy(isSearching = false),
                        addItemError  = result.exception.message ?: context.getString(R.string.error_food_search_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    fun onAddItemFoodSelected(food: com.autobill.smartpos.domain.model.Food) {
        _uiState.update { state ->
            state.copy(
                addItemDialog = state.addItemDialog?.copy(
                    selectedFood = food,
                    foodResults  = emptyList(),
                    searchQuery  = food.name,
                ),
            )
        }
    }

    fun onAddItemQuantityChange(delta: Int) {
        _uiState.update { state ->
            val current = state.addItemDialog?.quantity ?: 1
            val next = (current + delta).coerceAtLeast(1)
            state.copy(addItemDialog = state.addItemDialog?.copy(quantity = next))
        }
    }

    fun onAddItemSpecialRequestsChange(text: String) {
        _uiState.update { state ->
            state.copy(addItemDialog = state.addItemDialog?.copy(specialRequests = text))
        }
    }

    /** POST /orders/{orderId}/items */
    fun confirmAddItem() {
        val rid = restaurantId ?: return
        val dialog = _uiState.value.addItemDialog ?: return
        val food = dialog.selectedFood ?: run {
            _uiState.update { it.copy(addItemError = context.getString(R.string.error_select_food_first)) }
            return
        }
        _uiState.update { it.copy(isAddingItem = true, addItemError = null) }

        viewModelScope.launch {
            val result = withConflictRetry {
                addItemToOrderUseCase(
                    restaurantId    = rid,
                    orderId         = orderId,
                    foodId          = food.id,
                    quantity        = dialog.quantity,
                    specialRequests = dialog.specialRequests.ifBlank { null },
                )
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        order          = result.data,
                        addItemDialog  = null,
                        isAddingItem   = false,
                        addItemError   = null,
                        successMessage = context.getString(R.string.order_item_added_success, food.name),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isAddingItem = false,
                        addItemError = result.exception.message ?: context.getString(R.string.error_add_item_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── Edit Item dialog ─────────────────────────────────────────────────────

    fun showEditItemDialog(item: OrderItem) {
        _uiState.update {
            it.copy(editItemDialog = EditItemDialogState(item = item), editItemError = null)
        }
    }

    fun dismissEditItemDialog() {
        _uiState.update { it.copy(editItemDialog = null, editItemError = null) }
    }

    fun onEditItemQuantityChange(delta: Int) {
        _uiState.update { state ->
            val current = state.editItemDialog?.quantity ?: 1
            val next = (current + delta).coerceAtLeast(1)
            state.copy(editItemDialog = state.editItemDialog?.copy(quantity = next))
        }
    }

    fun onEditItemSpecialRequestsChange(text: String) {
        _uiState.update { state ->
            state.copy(editItemDialog = state.editItemDialog?.copy(specialRequests = text))
        }
    }

    /** PUT /orders/{orderId}/items/{itemId} */
    fun confirmEditItem() {
        val rid = restaurantId ?: return
        val dialog = _uiState.value.editItemDialog ?: return
        _uiState.update { it.copy(isEditingItem = true, editItemError = null) }

        viewModelScope.launch {
            val result = withConflictRetry {
                updateOrderItemUseCase(
                    restaurantId    = rid,
                    orderId         = orderId,
                    itemId          = dialog.item.id,
                    quantity        = dialog.quantity,
                    specialRequests = dialog.specialRequests.ifBlank { null },
                )
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        order          = result.data,
                        editItemDialog = null,
                        isEditingItem  = false,
                        editItemError  = null,
                        successMessage = context.getString(R.string.order_item_updated_success),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isEditingItem = false,
                        editItemError = result.exception.message ?: context.getString(R.string.error_update_item_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── Remove Item ──────────────────────────────────────────────────────────

    /** DELETE /orders/{orderId}/items/{itemId} */
    fun removeItem(itemId: Long) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(removingItemIds = it.removingItemIds + itemId) }

        viewModelScope.launch {
            val result = withConflictRetry {
                removeItemFromOrderUseCase(rid, orderId, itemId)
            }
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        order           = result.data,
                        removingItemIds = it.removingItemIds - itemId,
                        successMessage  = context.getString(R.string.order_item_removed_success),
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        removingItemIds = it.removingItemIds - itemId,
                        errorMessage    = result.exception.message ?: context.getString(R.string.error_remove_item_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── Cancel Order ─────────────────────────────────────────────────────────

    fun showCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun dismissCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    /** DELETE /orders/{orderId} */
    fun confirmCancelOrder() {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isCancelling = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = cancelOrderUseCase(rid, orderId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        order           = result.data,
                        showCancelDialog = false,
                        isCancelling    = false,
                        orderCancelled  = true,
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        showCancelDialog = false,
                        isCancelling    = false,
                        errorMessage    = result.exception.message ?: context.getString(R.string.error_cancel_order_failed),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── One-shot event consumers ─────────────────────────────────────────────

    fun onOrderCancelledConsumed() {
        _uiState.update { it.copy(orderCancelled = false) }
    }

    fun onSuccessMessageConsumed() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun onConflictMessageConsumed() {
        _uiState.update { it.copy(conflictMessage = null) }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Fetches the order from the network (network-first, Room fallback).
     * Shared by the init block, [refresh], and the 409 retry helper.
     */
    private suspend fun loadOrder() {
        val rid = restaurantId ?: return
        when (val result = getOrderByIdUseCase(rid, orderId)) {
            is Result.Success -> _uiState.update {
                it.copy(order = result.data, isLoading = false, errorMessage = null)
            }
            is Result.Failure -> _uiState.update {
                it.copy(
                    isLoading    = false,
                    errorMessage = result.exception.message ?: context.getString(R.string.error_load_order_failed),
                )
            }
            Result.Loading -> Unit
        }
    }

    /**
     * 409 retry helper — same optimistic-lock pattern used in TableRepositoryImpl.
     *
     * Strategy:
     *  1. Execute [operation].
     *  2. If result is [HttpConflictException] → re-fetch the order (gets fresh version).
     *  3. Retry [operation] once.
     *  4. If still 409 → return the failure and set conflictMessage in OrderDetailUiState.
     */
    private suspend fun <T> withConflictRetry(
        operation: suspend () -> Result<T>,
    ): Result<T> {
        val firstResult = operation()
        if (firstResult !is Result.Failure) return firstResult
        if (firstResult.exception !is HttpConflictException) return firstResult

        // First attempt got 409 — re-fetch to get the latest order state, then retry
        loadOrder()
        val retryResult = operation()
        if (retryResult is Result.Failure && retryResult.exception is HttpConflictException) {
            _uiState.update {
                it.copy(
                    conflictMessage = context.getString(R.string.error_order_conflict),
                )
            }
        }
        return retryResult
    }

    // ── Print ─────────────────────────────────────────────────────────────────

    /** Print a receipt for the current order using estimated CGST + SGST (no bill needed). */
    fun printOrder() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.isPrinting) return
        _uiState.update { it.copy(isPrinting = true, printResultMessage = null) }
        viewModelScope.launch {
            val job = printJobFactory.fromOrder(order)
            when (val result = printBillUseCase(job)) {
                is Result.Success ->
                    _uiState.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_success)) }
                is Result.Failure -> {
                    when (val err = result.exception.toPrintError()) {
                        PrintError.NoPrinterConfigured ->
                            _uiState.update { it.copy(isPrinting = false, navigateToPrinterSettings = true) }
                        PrintError.BluetoothDisabled ->
                            _uiState.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_error_bluetooth_disabled)) }
                        PrintError.ConnectionFailed ->
                            _uiState.update { it.copy(isPrinting = false, printResultMessage = context.getString(R.string.print_error_connection_failed)) }
                        is PrintError.Unknown ->
                            _uiState.update { it.copy(isPrinting = false, printResultMessage = err.message) }
                    }
                }
                else -> Unit
            }
        }
    }

    fun onPrintResultConsumed() = _uiState.update { it.copy(printResultMessage = null) }
    fun onNavigateToPrinterSettingsConsumed() = _uiState.update { it.copy(navigateToPrinterSettings = false) }
}
