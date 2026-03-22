package com.autobill.smartpos.data.repository

import com.autobill.smartpos.data.di.IoDispatcher
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
 */
class FoodRepositoryImpl @Inject constructor(
    private val apiService: FoodApiService,
    private val foodDao: FoodDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FoodRepository {

    override suspend fun getFoods(): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            val remoteFoods = apiService.getFoods()
            foodDao.deleteAll()
            foodDao.upsertAll(remoteFoods.data.map { it.toEntity() })
            Result.Success(remoteFoods.data.map { it.toEntity().toDomain() })
        } catch (e: Exception) {
            // Fallback to cached data
            val cachedFoods = foodDao.getAllFoods()
            if (cachedFoods.isNotEmpty()) {
                Result.Success(cachedFoods.map { it.toDomain() })
            } else {
                Result.Failure(e)
            }
        }
    }

    override suspend fun getFoodsPaginated(
        offset: Int,
        limit: Int,
    ): PaginationResult<Food> = withContext(ioDispatcher) {
        try {
            val response = apiService.getFoods(offset = offset, limit = limit)
            
            // Cache the data (upsert to not lose items from previous pages)
            foodDao.upsertAll(response.data.map { it.toEntity() })
            
            // Create pagination object with response metadata
            val pagination = Pagination(
                data = response.data.map { it.toEntity().toDomain() },
                currentPage = response.currentPage,
                limit = response.limit,
                total = response.total,
                hasMore = response.hasMore,
            )
            
            val result: PaginationResult<Food> = PaginationResult.Success(pagination)
            result
        } catch (e: Exception) {
            // Fallback to cached data
            val cachedFoods = foodDao.getAllFoods()
            if (cachedFoods.isNotEmpty()) {
                val pagination = Pagination(
                    data = cachedFoods.map { it.toDomain() },
                    currentPage = 0,
                    limit = limit,
                    total = cachedFoods.size,
                    hasMore = false,
                )
                val result: PaginationResult<Food> = PaginationResult.Success(pagination)
                result
            } else {
                val result: PaginationResult<Food> = PaginationResult.Failure(e)
                result
            }
        }
    }

    override suspend fun getFoodById(id: Int): Result<Food> = withContext(ioDispatcher) {
        try {
            val food = apiService.getFoodById(id)
            Result.Success(food.toEntity().toDomain())
        } catch (e: Exception) {
            val cachedFood = foodDao.getFoodById(id)
            if (cachedFood != null) {
                Result.Success(cachedFood.toDomain())
            } else {
                Result.Failure(e)
            }
        }
    }

    override suspend fun searchFoods(query: String): Result<List<Food>> = withContext(ioDispatcher) {
        try {
            val results = apiService.searchFoods(query)
            Result.Success(results.data.map { it.toEntity().toDomain() })
        } catch (e: Exception) {
            val cachedResults = foodDao.searchFoods(query)
            if (cachedResults.isNotEmpty()) {
                Result.Success(cachedResults.map { it.toDomain() })
            } else {
                Result.Failure(e)
            }
        }
    }

    override suspend fun searchFoodsPaginated(
        query: String,
        offset: Int,
        limit: Int,
    ): PaginationResult<Food> = withContext(ioDispatcher) {
        try {
            val response = apiService.searchFoods(query = query, offset = offset, limit = limit)
            
            val pagination = Pagination(
                data = response.data.map { it.toEntity().toDomain() },
                currentPage = response.currentPage,
                limit = response.limit,
                total = response.total,
                hasMore = response.hasMore,
            )
            
            val result: PaginationResult<Food> = PaginationResult.Success(pagination)
            result
        } catch (e: Exception) {
            val cachedResults = foodDao.searchFoods(query)
            if (cachedResults.isNotEmpty()) {
                val pagination = Pagination(
                    data = cachedResults.map { it.toDomain() },
                    currentPage = 0,
                    limit = limit,
                    total = cachedResults.size,
                    hasMore = false,
                )
                val result: PaginationResult<Food> = PaginationResult.Success(pagination)
                result
            } else {
                val result: PaginationResult<Food> = PaginationResult.Failure(e)
                result
            }
        }
    }
}
