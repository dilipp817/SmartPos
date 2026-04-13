package com.autobill.smartpos.feature.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.usecase.GetActiveOrdersUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.UpdateItemStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Kitchen Display Screen.
 *
 * Responsibilities:
 *  - Fetch all active orders (not DELIVERED / CANCELLED) via [GetActiveOrdersUseCase]
 *  - Auto-refresh every 30 seconds so the kitchen always sees the latest state
 *  - Update individual item status (PENDING → IN_PROGRESS → READY → SERVED | CANCELLED)
 *    via [UpdateItemStatusUseCase]
 *  - Track which items have an in-flight PATCH (per-row spinner in [updatingItemIds])
 *  - Filter display by [KitchenDisplayFilter] (All Active / Pending / In Progress / Ready)
 *
 * Auto-refresh is cancelled when the ViewModel is cleared (screen leaves composition).
 */
@HiltViewModel
class KitchenDisplayViewModel @Inject constructor(
    private val getActiveOrdersUseCase: GetActiveOrdersUseCase,
    private val updateItemStatusUseCase: UpdateItemStatusUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(KitchenDisplayUiState())
    val uiState: StateFlow<KitchenDisplayUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null
    private var autoRefreshJob: Job? = null

    companion object {
        /** Kitchen Display auto-refresh interval — 30 seconds. */
        private const val AUTO_REFRESH_INTERVAL_MS = 30_000L
    }

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "No restaurant assigned to this account. Please log in again.",
                    )
                }
                return@launch
            }
            loadOrders()
            startAutoRefresh()
        }
    }

    // ── Fetch ────────────────────────────────────────────────────────────────

    /** Manual pull-to-refresh — keeps existing orders visible while re-fetching. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch {
            loadOrders()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    // ── Filter ───────────────────────────────────────────────────────────────

    fun selectFilter(filter: KitchenDisplayFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // ── Item status update ────────────────────────────────────────────────────

    /**
     * PATCH /orders/{orderId}/items/{itemId}/status
     *
     * Shows a per-row spinner while in-flight via [updatingItemIds].
     * On success: replaces the order in [orders] list with the fresh server copy.
     * On failure: shows [errorMessage] snackbar.
     */
    fun updateItemStatus(orderId: Long, itemId: Long, newStatus: ItemStatus) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(updatingItemIds = it.updatingItemIds + itemId) }

        viewModelScope.launch {
            when (val result = updateItemStatusUseCase(rid, orderId, itemId, newStatus)) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        orders          = state.orders.map { if (it.id == orderId) result.data else it },
                        updatingItemIds = state.updatingItemIds - itemId,
                        successMessage  = "Item marked as ${newStatus.value.replace("_", " ")}",
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        updatingItemIds = it.updatingItemIds - itemId,
                        errorMessage    = result.exception.message ?: "Failed to update item status.",
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    // ── One-shot event consumers ──────────────────────────────────────────────

    fun onSuccessMessageConsumed() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun onErrorConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun loadOrders() {
        val rid = restaurantId ?: return
        when (val result = getActiveOrdersUseCase(rid)) {
            is Result.Success -> _uiState.update {
                it.copy(orders = result.data, isLoading = false, errorMessage = null)
            }
            is Result.Failure -> _uiState.update {
                it.copy(
                    isLoading    = false,
                    errorMessage = result.exception.message ?: "Failed to load orders.",
                )
            }
            Result.Loading -> Unit
        }
    }

    /**
     * Silently refreshes orders every [AUTO_REFRESH_INTERVAL_MS].
     * Does not set [isRefreshing] — no spinner for background ticks.
     * Cancelled automatically when the ViewModel is cleared.
     */
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                val rid = restaurantId ?: break
                when (val result = getActiveOrdersUseCase(rid)) {
                    is Result.Success -> _uiState.update { it.copy(orders = result.data) }
                    else              -> Unit  // silent — don't interrupt kitchen on auto-refresh error
                }
            }
        }
    }
}
