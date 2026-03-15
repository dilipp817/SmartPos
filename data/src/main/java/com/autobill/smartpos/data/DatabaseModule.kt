package com.autobill.smartpos.data

import android.content.Context
import androidx.room.Room
import com.autobill.smartpos.data.local.AppDatabase
import com.autobill.smartpos.data.local.dao.FoodDao

// Room Database Module
// Provides singleton instance of Room database using double-checked locking pattern
object DatabaseModule {

    // Volatile ensures visibility of changes across threads
    @Volatile
    private var database: AppDatabase? = null

    // Provides singleton AppDatabase instance
    // Uses double-checked locking for thread-safe lazy initialization
    private fun provideAppDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            // Check again inside synchronized block (double-checked locking)
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "smart_pos.db",
            ).build().also { database = it }
        }
    }

    // Provides FoodDao - Exposes only the DAO without exposing AppDatabase
    // This helps avoid classpath issues when importing from other modules
    fun provideFoodDao(context: Context): FoodDao {
        return provideAppDatabase(context).foodDao()
    }
}
