package com.autobill.smartpos.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.RestaurantAddress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.restaurantDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "smartpos_restaurant")

/**
 * Local cache for restaurant details (MOBILE_GUIDE_REVIEW.md 1.1).
 *
 * Stores the new Restaurant model: outletName, displayName, outletManager, address.
 * Previous fields (phone, email, taxRate, currency, settings) removed.
 */
@Singleton
class RestaurantDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.restaurantDataStore

    private object Keys {
        val ID              = longPreferencesKey("restaurant_id")
        val OUTLET_NAME     = stringPreferencesKey("restaurant_outlet_name")
        val DISPLAY_NAME    = stringPreferencesKey("restaurant_display_name")
        val OUTLET_MANAGER  = stringPreferencesKey("restaurant_outlet_manager")
        // Address
        val ADDR_BUILDING   = stringPreferencesKey("restaurant_addr_building")
        val ADDR_STREET     = stringPreferencesKey("restaurant_addr_street")
        val ADDR_LOCATION   = stringPreferencesKey("restaurant_addr_location")
        val ADDR_ZIP_CODE   = stringPreferencesKey("restaurant_addr_zip_code")
        // Timestamps
        val CREATED_AT      = stringPreferencesKey("restaurant_created_at")
        val UPDATED_AT      = stringPreferencesKey("restaurant_updated_at")
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Observe the cached restaurant.
     * Emits null if the cache is empty (first launch or after logout).
     */
    fun observeRestaurant(): Flow<Restaurant?> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val id = prefs[Keys.ID] ?: return@map null
            val outletName = prefs[Keys.OUTLET_NAME] ?: return@map null
            Restaurant(
                id            = id,
                outletName    = outletName,
                displayName   = prefs[Keys.DISPLAY_NAME]   ?: "",
                outletManager = prefs[Keys.OUTLET_MANAGER] ?: "",
                address       = RestaurantAddress(
                    building  = prefs[Keys.ADDR_BUILDING] ?: "",
                    street    = prefs[Keys.ADDR_STREET]   ?: "",
                    location  = prefs[Keys.ADDR_LOCATION] ?: "",
                    zipCode   = prefs[Keys.ADDR_ZIP_CODE] ?: "",
                ),
                createdAt = prefs[Keys.CREATED_AT] ?: "",
                updatedAt = prefs[Keys.UPDATED_AT] ?: "",
            )
        }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveRestaurant(restaurant: Restaurant) {
        dataStore.edit { prefs ->
            prefs[Keys.ID]             = restaurant.id
            prefs[Keys.OUTLET_NAME]    = restaurant.outletName
            prefs[Keys.DISPLAY_NAME]   = restaurant.displayName
            prefs[Keys.OUTLET_MANAGER] = restaurant.outletManager
            prefs[Keys.ADDR_BUILDING]  = restaurant.address.building
            prefs[Keys.ADDR_STREET]    = restaurant.address.street
            prefs[Keys.ADDR_LOCATION]  = restaurant.address.location
            prefs[Keys.ADDR_ZIP_CODE]  = restaurant.address.zipCode
            prefs[Keys.CREATED_AT]     = restaurant.createdAt
            prefs[Keys.UPDATED_AT]     = restaurant.updatedAt
        }
    }

    /** Called on logout to clear stale restaurant data from the device. */
    suspend fun clearRestaurant() {
        dataStore.edit { it.clear() }
    }
}
