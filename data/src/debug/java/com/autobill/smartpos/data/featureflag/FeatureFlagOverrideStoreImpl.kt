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

private val Context.flagOverrideDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "smartpos_feature_flag_overrides")

/**
 * DEBUG build — writable DataStore-backed override store.
 *
 * Overrides are keyed as "override_<flag.key>" so they never collide with
 * the remote-flag DataStore ("smartpos_feature_flags_remote").
 * Values persist across app restarts and are cleared by [clearAllOverrides].
 */
@Singleton
class FeatureFlagOverrideStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FeatureFlagOverrideStore {

    private val dataStore: DataStore<Preferences> = context.flagOverrideDataStore

    private fun prefKey(flag: FeatureFlag) =
        booleanPreferencesKey("override_${flag.key}")

    override fun observeOverrides(): Flow<Map<FeatureFlag, Boolean?>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            FeatureFlag.entries.associateWith { flag -> prefs[prefKey(flag)] }
        }

    override suspend fun setOverride(flag: FeatureFlag, enabled: Boolean) {
        dataStore.edit { prefs -> prefs[prefKey(flag)] = enabled }
    }

    override suspend fun clearOverride(flag: FeatureFlag) {
        dataStore.edit { prefs -> prefs.remove(prefKey(flag)) }
    }

    override suspend fun clearAllOverrides() {
        dataStore.edit { prefs ->
            FeatureFlag.entries.forEach { flag -> prefs.remove(prefKey(flag)) }
        }
    }
}

