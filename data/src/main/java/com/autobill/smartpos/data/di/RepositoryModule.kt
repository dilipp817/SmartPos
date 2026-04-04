package com.autobill.smartpos.data.di

import com.autobill.smartpos.data.repository.CartRepositoryImpl
import com.autobill.smartpos.data.repository.MockFoodRepository
import com.autobill.smartpos.domain.repository.CartRepository
import com.autobill.smartpos.domain.repository.FoodRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Food data source.
     * TODO: Switch to FoodRepositoryImpl when backend staging is deployed.
     */
    @Binds
    @Singleton
    abstract fun bindFoodRepository(impl: MockFoodRepository): FoodRepository

    /** In-memory cart — session-scoped, no backend required */
    @Binds
    @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository
}
