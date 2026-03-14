package com.autobill.smartpos.di

import android.content.Context
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.domain.repository.FoodRepository
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase

class AppContainer(context: Context) {
    private val ioDispatcher = DispatchersModule.io

    private val foodApiService: FoodApiService = NetworkModule.provideFoodApiService()

    private val foodDao: FoodDao = DatabaseModule
        .provideAppDatabase(context)
        .foodDao()

    private val foodRepository: FoodRepository = RepositoryModule.provideFoodRepository(
        apiService = foodApiService,
        foodDao = foodDao,
        ioDispatcher = ioDispatcher,
    )

    val getFoodsUseCase: GetFoodsUseCase = GetFoodsUseCase(foodRepository)
}
