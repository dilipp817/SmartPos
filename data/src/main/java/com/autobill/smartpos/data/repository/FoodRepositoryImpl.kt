package com.autobill.smartpos.data.repository

import com.autobill.smartpos.domain.common.FoodResponse
import com.autobill.smartpos.data.local.dao.FoodDao
import com.autobill.smartpos.data.mapper.toDomain
import com.autobill.smartpos.data.mapper.toEntity
import com.autobill.smartpos.data.remote.FoodApiService
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.repository.FoodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class FoodRepositoryImpl(
    private val apiService: FoodApiService,
    private val foodDao: FoodDao,
    private val ioDispatcher: CoroutineDispatcher,
) : FoodRepository {

    override fun getFoods(restaurantId: Int): Flow<FoodResponse<List<Food>>> = flow {
        withContext(ioDispatcher) {
            val remoteFoods = apiService.getFoods(restaurantId)
            foodDao.deleteByRestaurant(restaurantId)
            foodDao.upsertAll(remoteFoods.map { it.toEntity() })
        }

        emitAll(
            foodDao.observeFoodsByRestaurant(restaurantId)
                .map { entities -> FoodResponse.Success(entities.map { it.toDomain() }) as FoodResponse<List<Food>> },
        )
    }.onStart {
        emit(FoodResponse.Loading)
    }.catch { throwable ->
        val fallbackFoods = withContext(ioDispatcher) {
            foodDao.getFoodsByRestaurant(restaurantId)
        }.map { it.toDomain() }

        if (fallbackFoods.isNotEmpty()) {
            emit(FoodResponse.Success(fallbackFoods))
        } else {
            emit(FoodResponse.Error(throwable.message ?: "Unable to load foods", throwable))
        }
    }
}
