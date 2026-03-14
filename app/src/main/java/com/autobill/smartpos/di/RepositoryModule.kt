package com.autobill.smartpos.di

import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.data.repository.FoodRepositoryImpl
import com.autobill.smartpos.domain.repository.FoodRepository
import kotlinx.coroutines.CoroutineDispatcher

object RepositoryModule {
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
