package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobill.smartpos.data.local.entity.TableEntity

@Dao
interface TableDao {

    /** All tables for a restaurant ordered by floor then table number. */
    @Query("SELECT * FROM tables WHERE restaurantId = :restaurantId ORDER BY floor ASC, tableNumber ASC")
    suspend fun getAllTables(restaurantId: Long): List<TableEntity>

    /** Only AVAILABLE tables — offline fallback for available filter. */
    @Query("SELECT * FROM tables WHERE restaurantId = :restaurantId AND status = 'AVAILABLE' ORDER BY floor ASC, tableNumber ASC")
    suspend fun getAvailableTables(restaurantId: Long): List<TableEntity>

    /** Only OCCUPIED tables — offline fallback for occupied filter. */
    @Query("SELECT * FROM tables WHERE restaurantId = :restaurantId AND status = 'OCCUPIED' ORDER BY floor ASC, tableNumber ASC")
    suspend fun getOccupiedTables(restaurantId: Long): List<TableEntity>

    /** Count of AVAILABLE tables — for header badge. */
    @Query("SELECT COUNT(*) FROM tables WHERE restaurantId = :restaurantId AND status = 'AVAILABLE'")
    suspend fun countAvailableTables(restaurantId: Long): Int

    /** Insert or replace all tables (called after every successful API fetch). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tables: List<TableEntity>)

    /** Remove stale data for a restaurant before re-inserting fresh data. */
    @Query("DELETE FROM tables WHERE restaurantId = :restaurantId")
    suspend fun deleteAllByRestaurant(restaurantId: Long)
}

