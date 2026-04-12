package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
import com.autobill.smartpos.data.local.SessionDataStore
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.FoodApiService
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
            // GET /foods/restaurant/{restaurantId} — response is PagedDataDto, unwrap .data?.data
            val items = apiService.getFoodsByRestaurant(
                restaurantId = restaurantId,
                page = 0,
                limit = 100,
            ).data?.data.orEmpty()
            foodDao.deleteAll()
            foodDao.upsertAll(items.map { it.toEntity() })
            Result.Success(items.map { it.toEntity().toDomain() })
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
            // Prefer restaurantId passed by caller (from GetRestaurantIdUseCase in ViewModel).
            // Fall back to SessionDataStore for backward compatibility.
            val effectiveRestaurantId = restaurantId ?: sessionDataStore.getRestaurantId()
                ?: return@withContext PaginationResult.Failure(Exception("No restaurant ID in session — not logged in"))
            // GET /foods/restaurant/{id} uses page-based pagination (0-indexed page number).
            // Domain layer passes offset (item index); convert: page = offset / limit.
            val page = if (limit > 0) offset / limit else 0
            val response = apiService.getFoodsByRestaurant(
                restaurantId = effectiveRestaurantId,
                page = page,
                limit = limit,
            )
            val pagedData = response.data
            val items = pagedData?.data.orEmpty()
            foodDao.upsertAll(items.map { it.toEntity() })

            PaginationResult.Success(Pagination(
                data = items.map { it.toEntity().toDomain() },
                currentPage = pagedData?.pagination?.currentPage ?: page,
                limit = pagedData?.pagination?.limit ?: limit,
                total = pagedData?.pagination?.total ?: items.size,
                hasMore = pagedData?.pagination?.hasNext ?: false,
            ))
        } catch (e: Exception) {
            val cached = foodDao.getAllFoods()
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
        restaurantId: Long?,  // accepted but not used in search API — scoped by auth token
    ): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            val items = apiService.searchFoods(query = query).data?.data.orEmpty()
            Result.Success(items.map { it.toEntity().toDomain() })
        } catch (e: Exception) {
            val cached = foodDao.searchFoods(query)
            if (cached.isNotEmpty()) Result.Success(cached.map { it.toDomain() })
            else Result.Failure(e)
        }
    }

    override suspend fun searchFoodsPaginated(
        query: String,
        restaurantId: Long?,  // accepted but not used in search API — scoped by auth token
        offset: Int,
        limit: Int,
    ): PaginationResult<Food> = withContext(ioDispatcher) {
        try {
            val response = apiService.searchFoods(query = query, offset = offset, limit = limit)
            val page = response.data
            val items = page?.data.orEmpty()

            PaginationResult.Success(Pagination(
                data = items.map { it.toEntity().toDomain() },
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
}

