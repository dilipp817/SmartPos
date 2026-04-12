package com.autobill.smartpos.data.di

import android.content.Context
import androidx.room.Room
import com.autobill.smartpos.data.local.AppDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smartpos.db",
        )
            .addMigrations(
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7,
                AppDatabase.MIGRATION_7_8,
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: AppDatabase): FoodDao {
        return database.foodDao()
    }
}

