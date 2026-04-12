package com.autobill.smartpos.feature.table

import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

/**
 * UI state for the Table List screen.
 *
 * [tables]               — list shown in the grid (filtered by [selectedFilter])
 * [availableCount]       — number shown in the header badge
 * [selectedFilter]       — active tab: ALL / AVAILABLE / OCCUPIED
 * [isLoading]            — skeleton shown while fetching
 * [isRefreshing]         — pull-to-refresh spinner (tables already visible, just refreshing)
 * [errorMessage]         — non-null when the last fetch failed and cache is empty
 * [statusUpdateDialog]   — non-null → show the status-change dialog for that table
 * [isUpdatingStatus]     — true while the PATCH request is in flight
 * [statusUpdateError]    — non-null when the PATCH failed; shown in a snackbar
 * [statusUpdateSuccess]  — one-shot true after a successful PATCH; consumed by the screen
 */
data class TableUiState(
    val tables: List<Table> = emptyList(),
    val availableCount: Int = 0,
    val selectedFilter: TableFilter = TableFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    // ── Status update ────────────────────────────────────────────────────────
    val statusUpdateDialog: StatusUpdateDialogState? = null,
    val isUpdatingStatus: Boolean = false,
    val statusUpdateError: String? = null,
    val statusUpdateSuccess: Boolean = false,
)

/**
 * Carries the data needed to render the status-update dialog.
 *
 * [table]               — the table being changed
 * [availableTransitions] — statuses the user is allowed to switch to from [table.status]
 */
data class StatusUpdateDialogState(
    val table: Table,
    val availableTransitions: List<TableStatus>,
)

/**
 * Filter tabs for the table list.
 * Maps 1:1 to the three API endpoints used in [TableViewModel].
 */
enum class TableFilter(val label: String) {
    ALL("All Tables"),
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
}

/**
 * Business rules: valid status transitions for a table.
 * Enforced client-side for UX (server enforces its own rules independently).
 *
 * AVAILABLE   → RESERVED, MAINTENANCE
 * OCCUPIED    → CLEANING, MAINTENANCE
 * RESERVED    → AVAILABLE, OCCUPIED
 * CLEANING    → AVAILABLE, MAINTENANCE
 * MAINTENANCE → AVAILABLE
 */
fun TableStatus.allowedTransitions(): List<TableStatus> = when (this) {
    TableStatus.AVAILABLE   -> listOf(TableStatus.RESERVED, TableStatus.MAINTENANCE)
    TableStatus.OCCUPIED    -> listOf(TableStatus.CLEANING, TableStatus.MAINTENANCE)
    TableStatus.RESERVED    -> listOf(TableStatus.AVAILABLE, TableStatus.OCCUPIED)
    TableStatus.CLEANING    -> listOf(TableStatus.AVAILABLE, TableStatus.MAINTENANCE)
    TableStatus.MAINTENANCE -> listOf(TableStatus.AVAILABLE)
}

/** Colour tokens for each [TableStatus] — used by [TableGridCard]. */
fun TableStatus.containerColor(): Long = when (this) {
    TableStatus.AVAILABLE   -> 0xFFE8F5E9  // green-50
    TableStatus.OCCUPIED    -> 0xFFFFEBEE  // red-50
    TableStatus.RESERVED    -> 0xFFFFF8E1  // amber-50
    TableStatus.CLEANING    -> 0xFFE3F2FD  // blue-50
    TableStatus.MAINTENANCE -> 0xFFF3E5F5  // purple-50
}

fun TableStatus.contentColor(): Long = when (this) {
    TableStatus.AVAILABLE   -> 0xFF2E7D32  // green-800
    TableStatus.OCCUPIED    -> 0xFFC62828  // red-800
    TableStatus.RESERVED    -> 0xFFF57F17  // amber-900
    TableStatus.CLEANING    -> 0xFF1565C0  // blue-800
    TableStatus.MAINTENANCE -> 0xFF6A1B9A  // purple-800
}



