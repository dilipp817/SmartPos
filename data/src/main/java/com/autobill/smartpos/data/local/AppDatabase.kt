package com.autobill.smartpos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.local.entity.FoodEntity

/**
 * Room Database: AppDatabase
 * Version 2: Added imageUrl, category, description, isAvailable to FoodEntity
 */
@Database(
    entities = [FoodEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}

