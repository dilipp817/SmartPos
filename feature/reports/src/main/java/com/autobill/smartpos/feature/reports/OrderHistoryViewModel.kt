package com.autobill.smartpos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.usecase.GetOrdersByDateRangeUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Order History screen (Phase 8.2).
 *
 * Responsibilities:
 *  - Resolve [restaurantId] from session
 *  - Fetch orders for the selected date range
 *  - Apply client-side [OrderHistoryFilter] without re-fetching
 *  - Support pull-to-refresh
 */
@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getOrdersByDateRangeUseCase: GetOrdersByDateRangeUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    /** Full unfiltered list kept in memory so filter changes don't require a network call. */
    private var allOrders: List<Order> = emptyList()

    private var restaurantId: Long? = null

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading    = false,
                        errorMessage = "No restaurant session found. Please log in again.",
                    )
                }
            } else {
                loadOrders(isRefresh = false)
            }
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────

    fun showStartPicker()    { _uiState.update { it.copy(showStartPicker = true) } }
    fun showEndPicker()      { _uiState.update { it.copy(showEndPicker   = true) } }
    fun dismissStartPicker() { _uiState.update { it.copy(showStartPicker = false) } }
    fun dismissEndPicker()   { _uiState.update { it.copy(showEndPicker   = false) } }

    fun onStartDateSelected(ms: Long) {
        _uiState.update { it.copy(startDateMs = ms, showStartPicker = false) }
    }

    fun onEndDateSelected(ms: Long) {
        _uiState.update { it.copy(endDateMs = ms, showEndPicker = false) }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    fun onFilterSelected(filter: OrderHistoryFilter) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                orders         = allOrders.applyFilter(filter),
            )
        }
    }

    // ── Load / Refresh ────────────────────────────────────────────────────────

    /** Triggered by the "Load" button or pull-to-refresh. */
    fun loadOrders(isRefresh: Boolean = true) {
        val rid = restaurantId ?: return
        if (isRefresh) {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        } else {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }
        viewModelScope.launch {
            val start = _uiState.value.startDateMs.toIsoDateString(endOfDay = false)
            val end   = _uiState.value.endDateMs.toIsoDateString(endOfDay = true)
            when (val result = getOrdersByDateRangeUseCase(rid, start, end)) {
                is Result.Success -> {
                    allOrders = result.data
                    val filtered = allOrders.applyFilter(_uiState.value.selectedFilter)
                    _uiState.update {
                        it.copy(
                            isLoading    = false,
                            isRefreshing = false,
                            orders       = filtered,
                            errorMessage = null,
                        )
                    }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isLoading    = false,
                        isRefreshing = false,
                        errorMessage = result.exception.message ?: "Failed to load order history",
                    )
                }
                Result.Loading    -> Unit
            }
        }
    }

    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
}

