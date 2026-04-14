package com.autobill.smartpos.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetSalesReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Sales Report screen (Phase 8.1).
 *
 * Responsibilities:
 *  - Resolve [restaurantId] from session on init
 *  - Trigger report load for the selected date range
 *  - Expose date picker visibility state
 *  - Default date range = today
 */
@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val getSalesReportUseCase: GetSalesReportUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesReportUiState())
    val uiState: StateFlow<SalesReportUiState> = _uiState.asStateFlow()

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
                loadReport()
            }
        }
    }

    // ── Date picker ───────────────────────────────────────────────────────────

    fun showStartPicker()  { _uiState.update { it.copy(showStartPicker = true) } }
    fun showEndPicker()    { _uiState.update { it.copy(showEndPicker   = true) } }
    fun dismissStartPicker() { _uiState.update { it.copy(showStartPicker = false) } }
    fun dismissEndPicker()   { _uiState.update { it.copy(showEndPicker   = false) } }

    fun onStartDateSelected(ms: Long) {
        _uiState.update { it.copy(startDateMs = ms, showStartPicker = false) }
    }

    fun onEndDateSelected(ms: Long) {
        _uiState.update { it.copy(endDateMs = ms, showEndPicker = false) }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadReport() {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val start = _uiState.value.startDateMs.toIsoDateString(endOfDay = false)
            val end   = _uiState.value.endDateMs.toIsoDateString(endOfDay = true)
            when (val result = getSalesReportUseCase(rid, start, end)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, report = result.data, errorMessage = null)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = result.exception.message ?: "Failed to load report")
                }
                Result.Loading    -> Unit
            }
        }
    }

    fun dismissError() { _uiState.update { it.copy(errorMessage = null) } }
}

