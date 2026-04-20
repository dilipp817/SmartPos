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
            // Room is a server-data read-through cache — destructive migration is safe:
            // data is re-fetched from the API on next app start.
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

