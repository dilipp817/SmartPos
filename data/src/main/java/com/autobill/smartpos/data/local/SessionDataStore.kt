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
 * DataStore-backed session storage.
 * Persists token + restaurantId across app restarts.
 * restaurantId is THE critical field for multi-outlet support — never hardcode it.
 */
@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.sessionDataStore

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val RESTAURANT_ID = longPreferencesKey("restaurant_id")
        val EXPIRES_IN = longPreferencesKey("expires_in")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val DEVICE_TYPE = stringPreferencesKey("device_type")
    }

    /** Observe the active session (null = logged out). */
    fun observeUser(): Flow<User?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val token = prefs[Keys.TOKEN] ?: return@map null
            val userId = prefs[Keys.USER_ID] ?: return@map null
            User(
                id = userId,
                username = prefs[Keys.USERNAME] ?: "",
                email = prefs[Keys.EMAIL] ?: "",
                role = prefs[Keys.ROLE] ?: "",
                restaurantId = prefs[Keys.RESTAURANT_ID],  // null = super_admin
                token = token,
                expiresIn = prefs[Keys.EXPIRES_IN] ?: 0L,
                deviceId = prefs[Keys.DEVICE_ID],
                deviceType = prefs[Keys.DEVICE_TYPE],
            )
        }

    /** Persist the full user session after login. */
    suspend fun saveUser(user: User) {
        dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = user.token
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.USERNAME] = user.username
            prefs[Keys.EMAIL] = user.email
            prefs[Keys.ROLE] = user.role
            prefs[Keys.EXPIRES_IN] = user.expiresIn
            // Use local vals for smart-cast — cross-module public properties cannot be cast directly
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

    /** Clear the session on logout. */
    suspend fun clearUser() {
        dataStore.edit { it.clear() }
    }

    /**
     * Return the stored JWT token synchronously (for OkHttp interceptor via runBlocking).
     * Returns null if no session exists.
     */
    suspend fun getToken(): String? = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .firstOrNull()
        ?.get(Keys.TOKEN)

    /**
     * Return the stored restaurantId.
     * Always use this value for restaurant-scoped API calls — never hardcode it.
     */
    suspend fun getRestaurantId(): Long? = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .firstOrNull()
        ?.get(Keys.RESTAURANT_ID)
}

