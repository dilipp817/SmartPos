package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Table
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

