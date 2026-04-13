package com.autobill.smartpos.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.RestaurantSettings
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
 * Local cache for restaurant details.
 *
 * Populated on every successful [RestaurantRepositoryImpl.getRestaurant] call.
 * Survives app restarts — avoids a network round-trip if the restaurant data
 * is only needed for display (name, currency, settings flags).
 *
 * Cache-invalidation strategy: cache is refreshed on every cold app start
 * (MainViewModel calls GetRestaurantUseCase after RecoverSessionUseCase).
 * The cache is cleared on logout ([clearRestaurant]).
 */
@Singleton
class RestaurantDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.restaurantDataStore

    private object Keys {
        val ID                       = longPreferencesKey("restaurant_id")
        val NAME                     = stringPreferencesKey("restaurant_name")
        val ADDRESS                  = stringPreferencesKey("restaurant_address")
        val PHONE                    = stringPreferencesKey("restaurant_phone")
        val EMAIL                    = stringPreferencesKey("restaurant_email")
        val LOGO_URL                 = stringPreferencesKey("restaurant_logo_url")
        val TIMEZONE                 = stringPreferencesKey("restaurant_timezone")
        val CURRENCY                 = stringPreferencesKey("restaurant_currency")
        val TAX_RATE                 = doublePreferencesKey("restaurant_tax_rate")
        val IS_ACTIVE                = booleanPreferencesKey("restaurant_is_active")
        // Settings
        val ENABLE_TIPS              = booleanPreferencesKey("restaurant_enable_tips")
        val DEFAULT_TIP_PERCENTAGE   = doublePreferencesKey("restaurant_default_tip_pct")
        val AUTO_PRINT_BILL          = booleanPreferencesKey("restaurant_auto_print_bill")
        val TAX_INCLUSIVE            = booleanPreferencesKey("restaurant_tax_inclusive")
        // Timestamps
        val CREATED_AT               = stringPreferencesKey("restaurant_created_at")
        val UPDATED_AT               = stringPreferencesKey("restaurant_updated_at")
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
            Restaurant(
                id        = id,
                name      = prefs[Keys.NAME]     ?: return@map null,
                address   = prefs[Keys.ADDRESS]  ?: "",
                phone     = prefs[Keys.PHONE]    ?: "",
                email     = prefs[Keys.EMAIL]    ?: "",
                logoUrl   = prefs[Keys.LOGO_URL],
                timezone  = prefs[Keys.TIMEZONE] ?: "Asia/Kolkata",
                currency  = prefs[Keys.CURRENCY] ?: "INR",
                taxRate   = prefs[Keys.TAX_RATE] ?: 18.0,
                isActive  = prefs[Keys.IS_ACTIVE] ?: true,
                settings  = RestaurantSettings(
                    enableTips           = prefs[Keys.ENABLE_TIPS]            ?: false,
                    defaultTipPercentage = prefs[Keys.DEFAULT_TIP_PERCENTAGE] ?: 0.0,
                    autoPrintBill        = prefs[Keys.AUTO_PRINT_BILL]        ?: false,
                    taxInclusive         = prefs[Keys.TAX_INCLUSIVE]          ?: false,
                ),
                createdAt = prefs[Keys.CREATED_AT] ?: "",
                updatedAt = prefs[Keys.UPDATED_AT] ?: "",
            )
        }

    // ── Write ─────────────────────────────────────────────────────────────────

    suspend fun saveRestaurant(restaurant: Restaurant) {
        dataStore.edit { prefs ->
            prefs[Keys.ID]                     = restaurant.id
            prefs[Keys.NAME]                   = restaurant.name
            prefs[Keys.ADDRESS]                = restaurant.address
            prefs[Keys.PHONE]                  = restaurant.phone
            prefs[Keys.EMAIL]                  = restaurant.email
            restaurant.logoUrl?.let            { prefs[Keys.LOGO_URL] = it }
            prefs[Keys.TIMEZONE]               = restaurant.timezone
            prefs[Keys.CURRENCY]               = restaurant.currency
            prefs[Keys.TAX_RATE]               = restaurant.taxRate
            prefs[Keys.IS_ACTIVE]              = restaurant.isActive
            prefs[Keys.ENABLE_TIPS]            = restaurant.settings.enableTips
            prefs[Keys.DEFAULT_TIP_PERCENTAGE] = restaurant.settings.defaultTipPercentage
            prefs[Keys.AUTO_PRINT_BILL]        = restaurant.settings.autoPrintBill
            prefs[Keys.TAX_INCLUSIVE]          = restaurant.settings.taxInclusive
            prefs[Keys.CREATED_AT]             = restaurant.createdAt
            prefs[Keys.UPDATED_AT]             = restaurant.updatedAt
        }
    }

    /** Called on logout to clear stale restaurant data from the device. */
    suspend fun clearRestaurant() {
        dataStore.edit { it.clear() }
    }
}

