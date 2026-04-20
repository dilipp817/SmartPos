package com.autobill.smartpos.data.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.autobill.smartpos.data.local.AppDatabase
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.local.dao.OrderDao
import com.autobill.smartpos.data.local.dao.PendingOrderDao
import com.autobill.smartpos.data.local.dao.RestaurantDao
import com.autobill.smartpos.data.local.dao.TableDao
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
            .addMigrations(AppDatabase.MIGRATION_2_3)
            // Fallback protects against future unhandled version gaps during development.
            // Before release, every migration must be explicit — pending_orders rows must
            // never be silently dropped on upgrade (they are offline orders not yet synced).
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: AppDatabase): FoodDao = database.foodDao()

    @Provides
    @Singleton
    fun provideRestaurantDao(database: AppDatabase): RestaurantDao = database.restaurantDao()

    @Provides
    @Singleton
    fun provideTableDao(database: AppDatabase): TableDao = database.tableDao()

    @Provides
    @Singleton
    fun provideOrderDao(database: AppDatabase): OrderDao = database.orderDao()

    @Provides
    @Singleton
    fun providePendingOrderDao(database: AppDatabase): PendingOrderDao = database.pendingOrderDao()

    /** WorkManager instance — used by [OfflineQueueRepositoryImpl] to schedule [SyncWorker]. */
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}

