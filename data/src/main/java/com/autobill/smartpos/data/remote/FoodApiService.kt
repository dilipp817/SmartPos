package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.FoodDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API Service for Food operations.
 * Defines endpoints for retrieving food data from backend.
 */
interface FoodApiService {
    /**
     * Fetches all foods
     */
    @GET("foods")
    suspend fun getFoods(): List<FoodDto>

    /**
     * Fetches a single food by ID
     */
    @GET("foods/{id}")
    suspend fun getFoodById(
        @Path("id") id: Int,
    ): FoodDto

    /**
     * Searches foods by query
     */
    @GET("foods/search")
    suspend fun searchFoods(
        @Query("q") query: String,
    ): List<FoodDto>
}

