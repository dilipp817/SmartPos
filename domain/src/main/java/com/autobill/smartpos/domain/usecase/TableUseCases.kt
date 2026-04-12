package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus
import com.autobill.smartpos.domain.repository.TableRepository
import javax.inject.Inject

/**
 * Use case: Fetch all tables for a restaurant.
 * [restaurantId] MUST come from [GetRestaurantIdUseCase] — never hardcoded.
 */
class GetTablesUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<List<Table>> =
        repository.getAllTables(restaurantId)
}

/**
 * Use case: Fetch only AVAILABLE tables.
 * [minCapacity] is optional — used when user needs a table for N guests.
 */
class GetAvailableTablesUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        minCapacity: Int? = null,
    ): Result<List<Table>> = repository.getAvailableTables(restaurantId, minCapacity)
}

/**
 * Use case: Fetch only OCCUPIED tables.
 * Used for the occupied view / quick staff overview.
 */
class GetOccupiedTablesUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<List<Table>> =
        repository.getOccupiedTables(restaurantId)
}

/**
 * Use case: Get count of available tables.
 * Used for the header badge — quick at-a-glance number for the cashier.
 */
class GetAvailableTableCountUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<Int> =
        repository.countAvailableTables(restaurantId)
}

/**
 * Use case: Update the status of a single table.
 *
 * Key uses:
 *  - Staff manually marks a table as CLEANING / MAINTENANCE / RESERVED
 *  - Payment screen frees a table → newStatus = AVAILABLE
 *
 * 409 CONFLICT (optimistic locking) is handled inside [TableRepository] — callers
 * don't need to implement retry logic themselves.
 */
class UpdateTableStatusUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        tableId: Long,
        newStatus: TableStatus,
    ): Result<Table> = repository.updateTableStatus(restaurantId, tableId, newStatus)
}

// ── CRUD — Admin / Manager ──────────────────────────────────────────────────

/**
 * Use case: Create a new table.
 * Always starts as AVAILABLE — status is set by the backend on creation.
 */
class CreateTableUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        tableNumber: String,
        floor: Int,
        capacity: Int,
    ): Result<Table> = repository.createTable(restaurantId, tableNumber, floor, capacity)
}

/**
 * Use case: Update table number, floor, and capacity.
 * Status is intentionally excluded — use [UpdateTableStatusUseCase] for that.
 */
class UpdateTableUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        tableId: Long,
        tableNumber: String,
        floor: Int,
        capacity: Int,
    ): Result<Table> = repository.updateTable(restaurantId, tableId, tableNumber, floor, capacity)
}

/**
 * Use case: Permanently delete a table.
 * The backend enforces that only AVAILABLE / MAINTENANCE tables can be deleted.
 */
class DeleteTableUseCase @Inject constructor(
    private val repository: TableRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        tableId: Long,
    ): Result<Unit> = repository.deleteTable(restaurantId, tableId)
}

