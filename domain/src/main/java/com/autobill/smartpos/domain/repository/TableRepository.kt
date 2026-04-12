package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Table

/**
 * Repository contract for table data operations.
 * restaurantId always comes from the session — never hardcoded.
 */
interface TableRepository {

    /** GET /restaurants/{restaurantId}/tables — all tables, cached offline */
    suspend fun getAllTables(restaurantId: Long): Result<List<Table>>

    /**
     * GET /restaurants/{restaurantId}/tables/available?capacity={min}
     * [minCapacity] is optional — omit to get all available regardless of size.
     */
    suspend fun getAvailableTables(
        restaurantId: Long,
        minCapacity: Int? = null,
    ): Result<List<Table>>

    /** GET /restaurants/{restaurantId}/tables/occupied */
    suspend fun getOccupiedTables(restaurantId: Long): Result<List<Table>>

    /** GET /restaurants/{restaurantId}/tables/count/available — for header badge */
    suspend fun countAvailableTables(restaurantId: Long): Result<Int>
}

