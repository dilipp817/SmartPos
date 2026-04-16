package com.autobill.smartpos.domain.usecase

import android.util.Log
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.AdminStats
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.domain.repository.FoodRepository
import com.autobill.smartpos.domain.repository.OfflineQueueRepository
import com.autobill.smartpos.domain.repository.OrderRepository
import com.autobill.smartpos.domain.repository.TableRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

// ── Food CRUD ─────────────────────────────────────────────────────────────────

/**
 * Create a new food item (admin only — server enforces 403 for other roles).
 */
class CreateFoodUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        restaurantId: Long,
        name: String,
        price: Double,
        description: String? = null,
        imageUrl: String? = null,
        categoryId: Long? = null,
        isVegetarian: Boolean = false,
        isSpicy: Boolean = false,
        preparationTime: Int? = null,
        allergens: String? = null,
        calories: Int? = null,
    ): Result<Food> = repository.createFood(
        restaurantId    = restaurantId,
        name            = name,
        price           = price,
        description     = description,
        imageUrl        = imageUrl,
        categoryId      = categoryId,
        isVegetarian    = isVegetarian,
        isSpicy         = isSpicy,
        preparationTime = preparationTime,
        allergens       = allergens,
        calories        = calories,
    )
}

/**
 * Update an existing food item (admin only).
 */
class UpdateFoodUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(
        foodId: Long,
        restaurantId: Long,
        name: String,
        price: Double,
        description: String? = null,
        imageUrl: String? = null,
        categoryId: Long? = null,
        isVegetarian: Boolean = false,
        isSpicy: Boolean = false,
        isAvailable: Boolean = true,
        preparationTime: Int? = null,
        allergens: String? = null,
        calories: Int? = null,
    ): Result<Food> = repository.updateFood(
        foodId          = foodId,
        restaurantId    = restaurantId,
        name            = name,
        price           = price,
        description     = description,
        imageUrl        = imageUrl,
        categoryId      = categoryId,
        isVegetarian    = isVegetarian,
        isSpicy         = isSpicy,
        isAvailable     = isAvailable,
        preparationTime = preparationTime,
        allergens       = allergens,
        calories        = calories,
    )
}

/**
 * Delete a food item (admin only).
 */
class DeleteFoodUseCase @Inject constructor(
    private val repository: FoodRepository,
) {
    suspend operator fun invoke(foodId: Long): Result<Unit> =
        repository.deleteFood(foodId)
}

// ── Admin Dashboard Stats ─────────────────────────────────────────────────────

/**
 * Aggregates stats for the admin dashboard in a single parallel fetch.
 *
 * All four underlying calls run concurrently; the first to fail makes the whole
 * result partial — stats are returned with zero values for failed sub-queries
 * rather than surfacing an opaque error.
 */
class GetAdminStatsUseCase @Inject constructor(
    private val orderRepository: OrderRepository,
    private val tableRepository: TableRepository,
    private val foodRepository: FoodRepository,
    private val offlineQueueRepository: OfflineQueueRepository,
) {
    suspend operator fun invoke(restaurantId: Long): AdminStats = coroutineScope {
        // Build today's ISO-8601 date range using Calendar (API 24-safe)
        val cal = Calendar.getInstance()
        val today = "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
        )
        val startDate = "${today}T00:00:00"
        val endDate   = "${today}T23:59:59"

        val activeOrdersDeferred = async {
            when (val r = orderRepository.getActiveOrders(restaurantId)) {
                is Result.Success -> r.data.size
                else              -> 0
            }
        }

        val tablesDeferred = async {
            when (val r = tableRepository.getAvailableTables(restaurantId)) {
                is Result.Success -> r.data.size
                else              -> 0
            }
        }

        val revenueDeferred = async {
            when (val r = orderRepository.getOrdersByDateRange(restaurantId, startDate, endDate)) {
                is Result.Success -> r.data
                    .filter { it.status == OrderStatus.DELIVERED }
                    .sumOf { it.totalAmount }
                else              -> 0.0
            }
        }

        val unavailableFoodDeferred = async {
            when (val r = foodRepository.getFoods()) {
                is Result.Success -> r.data.count { !it.isAvailable }
                else              -> 0
            }
        }

        val offlineCount = try {
            offlineQueueRepository.observePendingCount().first()
        } catch (e: Exception) {
            Log.w("GetAdminStatsUseCase", "Failed to read offline queue count", e)
            0
        }

        AdminStats(
            todayRevenue        = revenueDeferred.await(),
            activeOrders        = activeOrdersDeferred.await(),
            availableTables     = tablesDeferred.await(),
            offlineQueueCount   = offlineCount,
            unavailableFoodCount = unavailableFoodDeferred.await(),
        )
    }
}





