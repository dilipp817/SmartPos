package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.featureflag.FeatureFlag
import kotlinx.coroutines.flow.Flow

/**
 * Contract for reading and (in debug builds) overriding feature flags.
 *
 * Consumers
 * ─────────
 *  • ViewModels — call [observe] and collect into a StateFlow for reactive UI gating.
 *  • Use-cases  — call [isEnabled] for synchronous checks against the last-known state.
 *  • Debug panel — calls [setOverride] / [clearOverride] / [clearAllOverrides].
 *
 * Release behaviour
 * ─────────────────
 *  [setOverride], [clearOverride], and [clearAllOverrides] are no-ops in release builds.
 *  [observeOverrides] emits an empty map in release builds.
 */
interface FeatureFlagRepository {

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Synchronous read against the in-memory merged cache.
     * Safe to call from any thread. Returns [FeatureFlag.defaultValue] until the
     * first DataStore emission resolves (typically < 50 ms after app start).
     */
    fun isEnabled(flag: FeatureFlag): Boolean

    /**
     * Reactive read — emits immediately with the current value and again on any change
     * (remote update, debug override toggle, or override cleared).
     */
    fun observe(flag: FeatureFlag): Flow<Boolean>

    /**
     * Reactive map of ALL flags — useful for the debug panel to render the full list.
     */
    fun observeAll(): Flow<Map<FeatureFlag, Boolean>>

    // ── Remote update ─────────────────────────────────────────────────────────

    /**
     * Fetch the latest flags from GET /feature-flags and persist them.
     * Called at app startup (after session recovery) and on every app foreground.
     * Silent no-op if the user is not logged in or the network call fails —
     * the last persisted values remain in effect.
     */
    suspend fun refreshFromRemoteApi()

    // ── Debug overrides — no-ops in release builds ────────────────────────────

    /** Set a local override for [flag]. Persists across app restarts in debug builds. */
    suspend fun setOverride(flag: FeatureFlag, enabled: Boolean)

    /** Remove the local override for [flag] — falls back to remote / default. */
    suspend fun clearOverride(flag: FeatureFlag)

    /** Remove ALL local overrides. */
    suspend fun clearAllOverrides()

    /**
     * Observe each flag's current override (null = no override set for that flag).
     * Used by the debug panel to show the source of each flag's current value.
     * Emits empty map in release builds.
     */
    fun observeOverrides(): Flow<Map<FeatureFlag, Boolean?>>
}



