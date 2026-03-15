package com.autobill.smartpos.di

import android.content.Context
import com.autobill.smartpos.BuildConfig
import com.autobill.smartpos.data.DatabaseModule
import com.autobill.smartpos.data.DispatchersModule
import com.autobill.smartpos.data.NetworkModule
import com.autobill.smartpos.data.RepositoryModule
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.domain.repository.FoodRepository
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase

// Manual Dependency Injection Container
// Initializes all dependencies for the application
class AppContainer(context: Context) {
    // Get IO Dispatcher for background operations
    private val ioDispatcher = DispatchersModule.io

    // Initialize Retrofit API service with base URL from BuildConfig
    private val foodApiService: FoodApiService = NetworkModule.provideFoodApiService(
        baseUrl = BuildConfig.BASE_URL,
        isDebug = BuildConfig.DEBUG
    )

    // Initialize Room database and get FoodDao
    private val foodDao = DatabaseModule.provideFoodDao(context)

    // Create FoodRepository implementation
    private val foodRepository: FoodRepository = RepositoryModule.provideFoodRepository(
        apiService = foodApiService,
        foodDao = foodDao,
        ioDispatcher = ioDispatcher,
    )

    // Expose GetFoodsUseCase for UI layer to use
    val getFoodsUseCase: GetFoodsUseCase = GetFoodsUseCase(foodRepository)
}
