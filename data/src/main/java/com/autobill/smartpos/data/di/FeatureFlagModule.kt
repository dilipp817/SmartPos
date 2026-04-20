package com.autobill.smartpos.data.di

import com.autobill.smartpos.data.featureflag.FeatureFlagOverrideStore
import com.autobill.smartpos.data.featureflag.FeatureFlagOverrideStoreImpl
import com.autobill.smartpos.data.repository.FeatureFlagRepositoryImpl
import com.autobill.smartpos.domain.repository.FeatureFlagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt bindings for the feature flag system.
 *
 * [FeatureFlagOverrideStoreImpl] resolves to a different class at compile time depending
 * on the build type:
 *   debug   → data/src/debug/.../FeatureFlagOverrideStoreImpl  (writable DataStore)
 *   release → data/src/release/.../FeatureFlagOverrideStoreImpl (no-op)
 *
 * Both share the same class name and package, so this module works unchanged
 * across all build variants.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureFlagModule {

    @Binds
    @Singleton
    abstract fun bindFeatureFlagRepository(
        impl: FeatureFlagRepositoryImpl,
    ): FeatureFlagRepository

    @Binds
    @Singleton
    abstract fun bindFeatureFlagOverrideStore(
        impl: FeatureFlagOverrideStoreImpl,
    ): FeatureFlagOverrideStore
}

