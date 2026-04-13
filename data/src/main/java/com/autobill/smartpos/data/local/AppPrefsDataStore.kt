package com.autobill.smartpos.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appPrefsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "smartpos_app_prefs")

/**
 * Stores app-wide UI preferences that persist across sessions.
 *
 * Unlike [SessionDataStore] and [RestaurantDataStore], this store is
 * **never cleared on logout** — preferences like theme belong to the device/user,
 * not to a restaurant session.
 */
@Singleton
class AppPrefsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.appPrefsDataStore

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    /** Observe the current theme preference. Emits `false` (light) until explicitly set. */
    fun observeIsDarkTheme(): Flow<Boolean> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> prefs[Keys.IS_DARK_THEME] ?: false }

    /** Persist the user's theme choice. */
    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.IS_DARK_THEME] = enabled }
    }
}

