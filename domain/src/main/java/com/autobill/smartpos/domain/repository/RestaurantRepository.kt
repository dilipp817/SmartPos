package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for restaurant details and settings.
 *
 * Read path:
 *  - [getRestaurant] fetches from network and writes to local cache.
 *  - [observeRestaurant] emits from cache; never null after the first successful fetch.
 *
 * Write path:
 *  - [updateRestaurantSettings] sends PATCH to backend and refreshes the cache on success.
 *  - Role enforcement is done server-side (403 Forbidden for staff).
 */
interface RestaurantRepository {

    /**
     * Fetch restaurant details from the network and update the local cache.
     * Call this on app startup (after session recovery) and on pull-to-refresh.
     */
    suspend fun getRestaurant(id: Long): Result<Restaurant>

    /**
     * Observe cached restaurant details.
     * Emits null until the first successful [getRestaurant] call.
     * Use this in ViewModels that display restaurant info.
     */
    fun observeRestaurant(): Flow<Restaurant?>

    /**
     * Update restaurant settings via PATCH /restaurants/{id}.
     * Only non-null fields in [request] are sent to the backend.
     * On success, the local cache is refreshed.
     *
     * ⚠️ Role-gated: server returns 403 for staff/kitchen roles.
     */
    suspend fun updateRestaurantSettings(
        id: Long,
        request: UpdateRestaurantSettingsRequest,
    ): Result<Restaurant>
}

