package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.SessionDataStore
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.data.remote.dto.CreateFoodRequest
import com.autobill.smartpos.data.remote.dto.UpdateFoodRequest
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.PaginationResult
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of FoodRepository.
 * Handles data access from remote API and local database with proper error handling.
 * Implements offline-first caching strategy with pagination support for infinite scroll.
 *
 * Uses GET /foods/restaurant/{restaurantId} (backendapi.md §7) — the primary documented endpoint.
 * restaurantId is always read from SessionDataStore (set at login) — never hardcoded.
 */
class FoodRepositoryImpl @Inject constructor(
    private val apiService: FoodApiService,
    private val foodDao: FoodDao,
    private val sessionDataStore: SessionDataStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FoodRepository {

    override suspend fun getFoods(): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            val restaurantId = sessionDataStore.getRestaurantId()
                ?: return@withContext Result.Failure(Exception("No restaurant ID in session — not logged in"))
            // M-14: use GET /foods?restaurant_id= (offset-based) instead of legacy endpoint
            // Backend confirmed: is_available is not a server-side filter param.
            // Filter client-side so unavailable/out-of-stock items never appear on the order menu.
            val items = apiService.getFoods(
                restaurantId = restaurantId,
                offset       = 0,
                limit        = 100,
            ).data?.data.orEmpty().filter { it.isAvailable }
            foodDao.deleteAll()
            foodDao.upsertAll(items.map { it.toEntity(restaurantId) })
            Result.Success(items.map { it.toEntity(restaurantId).toDomain() })
        } catch (e: Exception) {
            val cached = foodDao.getAllFoods()
            if (cached.isNotEmpty()) Result.Success(cached.map { it.toDomain() })
            else Result.Failure(e)
        }
    }

    override suspend fun getFoodsPaginated(
        restaurantId: Long?,
        offset: Int,
        limit: Int,
        category: String?,
        sort: String?,
    ): PaginationResult<Food> = withContext(ioDispatcher) {
        try {
            val effectiveRestaurantId = restaurantId ?: sessionDataStore.getRestaurantId()
                ?: return@withContext PaginationResult.Failure(Exception("No restaurant ID in session — not logged in"))
            // M-14: use GET /foods with offset-based pagination and full filter support
            // Backend confirmed: is_available is not a server-side filter param.
            // Filter client-side so unavailable items never appear on the order menu.
            val response = apiService.getFoods(
                restaurantId = effectiveRestaurantId,
                offset       = offset,
                limit        = limit,
                sort         = sort,
                categoryId   = category?.toLongOrNull(),
            )
            val pagedData = response.data
            val items = pagedData?.data.orEmpty().filter { it.isAvailable }
            foodDao.upsertAll(items.map { it.toEntity(effectiveRestaurantId) })

            PaginationResult.Success(Pagination(
                data        = items.map { it.toEntity(effectiveRestaurantId).toDomain() },
                currentPage = pagedData?.pagination?.currentPage ?: 0,
                limit       = pagedData?.pagination?.limit ?: limit,
                total       = pagedData?.pagination?.total ?: items.size,
                hasMore     = pagedData?.pagination?.hasNext ?: false,
            ))
        } catch (e: Exception) {
            val cached = foodDao.getAllFoods()
            if (cached.isNotEmpty()) {
                PaginationResult.Success(Pagination(
                    data        = cached.map { it.toDomain() },
                    currentPage = 0,
                    limit       = limit,
                    total       = cached.size,
                    hasMore     = false,
                ))
            } else {
                PaginationResult.Failure(e)
            }
        }
    }

    override suspend fun getFoodById(id: Long): Result<Food> = withContext(ioDispatcher) {
        try {
            val food = apiService.getFoodById(id).data
                ?: return@withContext Result.Failure(Exception("Food not found"))
            Result.Success(food.toEntity().toDomain())
        } catch (e: Exception) {
            val cached = foodDao.getFoodById(id)
            if (cached != null) Result.Success(cached.toDomain())
            else Result.Failure(e)
        }
    }

    override suspend fun searchFoods(
        query: String,
        restaurantId: Long?,
    ): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            // M-11: always pass restaurant_id to scope results to the current outlet
            val effectiveRestaurantId = restaurantId ?: sessionDataStore.getRestaurantId()
                ?: return@withContext Result.Failure(Exception("No restaurant ID in session — not logged in"))
            val items = apiService.searchFoods(
                query        = query,
                restaurantId = effectiveRestaurantId,
            ).data?.data.orEmpty()
            Result.Success(items.map { it.toEntity(effectiveRestaurantId).toDomain() })
        } catch (e: Exception) {
            val cached = foodDao.searchFoods(query)
            if (cached.isNotEmpty()) Result.Success(cached.map { it.toDomain() })
            else Result.Failure(e)
        }
    }

    override suspend fun searchFoodsPaginated(
        query: String,
        restaurantId: Long?,
        offset: Int,
        limit: Int,
    ): PaginationResult<Food> = withContext(ioDispatcher) {
        try {
            // M-11: always pass restaurantId to prevent cross-tenant data leak
            val effectiveRestaurantId = restaurantId ?: sessionDataStore.getRestaurantId()
                ?: return@withContext PaginationResult.Failure(Exception("No restaurant ID in session — not logged in"))
            val response = apiService.searchFoods(query = query, restaurantId = effectiveRestaurantId, offset = offset, limit = limit)
            val page = response.data
            val items = page?.data.orEmpty()

            PaginationResult.Success(Pagination(
                data = items.map { it.toEntity(effectiveRestaurantId).toDomain() },
                currentPage = page?.pagination?.currentPage ?: 0,
                limit = page?.pagination?.limit ?: limit,
                total = page?.pagination?.total ?: items.size,
                hasMore = page?.pagination?.hasNext ?: false,
            ))
        } catch (e: Exception) {
            val cached = foodDao.searchFoods(query)
            if (cached.isNotEmpty()) {
                PaginationResult.Success(Pagination(
                    data = cached.map { it.toDomain() },
                    currentPage = 0,
                    limit = limit,
                    total = cached.size,
                    hasMore = false,
                ))
            } else {
                PaginationResult.Failure(e)
            }
        }
    }

    // ── Admin mutations ───────────────────────────────────────────────────────

    override suspend fun createFood(
        restaurantId: Long, name: String, price: Double, description: String?,
        imageUrl: String?, categoryId: Long?, isVegetarian: Boolean, isSpicy: Boolean,
        preparationTime: Int?, allergens: String?, calories: Int?,
    ): Result<Food> = withContext(ioDispatcher) {
        try {
            val request = CreateFoodRequest(
                name = name, price = price, description = description,
                imageUrl = imageUrl, restaurantId = restaurantId,
                categoryId = categoryId, isVegetarian = isVegetarian, isSpicy = isSpicy,
            )
            val food = apiService.createFood(restaurantId, request).data
                ?: return@withContext Result.Failure(Exception("Empty response from server"))
            val entity = food.toEntity()
            foodDao.upsertAll(listOf(entity))
            Result.Success(entity.toDomain())
        } catch (e: Exception) { Result.Failure(e) }
    }

    override suspend fun updateFood(
        foodId: Long, restaurantId: Long, name: String, price: Double, description: String?,
        imageUrl: String?, categoryId: Long?, isVegetarian: Boolean, isSpicy: Boolean,
        isAvailable: Boolean, preparationTime: Int?, allergens: String?, calories: Int?,
    ): Result<Food> = withContext(ioDispatcher) {
        try {
            val request = UpdateFoodRequest(
                name = name, price = price, description = description,
                imageUrl = imageUrl, restaurantId = restaurantId, categoryId = categoryId,
                isVegetarian = isVegetarian, isSpicy = isSpicy, isAvailable = isAvailable,
                preparationTime = preparationTime, allergens = allergens, calories = calories,
            )
            val food = apiService.updateFood(foodId, request).data
                ?: return@withContext Result.Failure(Exception("Empty response from server"))
            val entity = food.toEntity()
            foodDao.upsertAll(listOf(entity))
            Result.Success(entity.toDomain())
        } catch (e: Exception) { Result.Failure(e) }
    }

    override suspend fun deleteFood(foodId: Long): Result<Unit> = withContext(ioDispatcher) {
        try {
            val response = apiService.deleteFood(foodId)
            if (!response.isSuccessful) {
                return@withContext Result.Failure(Exception("Delete failed with HTTP ${response.code()}"))
            }
            // Remove from local cache
            foodDao.deleteById(foodId)
            Result.Success(Unit)
        } catch (e: Exception) { Result.Failure(e) }
    }
}

