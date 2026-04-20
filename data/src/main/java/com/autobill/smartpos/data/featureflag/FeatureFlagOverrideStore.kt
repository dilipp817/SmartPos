package com.autobill.smartpos.data.featureflag

import com.autobill.smartpos.domain.featureflag.FeatureFlag
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for debug-only feature flag overrides.
 *
 * Two implementations exist — selected at compile time by build type source set:
 *  • debug   → [FeatureFlagOverrideStoreImpl] — writable DataStore, persists across restarts
 *  • release → [FeatureFlagOverrideStoreImpl] — all methods are no-ops, flow emits empty map
 *
 * Hilt binds whichever compiled variant is present via [FeatureFlagModule].
 */
interface FeatureFlagOverrideStore {
    /** Emits the current override map. Null value = no override for that flag. */
    fun observeOverrides(): Flow<Map<FeatureFlag, Boolean?>>

    /** Persist an override for [flag]. No-op in release builds. */
    suspend fun setOverride(flag: FeatureFlag, enabled: Boolean)

    /** Remove the override for [flag]. No-op in release builds. */
    suspend fun clearOverride(flag: FeatureFlag)

    /** Remove all overrides. No-op in release builds. */
    suspend fun clearAllOverrides()
}

