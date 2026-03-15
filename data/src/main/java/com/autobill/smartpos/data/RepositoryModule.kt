package com.autobill.smartpos.data

import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.data.repository.FoodRepositoryImpl
import com.autobill.smartpos.domain.repository.FoodRepository
import kotlinx.coroutines.CoroutineDispatcher

// Repository Factory Module
// Creates concrete implementations of repository interfaces
object RepositoryModule {

    // Factory function to create FoodRepository implementation
    // Takes dependencies as parameters for better control and testability
    fun provideFoodRepository(
        apiService: FoodApiService,
        foodDao: FoodDao,
        ioDispatcher: CoroutineDispatcher,
    ): FoodRepository = FoodRepositoryImpl(
        apiService = apiService,
        foodDao = foodDao,
        ioDispatcher = ioDispatcher,
    )
}

