package com.autobill.smartpos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.autobill.smartpos.data.local.entity.RestaurantEntity

@Dao
interface RestaurantDao {

    /**
     * Upsert a restaurant row.
     * Called at login / session recovery so that tables, orders, bills, and payments
     * can satisfy their FOREIGN KEY → restaurants(id) constraint.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(restaurant: RestaurantEntity)
}

