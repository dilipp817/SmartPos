package com.autobill.smartpos.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
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
        /**
         * Idempotency guard for the current in-flight payment.
         * Persisted BEFORE the network call so process-death cannot generate a new UUID
         * and cause a double charge — contract §7.3 (M-05).
         * Cleared on terminal success or failure.
         */
        val CURRENT_PAYMENT_REF_NUMBER = stringPreferencesKey("current_payment_ref_number")
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

    // ── Payment reference number (idempotency guard) — contract §7.3 ─────────

    /** Read the in-flight payment reference number, or null if none is active. */
    suspend fun getCurrentPaymentRefNumber(): String? =
        dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .firstOrNull()
            ?.get(Keys.CURRENT_PAYMENT_REF_NUMBER)

    /** Persist a reference number BEFORE making the payment network call. */
    suspend fun saveCurrentPaymentRefNumber(refNumber: String) {
        dataStore.edit { prefs -> prefs[Keys.CURRENT_PAYMENT_REF_NUMBER] = refNumber }
    }

    /** Clear the reference number after terminal success or failure. */
    suspend fun clearCurrentPaymentRefNumber() {
        dataStore.edit { prefs -> prefs.remove(Keys.CURRENT_PAYMENT_REF_NUMBER) }
    }
}

