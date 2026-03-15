package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.autobill.smartpos.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE restaurantId = :restaurantId ORDER BY id ASC")
    fun observeFoodsByRestaurant(restaurantId: Int): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE restaurantId = :restaurantId ORDER BY id ASC")
    suspend fun getFoodsByRestaurant(restaurantId: Int): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Query("DELETE FROM foods WHERE restaurantId = :restaurantId")
    suspend fun deleteByRestaurant(restaurantId: Int)
}

