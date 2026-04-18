package com.autobill.smartpos.feature.table

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus
import com.autobill.smartpos.domain.usecase.CreateTableUseCase
import com.autobill.smartpos.domain.usecase.DeleteTableUseCase
import com.autobill.smartpos.domain.usecase.GetAvailableTableCountUseCase
import com.autobill.smartpos.domain.usecase.GetAvailableTablesUseCase
import com.autobill.smartpos.domain.usecase.GetOccupiedTablesUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetTablesUseCase
import com.autobill.smartpos.domain.usecase.ObserveRolePermissionsUseCase
import com.autobill.smartpos.domain.usecase.ObserveTableEventsUseCase
import com.autobill.smartpos.domain.usecase.UpdateTableStatusUseCase
import com.autobill.smartpos.domain.usecase.UpdateTableUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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
 * ViewModel for the Table List / Selection screen.
 *
 * Responsibilities:
 *  - Load tables based on the active [TableFilter] tab
 *  - Fetch available table count for the header badge
 *  - Support pull-to-refresh
 *  - Guard against null restaurantId (super_admin — should not reach this screen)
 *  - Expose role-based [TableUiState.canManageTables] for FAB / card overflow gating
 *  - Full table CRUD for admin / manager roles
 */
@HiltViewModel
class TableViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getTablesUseCase: GetTablesUseCase,
    private val getAvailableTablesUseCase: GetAvailableTablesUseCase,
    private val getOccupiedTablesUseCase: GetOccupiedTablesUseCase,
    private val getAvailableTableCountUseCase: GetAvailableTableCountUseCase,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val updateTableStatusUseCase: UpdateTableStatusUseCase,
    private val observeRolePermissionsUseCase: ObserveRolePermissionsUseCase,
    private val createTableUseCase: CreateTableUseCase,
    private val updateTableUseCase: UpdateTableUseCase,
    private val deleteTableUseCase: DeleteTableUseCase,
    private val observeTableEventsUseCase: ObserveTableEventsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null

    /** Polling fallback job — 30 s interval (contract M-09). */
    private var pollingJob: Job? = null

    companion object {
        private const val POLLING_INTERVAL_MS = 30_000L
    }

    init {
        observeRolePermissionsUseCase()
            .onEach { perms ->
                _uiState.update { it.copy(canManageTables = perms.canManageTables) }
            }
            .launchIn(viewModelScope)

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
            loadAll()
            observeRealTimeEvents()
        }
    }

    // ── Phase 9.1: Real-time table updates ───────────────────────────────────

    /**
     * Subscribes to WebSocket TABLE_UPDATED events.
     * On each event, the matching table is replaced in-place in the current list.
     * If the updated table is no longer visible under the active filter (e.g. it became
     * OCCUPIED while viewing AVAILABLE), it is quietly removed from the displayed list
     * and the available-count badge is refreshed.
     */
    private fun observeRealTimeEvents() {
        observeTableEventsUseCase()
            .onEach { event ->
                val updated = event.table
                val rid = restaurantId ?: return@onEach
                _uiState.update { state ->
                    val filter = state.selectedFilter
                    val stillVisible = when (filter) {
                        TableFilter.ALL       -> true
                        TableFilter.AVAILABLE -> updated.status == com.autobill.smartpos.domain.model.TableStatus.AVAILABLE
                        TableFilter.OCCUPIED  -> updated.status == com.autobill.smartpos.domain.model.TableStatus.OCCUPIED
                    }
                    val newList = if (stillVisible) {
                        val exists = state.tables.any { it.id == updated.id }
                        if (exists) state.tables.map { if (it.id == updated.id) updated else it }
                        else state.tables + updated
                    } else {
                        state.tables.filter { it.id != updated.id }
                    }
                    state.copy(tables = newList)
                }
                // Refresh badge count whenever table status changes
                refreshAvailableCount(rid)
            }
            .launchIn(viewModelScope)
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

    // ── Lifecycle callbacks (called from Route via LifecycleEventEffect) ──────

    /** Start 30s polling when screen is resumed (contract M-09). */
    fun onResume() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(POLLING_INTERVAL_MS)
                loadAll()
            }
        }
    }

    /** Stop polling when screen is paused to avoid battery drain (contract M-09). */
    fun onPause() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // ── Status update dialog ─────────────────────────────────────────────────

    /**
     * Open the status-change dialog for [table].
     * Tapping a non-AVAILABLE card in the grid calls this.
     */
    fun showStatusUpdateDialog(table: Table) {
        val transitions = table.status.allowedTransitions()
        if (transitions.isEmpty()) return  // no valid moves — don't show an empty dialog
        _uiState.update {
            it.copy(
                statusUpdateDialog = StatusUpdateDialogState(
                    table = table,
                    availableTransitions = transitions,
                ),
                statusUpdateError = null,
            )
        }
    }

    /** Dismiss the dialog without making any change. */
    fun dismissStatusUpdateDialog() {
        _uiState.update { it.copy(statusUpdateDialog = null, statusUpdateError = null) }
    }

    /**
     * Execute the status PATCH for [table] → [newStatus].
     *
     * Flow:
     *  1. Optimistically update the table in the local list so the grid is instantly responsive.
     *  2. Call [UpdateTableStatusUseCase] (handles 409 retry internally).
     *  3. On success → replace the local row with the server-confirmed data + dismiss dialog.
     *  4. On failure → roll back the optimistic update + show error in dialog.
     */
    fun confirmStatusUpdate(table: Table, newStatus: TableStatus) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isUpdatingStatus = true, statusUpdateError = null) }
        val optimisticTable = table.copy(status = newStatus)
        _uiState.update { state ->
            state.copy(tables = state.tables.map { if (it.id == table.id) optimisticTable else it })
        }
        viewModelScope.launch {
            when (val result = updateTableStatusUseCase(rid, table.id, newStatus)) {
                is Result.Success -> {
                    val confirmed = result.data
                    _uiState.update { state ->
                        state.copy(
                            tables = state.tables.map { if (it.id == confirmed.id) confirmed else it },
                            statusUpdateDialog = null,
                            isUpdatingStatus = false,
                            statusUpdateSuccess = true,
                            statusUpdateError = null,
                        )
                    }
                    // Refresh badge count after any status change
                    refreshAvailableCount(rid)
                }
                is Result.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            tables = state.tables.map { if (it.id == table.id) table else it },
                            isUpdatingStatus = false,
                            statusUpdateError = result.exception.message
                                ?: context.getString(R.string.error_update_table_status),
                        )
                    }
                }
                Result.Loading -> Unit
            }
        }
    }

    /** Consume the one-shot success event after the screen has shown feedback. */
    fun onStatusUpdateSuccessConsumed() {
        _uiState.update { it.copy(statusUpdateSuccess = false) }
    }

    // ── CRUD — Admin / Manager ───────────────────────────────────────────────

    /** Open blank Create dialog. */
    fun showCreateDialog() {
        _uiState.update { it.copy(crudDialog = TableCrudDialogState.Create, crudError = null) }
    }

    /** Open Edit dialog pre-filled with [table]'s current values. */
    fun showEditDialog(table: Table) {
        _uiState.update {
            it.copy(crudDialog = TableCrudDialogState.Edit(table), crudError = null)
        }
    }

    /** Open Delete confirmation dialog for [table]. */
    fun showDeleteConfirm(table: Table) {
        _uiState.update {
            it.copy(crudDialog = TableCrudDialogState.DeleteConfirm(table), crudError = null)
        }
    }

    /** Dismiss any open CRUD dialog without making changes. */
    fun dismissCrudDialog() {
        _uiState.update { it.copy(crudDialog = null, crudError = null) }
    }

    /**
     * POST /restaurants/{restaurantId}/tables
     * On success: prepend new table to grid + show snackbar.
     */
    fun confirmCreate(tableNumber: String, floor: Int, capacity: Int) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isCrudInFlight = true, crudError = null) }
        viewModelScope.launch {
            when (val result = createTableUseCase(rid, tableNumber.trim(), floor, capacity)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            tables = listOf(result.data) + state.tables,
                            crudDialog = null,
                            isCrudInFlight = false,
                            crudError = null,
                            crudSuccessMessage = context.getString(R.string.table_created_success, result.data.tableNumber),
                        )
                    }
                    refreshAvailableCount(rid)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isCrudInFlight = false,
                        crudError = it.crudError ?: result.exception.message
                            ?: context.getString(R.string.error_create_table),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    /**
     * PUT /restaurants/{restaurantId}/tables/{id}
     * On success: replace the row in the grid + show snackbar.
     */
    fun confirmEdit(table: Table, tableNumber: String, floor: Int, capacity: Int) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isCrudInFlight = true, crudError = null) }
        viewModelScope.launch {
            when (val result = updateTableUseCase(rid, table.id, tableNumber.trim(), floor, capacity)) {
                is Result.Success -> {
                    val updated = result.data
                    _uiState.update { state ->
                        state.copy(
                            tables = state.tables.map { if (it.id == updated.id) updated else it },
                            crudDialog = null,
                            isCrudInFlight = false,
                            crudError = null,
                            crudSuccessMessage = context.getString(R.string.table_updated_success, updated.tableNumber),
                        )
                    }
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isCrudInFlight = false,
                        crudError = result.exception.message ?: context.getString(R.string.error_update_table),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    /**
     * DELETE /restaurants/{restaurantId}/tables/{id}
     * On success: remove from grid + show snackbar.
     */
    fun confirmDelete(table: Table) {
        val rid = restaurantId ?: return
        _uiState.update { it.copy(isCrudInFlight = true, crudError = null) }
        viewModelScope.launch {
            when (val result = deleteTableUseCase(rid, table.id)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            tables = state.tables.filter { it.id != table.id },
                            crudDialog = null,
                            isCrudInFlight = false,
                            crudError = null,
                            crudSuccessMessage = context.getString(R.string.table_deleted_success, table.tableNumber),
                        )
                    }
                    refreshAvailableCount(rid)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isCrudInFlight = false,
                        crudError = result.exception.message ?: context.getString(R.string.error_delete_table),
                    )
                }
                Result.Loading -> Unit
            }
        }
    }

    /** Consume the one-shot CRUD success snackbar message. */
    fun onCrudSuccessConsumed() {
        _uiState.update { it.copy(crudSuccessMessage = null) }
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
                    errorMessage = result.exception.message ?: context.getString(R.string.error_load_tables),
                )
                Result.Loading -> it.copy(isLoading = true)
            }
        }
    }

    private suspend fun refreshAvailableCount(restaurantId: Long) {
        when (val result = getAvailableTableCountUseCase(restaurantId)) {
            is Result.Success -> _uiState.update { it.copy(availableCount = result.data) }
            else -> Unit
        }
    }
}
