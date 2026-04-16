package com.autobill.smartpos.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Application-lifetime [CoroutineScope] — survives configuration changes and screen
 * transitions. Use this wherever work must outlive any single ViewModel or Activity.
 *
 * Testability: inject a [kotlinx.coroutines.test.TestScope] in unit tests so you
 * can advance virtual time and assert completion deterministically.
 *
 * Example test setup:
 *   val testScope = TestScope()
 *   val interceptor = UnauthorizedInterceptor(fakeSession, testScope)
 *   interceptor.intercept(chain)
 *   testScope.advanceUntilIdle()   // ← now clearUser() is guaranteed to have run
 *   verify(fakeSession).clearUser()
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}

