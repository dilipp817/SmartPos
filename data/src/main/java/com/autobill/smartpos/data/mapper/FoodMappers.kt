package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.FoodEntity
import com.autobill.smartpos.data.remote.dto.FoodListItemDto
import com.autobill.smartpos.data.remote.dto.FoodResponseDto
import com.autobill.smartpos.domain.model.Food

// FoodListItemDto → FoodEntity
// backendapi.md §7: list endpoint now returns category_id, description, preparation_time,
// allergens, calories — use them directly.
fun FoodListItemDto.toEntity(restaurantId: Long = 0L): FoodEntity = FoodEntity(
    id = id,
    name = name,
    price = price,
    restaurantId = restaurantId,
    imageUrl = imageUrl,
    categoryId = categoryId,         // ← now available from list response
    categoryName = categoryName,
    description = description,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isSpicy = isSpicy,
    preparationTime = preparationTime,
    allergens = allergens,
    calories = calories,
)

// FoodResponseDto (detail) → FoodEntity — includes all fields
fun FoodResponseDto.toEntity(): FoodEntity = FoodEntity(
    id = id,
    name = name,
    price = price,
    restaurantId = restaurantId,
    imageUrl = imageUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    description = description,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isSpicy = isSpicy,
    preparationTime = preparationTime,
    allergens = allergens,
    calories = calories,
)

// FoodEntity → Food domain model
fun FoodEntity.toDomain(): Food = Food(
    id = id,
    name = name,
    price = price,
    restaurantId = restaurantId,
    imageUrl = imageUrl,
    categoryId = categoryId,
    categoryName = categoryName,
    description = description,
    isAvailable = isAvailable,
    isVegetarian = isVegetarian,
    isSpicy = isSpicy,
    preparationTime = preparationTime,
    allergens = allergens,
    calories = calories,
)
