package com.autobill.smartpos.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autobill.smartpos.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "smartpos_session"
)

/**
 * Session storage split across two backends:
 *
 *  ┌─────────────────────────────────────────────────────────────────┐
 *  │  Plain DataStore  (non-sensitive session metadata)              │
 *  │  userId, username, email, role, restaurantId, expiresIn,        │
 *  │  deviceId, deviceType                                           │
 *  ├─────────────────────────────────────────────────────────────────┤
 *  │  SecureTokenStorage  (JWT only)                                 │
 *  │  AES-256-GCM via Android Keystore — never touches DataStore     │
 *  └─────────────────────────────────────────────────────────────────┘
 *
 *  The JWT is the one value that grants API access. Everything else is
 *  non-sensitive display/routing metadata.
 *
 *  Session liveness: driven by USER_ID in DataStore AND a non-null token
 *  from SecureTokenStorage. Both must exist for [observeUser] to emit a
 *  non-null User — so logout (which clears both) is atomic.
 */
@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTokenStorage: SecureTokenStorage,
) {
    private val dataStore: DataStore<Preferences> = context.sessionDataStore

    private object Keys {
        // TOKEN intentionally absent — stored in SecureTokenStorage instead
        val USER_ID     = longPreferencesKey("user_id")
        val USERNAME    = stringPreferencesKey("username")
        val EMAIL       = stringPreferencesKey("email")
        val ROLE        = stringPreferencesKey("role")
        val RESTAURANT_ID = longPreferencesKey("restaurant_id")
        /** Absolute Unix epoch seconds — persisted from login response `expires_at`. */
        val EXPIRES_AT  = longPreferencesKey("expires_at")
        val DEVICE_ID   = stringPreferencesKey("device_id")
        val DEVICE_TYPE = stringPreferencesKey("device_type")
    }

    /**
     * Observe the active session (null = logged out).
     *
     * USER_ID is the DataStore sentinel. When it is present we also check that
     * a token exists in SecureTokenStorage. If the token was wiped independently
     * (Keystore corruption recovery) the flow returns null, keeping the app safe.
     */
    fun observeUser(): Flow<User?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val userId = prefs[Keys.USER_ID] ?: return@map null
            // Cross-check: JWT must also exist — both cleared on logout / corruption
            val token = secureTokenStorage.getToken() ?: return@map null
            User(
                id = userId,
                username = prefs[Keys.USERNAME] ?: "",
                email = prefs[Keys.EMAIL] ?: "",
                role = prefs[Keys.ROLE] ?: "",
                restaurantId = prefs[Keys.RESTAURANT_ID],   // null = super_admin
                token = token,
                expiresAt = prefs[Keys.EXPIRES_AT] ?: 0L,
                deviceId = prefs[Keys.DEVICE_ID],
                deviceType = prefs[Keys.DEVICE_TYPE],
            )
        }

    /** Persist the full user session after login. */
    suspend fun saveUser(user: User) {
        // 1. Write the JWT to encrypted storage first (most critical)
        secureTokenStorage.saveToken(user.token)

        // 2. Write non-sensitive metadata to plain DataStore
        dataStore.edit { prefs ->
            prefs[Keys.USER_ID]  = user.id
            prefs[Keys.USERNAME] = user.username
            prefs[Keys.EMAIL]    = user.email
            prefs[Keys.ROLE]     = user.role
            prefs[Keys.EXPIRES_AT] = user.expiresAt
            val restaurantId = user.restaurantId
            if (restaurantId != null) prefs[Keys.RESTAURANT_ID] = restaurantId
            else prefs.remove(Keys.RESTAURANT_ID)
            val deviceId = user.deviceId
            if (deviceId != null) prefs[Keys.DEVICE_ID] = deviceId
            else prefs.remove(Keys.DEVICE_ID)
            val deviceType = user.deviceType
            if (deviceType != null) prefs[Keys.DEVICE_TYPE] = deviceType
            else prefs.remove(Keys.DEVICE_TYPE)
        }
    }

    /** Clear the session on logout — both stores, atomically. */
    suspend fun clearUser() {
        // Clear JWT first so any in-flight request sees an invalid session immediately
        secureTokenStorage.clearToken()
        dataStore.edit { it.clear() }
    }

    /**
     * Return the stored JWT token (for OkHttp interceptor — synchronous, no runBlocking needed).
     * Delegates entirely to SecureTokenStorage — DataStore is not involved.
     * Returns null if no session exists or if the Keystore was corrupted.
     */
    fun getToken(): String? = secureTokenStorage.getToken()

    /**
     * Return the stored restaurantId.
     * Always use this for restaurant-scoped API calls — never hardcode it.
     */
    suspend fun getRestaurantId(): Long? = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .firstOrNull()
        ?.get(Keys.RESTAURANT_ID)

    /**
     * Return the stored token expiry as an absolute Unix epoch second.
     * Returns 0 if not stored (e.g. very old session before this field existed).
     */
    suspend fun getExpiresAt(): Long = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .firstOrNull()
        ?.get(Keys.EXPIRES_AT) ?: 0L
}

