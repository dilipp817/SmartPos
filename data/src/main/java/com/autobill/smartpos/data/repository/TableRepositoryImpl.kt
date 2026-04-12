package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.dao.TableDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.TableApiService
import com.autobill.smartpos.data.remote.dto.CreateTableRequest
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus
import com.autobill.smartpos.domain.repository.TableRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TableRepository implementation.
 *
 * Strategy: network-first with Room cache fallback.
 *  - On success → upsert to Room, return live data.
 *  - On network failure → return cached data (stale-but-usable for offline resilience).
 *  - If both fail → propagate the network error.
 */
@Singleton
class TableRepositoryImpl @Inject constructor(
    private val apiService: TableApiService,
    private val tableDao: TableDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : TableRepository {

    override suspend fun getAllTables(restaurantId: Long): Result<List<Table>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getAllTables(restaurantId)
                val data = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch tables"
                }
                val tables = data.tables.map { it.toDomain() }
                tableDao.upsertAll(data.tables.map { it.toEntity() })
                Result.Success(tables)
            } catch (e: Exception) {
                val cached = tableDao.getAllTables(restaurantId).map { it.toDomain() }
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
            }
        }

    override suspend fun getAvailableTables(
        restaurantId: Long,
        minCapacity: Int?,
    ): Result<List<Table>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getAvailableTables(restaurantId, minCapacity)
            val tables = checkNotNull(response.data) {
                response.message ?: "Failed to fetch available tables"
            }.map { it.toDomain() }
            // Upsert into cache so offline mode reflects latest availability
            tableDao.upsertAll(
                checkNotNull(response.data).map { it.toEntity() }
            )
            Result.Success(tables)
        } catch (e: Exception) {
            val cached = tableDao.getAvailableTables(restaurantId).map { it.toDomain() }
                .let { all ->
                    if (minCapacity != null) all.filter { it.capacity >= minCapacity } else all
                }
            if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
        }
    }

    override suspend fun getOccupiedTables(restaurantId: Long): Result<List<Table>> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getOccupiedTables(restaurantId)
                val tables = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch occupied tables"
                }.map { it.toDomain() }
                tableDao.upsertAll(checkNotNull(response.data).map { it.toEntity() })
                Result.Success(tables)
            } catch (e: Exception) {
                val cached = tableDao.getOccupiedTables(restaurantId).map { it.toDomain() }
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Failure(e)
            }
        }

    override suspend fun countAvailableTables(restaurantId: Long): Result<Int> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.countAvailableTables(restaurantId)
                val count = checkNotNull(response.data) {
                    response.message ?: "Failed to fetch available count"
                }
                Result.Success(count)
            } catch (e: Exception) {
                Result.Success(tableDao.countAvailableTables(restaurantId))
            }
        }

    override suspend fun getTableById(restaurantId: Long, tableId: Long): Result<Table> =
        withContext(ioDispatcher) {
            // Cache-first: Room is always up-to-date from Phase 4 list fetches
            val cached = tableDao.getTableById(tableId)
            if (cached != null) return@withContext Result.Success(cached.toDomain())
            // Fallback to network if cache misses (e.g. deep-link or fresh install)
            try {
                val response = apiService.getTableById(restaurantId, tableId)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Table not found"
                }
                tableDao.upsertAll(listOf(dto.toEntity()))
                Result.Success(dto.toDomain())
            } catch (e: Exception) {
                Result.Failure(e)
            }
        }

    /**
     * PATCH …/tables/{id}/status?newStatus=CLEANING
     *
     * Optimistic locking strategy:
     *  1. Attempt the PATCH.
     *  2. On 409 CONFLICT — the server's version has changed (another actor updated the table).
     *     Re-fetch the table to get the current state, then retry the PATCH once.
     *  3. Any other error → propagate as [Result.Failure].
     *  4. On success → upsert the returned DTO into the local cache so the grid reflects
     *     the change immediately without a full reload.
     */
    override suspend fun updateTableStatus(
        restaurantId: Long,
        tableId: Long,
        newStatus: TableStatus,
    ): Result<Table> = withContext(ioDispatcher) {
        suspend fun patch(): Result<Table> {
            val response = apiService.updateTableStatus(restaurantId, tableId, newStatus.value)
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update table status"
            }
            tableDao.upsertAll(listOf(dto.toEntity()))
            return Result.Success(dto.toDomain())
        }

        try {
            patch()
        } catch (e: HttpException) {
            if (e.code() == 409) {
                // 409 CONFLICT — re-fetch to get latest state, then retry once
                try {
                    // Refresh local cache with current server state
                    val refreshResponse = apiService.getTableById(restaurantId, tableId)
                    val refreshedDto = checkNotNull(refreshResponse.data) {
                        "Table $tableId not found during 409 recovery"
                    }
                    tableDao.upsertAll(listOf(refreshedDto.toEntity()))
                    // Retry the status update with the refreshed state
                    patch()
                } catch (retryEx: Exception) {
                    Result.Failure(retryEx)
                }
            } else {
                Result.Failure(e)
            }
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    // ── CRUD — Admin / Manager ───────────────────────────────────────────────

    /** POST …/tables — creates a new table, always starts AVAILABLE. */
    override suspend fun createTable(
        restaurantId: Long,
        tableNumber: String,
        floor: Int,
        capacity: Int,
    ): Result<Table> = withContext(ioDispatcher) {
        try {
            val response = apiService.createTable(
                restaurantId = restaurantId,
                request = CreateTableRequest(
                    tableNumber = tableNumber,
                    floor = floor,
                    capacity = capacity,
                    status = "AVAILABLE",
                ),
            )
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to create table"
            }
            tableDao.upsertAll(listOf(dto.toEntity()))
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    /** PUT …/tables/{id} — updates tableNumber, floor, capacity. Status is preserved. */
    override suspend fun updateTable(
        restaurantId: Long,
        tableId: Long,
        tableNumber: String,
        floor: Int,
        capacity: Int,
    ): Result<Table> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateTable(
                restaurantId = restaurantId,
                id = tableId,
                request = CreateTableRequest(
                    tableNumber = tableNumber,
                    floor = floor,
                    capacity = capacity,
                ),
            )
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update table"
            }
            tableDao.upsertAll(listOf(dto.toEntity()))
            Result.Success(dto.toDomain())
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }

    /** DELETE …/tables/{id} — removes table from backend and local cache. */
    override suspend fun deleteTable(
        restaurantId: Long,
        tableId: Long,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            apiService.deleteTable(restaurantId, tableId)
            tableDao.deleteById(tableId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}

