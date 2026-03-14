package com.autobill.smartpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.local.entity.FoodEntity

@Database(
    entities = [FoodEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}

