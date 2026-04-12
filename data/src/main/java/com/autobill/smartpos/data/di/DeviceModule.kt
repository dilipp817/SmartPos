package com.autobill.smartpos.data.di

import com.autobill.smartpos.core.device.DeviceInfoProvider
import com.autobill.smartpos.data.device.AndroidDeviceInfoProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module: binds [DeviceInfoProvider] to its Android implementation.
 *
 * Kept separate from RepositoryModule so each module has a single binding
 * concern — easier to read, easier to swap in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceModule {

    @Binds
    @Singleton
    abstract fun bindDeviceInfoProvider(
        impl: AndroidDeviceInfoProvider,
    ): DeviceInfoProvider
}

