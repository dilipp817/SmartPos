package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.FoodEntity
import com.autobill.smartpos.data.remote.dto.FoodDto
import com.autobill.smartpos.domain.model.Food

fun FoodDto.toEntity(): FoodEntity = FoodEntity(
    id = id,
    name = name,
    price = price,
    restaurantId = restaurantId,
    imageUrl = imageUrl,
    category = category,
    description = description,
    isAvailable = isAvailable,
)

fun FoodEntity.toDomain(): Food = Food(
    id = id,
    name = name,
    price = price,
    restaurantId = restaurantId,
    imageUrl = imageUrl,
    category = category,
    description = description,
    isAvailable = isAvailable,
)
