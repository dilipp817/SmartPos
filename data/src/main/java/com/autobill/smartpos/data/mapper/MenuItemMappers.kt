@file:Suppress("DEPRECATION")

package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.MenuItemEntity
import com.autobill.smartpos.data.local.entity.MenuItemVariantEntity
import com.autobill.smartpos.data.remote.dto.MenuItemDto
import com.autobill.smartpos.data.remote.dto.MenuItemVariantDto
import com.autobill.smartpos.domain.model.MenuItem
import com.autobill.smartpos.domain.model.MenuItemVariant

// DEPRECATED — April 12, 2026
// All functions in this file operate on deprecated entity classes (MenuItemEntity,
// MenuItemVariantEntity) that are no longer registered in AppDatabase.
// Kept for reference only. Do NOT add new callers.
// DTO → Domain functions (MenuItemVariantDto.toDomain, MenuItemDto.toDomain) remain usable
// since MenuItemDto is a DTO, not a deprecated entity.

// MenuItemVariant DTO to Domain
fun MenuItemVariantDto.toDomain(): MenuItemVariant = MenuItemVariant(
    id = id,
    name = name,
    priceModifier = priceModifier,
    description = description,
)

// MenuItemVariant Entity to Domain — DEPRECATED (entity is deprecated)
@Suppress("DEPRECATION")
@Deprecated("MenuItemVariantEntity is deprecated. No local caching for variants in POS v1.")
fun MenuItemVariantEntity.toDomain(): MenuItemVariant = MenuItemVariant(
    id = id,
    name = name,
    priceModifier = priceModifier,
    description = description,
)

// MenuItemVariant DTO to Entity — DEPRECATED (entity is deprecated)
@Suppress("DEPRECATION")
@Deprecated("MenuItemVariantEntity is deprecated. No local caching for variants in POS v1.")
fun MenuItemVariantDto.toEntity(menuItemId: Long): MenuItemVariantEntity = MenuItemVariantEntity(
    id = id,
    menuItemId = menuItemId,
    name = name,
    priceModifier = priceModifier,
    description = description,
)

// MenuItem DTO to Domain
fun MenuItemDto.toDomain(): MenuItem = MenuItem(
    id = id,
    name = name,
    description = description,
    category = category,
    price = price,
    cost = cost,
    imageUrl = imageUrl,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    isAvailable = isAvailable,
    preparationTimeMinutes = preparationTimeMinutes,
    restaurantId = restaurantId,
    ingredients = ingredients,
    allergens = allergens,
    variants = variants?.map { it.toDomain() },
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// MenuItem Entity to Domain — DEPRECATED (entity is deprecated)
@Suppress("DEPRECATION")
@Deprecated("MenuItemEntity is deprecated. Use FoodEntity/FoodMappers instead.")
fun MenuItemEntity.toDomain(variants: List<MenuItemVariant>? = null): MenuItem = MenuItem(
    id = id,
    name = name,
    description = description,
    category = category,
    price = price,
    cost = cost,
    imageUrl = imageUrl,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    isAvailable = isAvailable,
    preparationTimeMinutes = preparationTimeMinutes,
    restaurantId = restaurantId,
    ingredients = null,
    allergens = null,
    variants = variants,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// MenuItem DTO to Entity — DEPRECATED (entity is deprecated)
@Suppress("DEPRECATION")
@Deprecated("MenuItemEntity is deprecated. Use FoodEntity/FoodMappers instead.")
fun MenuItemDto.toEntity(): MenuItemEntity = MenuItemEntity(
    id = id,
    name = name,
    description = description,
    category = category,
    price = price,
    cost = cost,
    imageUrl = imageUrl,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    isAvailable = isAvailable,
    preparationTimeMinutes = preparationTimeMinutes,
    restaurantId = restaurantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

