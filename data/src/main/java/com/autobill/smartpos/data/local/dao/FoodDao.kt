package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobill.smartpos.data.local.entity.FoodEntity

/**
 * Room DAO for Food entity.
 * Provides database access methods for CRUD operations.
 */
@Dao
interface FoodDao {
    /**
     * Fetches all foods from database
     */
    @Query("SELECT * FROM foods ORDER BY id ASC")
    suspend fun getAllFoods(): List<FoodEntity>

    /**
     * Fetches a single food by ID
     */
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Int): FoodEntity?

    /**
     * Searches foods by name
     */
    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
    suspend fun searchFoods(query: String): List<FoodEntity>

    /**
     * Inserts or updates foods in database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(foods: List<FoodEntity>)

    /**
     * Deletes all foods from database
     */
    @Query("DELETE FROM foods")
    suspend fun deleteAll()
}

