package com.autobill.smartpos.data.di

import com.autobill.smartpos.data.repository.AuthRepositoryImpl
import com.autobill.smartpos.data.repository.BillRepositoryImpl
import com.autobill.smartpos.data.repository.CartRepositoryImpl
import com.autobill.smartpos.data.repository.CategoryRepositoryImpl
import com.autobill.smartpos.data.repository.MockFoodRepository
import com.autobill.smartpos.data.repository.OrderRepositoryImpl
import com.autobill.smartpos.data.repository.PaymentRepositoryImpl
import com.autobill.smartpos.data.repository.RealTimeRepositoryImpl
import com.autobill.smartpos.data.repository.RestaurantRepositoryImpl
import com.autobill.smartpos.data.repository.TableRepositoryImpl
import com.autobill.smartpos.domain.repository.AuthRepository
import com.autobill.smartpos.domain.repository.BillRepository
import com.autobill.smartpos.domain.repository.CartRepository
import com.autobill.smartpos.domain.repository.CategoryRepository
import com.autobill.smartpos.domain.repository.FoodRepository
import com.autobill.smartpos.domain.repository.OrderRepository
import com.autobill.smartpos.domain.repository.PaymentRepository
import com.autobill.smartpos.domain.repository.RealTimeRepository
import com.autobill.smartpos.domain.repository.RestaurantRepository
import com.autobill.smartpos.domain.repository.TableRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Auth repository — provides login, session, and restaurantId management.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

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

    /** Table data — network-first with Room cache fallback */
    @Binds
    @Singleton
    abstract fun bindTableRepository(impl: TableRepositoryImpl): TableRepository

    /** Order data — create / fetch / update orders */
    @Binds
    @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    /** Bill data — generate, fetch, and cancel bills */
    @Binds
    @Singleton
    abstract fun bindBillRepository(impl: BillRepositoryImpl): BillRepository

    /** Payment data — process, confirm, refund payments */
    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    /** Restaurant details and settings — cached locally after first fetch */
    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(impl: RestaurantRepositoryImpl): RestaurantRepository

    /** Category list — in-memory cache refreshed on every fetch */
    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    /** WebSocket real-time event pipeline — Phase 9.1 */
    @Binds
    @Singleton
    abstract fun bindRealTimeRepository(impl: RealTimeRepositoryImpl): RealTimeRepository
}
