package com.autobill.smartpos.data.remote

import com.autobill.smartpos.data.remote.dto.FoodDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FoodApiService {
    @GET("restaurants/{restaurantId}/foods")
    suspend fun getFoods(
        @Path("restaurantId") restaurantId: Int,
    ): List<FoodDto>
}

