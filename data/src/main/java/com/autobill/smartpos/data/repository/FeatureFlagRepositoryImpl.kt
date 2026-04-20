package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.featureflag.FeatureFlagDataStore
import com.autobill.smartpos.data.featureflag.FeatureFlagOverrideStore
import com.autobill.smartpos.data.remote.FeatureFlagApiService
import com.autobill.smartpos.domain.featureflag.FeatureFlag
import com.autobill.smartpos.domain.repository.FeatureFlagRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Merges three sources into a single resolved flag value:
 *
 *   Priority 1 — debug override  (only non-null in debug builds)
 *   Priority 2 — remote flag     (delivered from backend after login)
 *   Priority 3 — default value   (compile-time fallback in [FeatureFlag])
 *
 * An in-memory [StateFlow] (_merged) is kept up-to-date by combining both DataStore
 * flows in a long-lived [repositoryScope]. This allows [isEnabled] to be synchronous
 * while still reflecting the latest persisted values.
 *
 * The scope uses [SupervisorJob] so a failure in one collect doesn't cancel the other.
 */
@Singleton
class FeatureFlagRepositoryImpl @Inject constructor(
    private val remoteStore: FeatureFlagDataStore,
    private val overrideStore: FeatureFlagOverrideStore,
    private val apiService: FeatureFlagApiService,
) : FeatureFlagRepository {

    /** Long-lived scope tied to the singleton lifetime (lives as long as the process). */
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _merged: MutableStateFlow<Map<FeatureFlag, Boolean>> = MutableStateFlow(
        FeatureFlag.entries.associateWith { it.defaultValue }
    )
    private val merged: StateFlow<Map<FeatureFlag, Boolean>> = _merged.asStateFlow()

    init {
        repositoryScope.launch {
            combine(
                remoteStore.observeFlags(),
                overrideStore.observeOverrides(),
            ) { remote, overrides ->
                FeatureFlag.entries.associateWith { flag ->
                    // Override wins → remote wins → compile-time default
                    overrides[flag] ?: remote[flag] ?: flag.defaultValue
                }
            }.collect { _merged.value = it }
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    override fun isEnabled(flag: FeatureFlag): Boolean =
        merged.value[flag] ?: flag.defaultValue

    override fun observe(flag: FeatureFlag): Flow<Boolean> =
        merged.map { it[flag] ?: flag.defaultValue }

    override fun observeAll(): Flow<Map<FeatureFlag, Boolean>> =
        merged

    // ── Remote update ─────────────────────────────────────────────────────────

    /**
     * Fetches GET /feature-flags and persists the result.
     * Called at startup and on every app foreground via [MainViewModel].
     * Any failure is swallowed — stale cached values remain in effect.
     */
    override suspend fun refreshFromRemoteApi(): Boolean {
        return try {
            val response = apiService.getFeatureFlags()
            val flags = response.data?.flags
            if (!flags.isNullOrEmpty()) {
                remoteStore.updateFromRemote(flags)
                true   // flags fetched and persisted — advance the throttle window
            } else {
                false  // endpoint returned empty/null data — don't advance throttle
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            false      // network error, 404 (not yet implemented), 401, etc. — don't advance throttle
        }
    }

    // ── Debug overrides ───────────────────────────────────────────────────────
    // In release builds these delegate to the no-op FeatureFlagOverrideStoreImpl.

    override suspend fun setOverride(flag: FeatureFlag, enabled: Boolean) =
        overrideStore.setOverride(flag, enabled)

    override suspend fun clearOverride(flag: FeatureFlag) =
        overrideStore.clearOverride(flag)

    override suspend fun clearAllOverrides() =
        overrideStore.clearAllOverrides()

    override fun observeOverrides(): Flow<Map<FeatureFlag, Boolean?>> =
        overrideStore.observeOverrides()
}

