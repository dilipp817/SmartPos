package com.autobill.smartpos.di

import android.content.Context
import androidx.room.Room
import com.autobill.smartpos.data.local.AppDatabase

object DatabaseModule {

    @Volatile
    private var database: AppDatabase? = null

    fun provideAppDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "smart_pos.db",
            ).build().also { database = it }
        }
    }
}
