package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Restaurant
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest
import com.autobill.smartpos.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Fetch restaurant details from the network and cache them locally.
 *
 * Called by [MainViewModel] on every cold app start (after session recovery)
 * so the rest of the app can consume restaurant settings via [ObserveRestaurantUseCase].
 *
 * Usage in ViewModel:
 * ```kotlin
 * val restaurantId = getRestaurantIdUseCase() ?: return
 * getRestaurantUseCase(restaurantId)   // result is automatically cached
 * ```
 */
class GetRestaurantUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke(restaurantId: Long): Result<Restaurant> =
        repository.getRestaurant(restaurantId)
}

/**
 * Observe cached restaurant details as a [Flow].
 *
 * Emits null until the first successful [GetRestaurantUseCase] call.
 * Subsequent app starts with a warm cache emit immediately.
 *
 * Typical use in a ViewModel:
 * ```kotlin
 * observeRestaurantUseCase()
 *     .onEach { restaurant -> _uiState.update { it.copy(restaurant = restaurant) } }
 *     .launchIn(viewModelScope)
 * ```
 */
class ObserveRestaurantUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(): Flow<Restaurant?> = repository.observeRestaurant()
}

/**
 * Update restaurant settings (admin / manager only).
 *
 * Sends a partial PATCH — only non-null fields in [request] are included in the request body.
 * On success the local cache is refreshed automatically.
 *
 * ⚠️ Server returns 403 if the caller's role is not manager / admin / super_admin.
 */
class UpdateRestaurantSettingsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        request: UpdateRestaurantSettingsRequest,
    ): Result<Restaurant> = repository.updateRestaurantSettings(restaurantId, request)
}

