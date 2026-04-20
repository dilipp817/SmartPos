package com.autobill.smartpos.data.featureflag

import com.autobill.smartpos.domain.featureflag.FeatureFlag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RELEASE build — no-op override store.
 *
 * All write methods are no-ops. [observeOverrides] always emits an empty map
 * so the repository's merge logic always falls through to remote / default values.
 * Zero DataStore or disk I/O occurs in production builds.
 */
@Singleton
class FeatureFlagOverrideStoreImpl @Inject constructor() : FeatureFlagOverrideStore {

    override fun observeOverrides(): Flow<Map<FeatureFlag, Boolean?>> =
        flowOf(FeatureFlag.entries.associateWith { null })

    override suspend fun setOverride(flag: FeatureFlag, enabled: Boolean) = Unit

    override suspend fun clearOverride(flag: FeatureFlag) = Unit

    override suspend fun clearAllOverrides() = Unit
}

