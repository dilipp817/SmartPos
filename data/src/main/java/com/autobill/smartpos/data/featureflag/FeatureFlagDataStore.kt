package com.autobill.smartpos.data.featureflag

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.autobill.smartpos.domain.featureflag.FeatureFlag
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.featureFlagDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "smartpos_feature_flags_remote")

/**
 * Persists feature flags received from the backend (login response / remote config).
 *
 * Each [FeatureFlag] is stored as an individual boolean preference keyed by
 * "remote_<flag.key>" so that future DataStore schema changes don't clobber existing values.
 *
 * This store holds REMOTE flags only — debug overrides live in [FeatureFlagOverrideStore].
 */
@Singleton
class FeatureFlagDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.featureFlagDataStore

    private fun prefKey(flag: FeatureFlag) =
        booleanPreferencesKey("remote_${flag.key}")

    /** Observe all stored remote flags. Emits null for flags not yet received from backend. */
    fun observeFlags(): Flow<Map<FeatureFlag, Boolean?>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            FeatureFlag.entries.associateWith { flag -> prefs[prefKey(flag)] }
        }

    /**
     * Persist remote flags delivered by the backend.
     * Only keys matching a known [FeatureFlag.key] are written — unknown keys are ignored.
     */
    suspend fun updateFromRemote(flags: Map<String, Boolean>) {
        val keyToFlag = FeatureFlag.entries.associateBy { it.key }
        dataStore.edit { prefs ->
            flags.forEach { (key, value) ->
                val flag = keyToFlag[key] ?: return@forEach
                prefs[prefKey(flag)] = value
            }
        }
    }
}

