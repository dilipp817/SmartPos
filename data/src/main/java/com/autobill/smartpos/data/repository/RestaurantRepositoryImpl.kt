package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.RestaurantDataStore
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toDto
import com.autobill.smartpos.data.remote.RestaurantApiService
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest
import com.autobill.smartpos.domain.repository.RestaurantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Restaurant repository implementation.
 *
 * Strategy: network-first with local DataStore cache.
 *  - [getRestaurant] always hits the network, then writes to cache.
 *  - [observeRestaurant] reads from cache only — no network call.
 *  - [updateRestaurantSettings] PATCHes the backend, then refreshes the cache on success.
 *
 * Cache is cleared by [com.autobill.smartpos.data.repository.AuthRepositoryImpl.clearSession]
 * on logout so stale data never persists across user sessions.
 */
@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val apiService: RestaurantApiService,
    private val restaurantDataStore: RestaurantDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RestaurantRepository {

    override suspend fun getRestaurant(id: Long): Result<Restaurant> =
        withContext(ioDispatcher) {
            try {
                val response = apiService.getRestaurant(id)
                val dto = checkNotNull(response.data) {
                    response.message ?: "Restaurant not found"
                }
                val restaurant = dto.toDomain()
                // Persist to local cache so it's available offline
                restaurantDataStore.saveRestaurant(restaurant)
                Result.Success(restaurant)
            } catch (e: Exception) {
                // Network failed — return cached value if available, else propagate the error
                val cached = restaurantDataStore.observeRestaurant().firstOrNull()
                if (cached != null) Result.Success(cached)
                else Result.Failure(e)
            }
        }

    override fun observeRestaurant(): Flow<Restaurant?> =
        restaurantDataStore.observeRestaurant()

    override suspend fun updateRestaurantSettings(
        id: Long,
        request: UpdateRestaurantSettingsRequest,
    ): Result<Restaurant> = withContext(ioDispatcher) {
        try {
            val response = apiService.updateRestaurant(id, request.toDto())
            val dto = checkNotNull(response.data) {
                response.message ?: "Failed to update restaurant settings"
            }
            val restaurant = dto.toDomain()
            // Keep cache in sync after a successful update
            restaurantDataStore.saveRestaurant(restaurant)
            Result.Success(restaurant)
        } catch (e: Exception) {
            Result.Failure(e)
        }
    }
}



