package com.autobill.smartpos.feature.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.GetAvailableTableCountUseCase
import com.autobill.smartpos.domain.usecase.GetAvailableTablesUseCase
import com.autobill.smartpos.domain.usecase.GetOccupiedTablesUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetTablesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Table List / Selection screen.
 *
 * Responsibilities:
 *  - Load tables based on the active [TableFilter] tab
 *  - Fetch available table count for the header badge
 *  - Support pull-to-refresh
 *  - Guard against null restaurantId (super_admin — should not reach this screen)
 */
@HiltViewModel
class TableViewModel @Inject constructor(
    private val getTablesUseCase: GetTablesUseCase,
    private val getAvailableTablesUseCase: GetAvailableTablesUseCase,
    private val getOccupiedTablesUseCase: GetOccupiedTablesUseCase,
    private val getAvailableTableCountUseCase: GetAvailableTableCountUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    /** Cached restaurantId — sourced from session, never hardcoded. */
    private var restaurantId: Long? = null

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            if (restaurantId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No restaurant assigned to this account. Please log in again.",
                    )
                }
                return@launch
            }
            loadAll()
        }
    }

    /** Switch filter tab and reload immediately. */
    fun selectFilter(filter: TableFilter) {
        if (_uiState.value.selectedFilter == filter) return
        _uiState.update { it.copy(selectedFilter = filter, isLoading = true, errorMessage = null) }
        viewModelScope.launch { loadTables() }
    }

    /** Pull-to-refresh — shows spinner without replacing the existing grid. */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        viewModelScope.launch { loadAll(refreshing = true) }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun loadAll(refreshing: Boolean = false) {
        val rid = restaurantId ?: return
        // Fetch tables + badge count concurrently
        val tablesDeferred = viewModelScope.async { loadTables() }
        val countDeferred = viewModelScope.async {
            when (val result = getAvailableTableCountUseCase(rid)) {
                is Result.Success -> _uiState.update { it.copy(availableCount = result.data) }
                else -> Unit  // badge count failure is non-critical — keep previous value
            }
        }
        tablesDeferred.await()
        countDeferred.await()
        if (refreshing) _uiState.update { it.copy(isRefreshing = false) }
    }

    private suspend fun loadTables() {
        val rid = restaurantId ?: return
        val result = when (_uiState.value.selectedFilter) {
            TableFilter.ALL       -> getTablesUseCase(rid)
            TableFilter.AVAILABLE -> getAvailableTablesUseCase(rid)
            TableFilter.OCCUPIED  -> getOccupiedTablesUseCase(rid)
        }
        _uiState.update {
            when (result) {
                is Result.Success -> it.copy(
                    tables = result.data,
                    isLoading = false,
                    errorMessage = null,
                )
                is Result.Failure -> it.copy(
                    isLoading = false,
                    errorMessage = result.exception.message ?: "Failed to load tables. Please try again.",
                )
                Result.Loading -> it.copy(isLoading = true)
            }
        }
    }
}

