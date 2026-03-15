package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.MenuItemEntity
import com.autobill.smartpos.data.local.entity.MenuItemVariantEntity
import com.autobill.smartpos.data.remote.dto.MenuItemDto
import com.autobill.smartpos.data.remote.dto.MenuItemVariantDto
import com.autobill.smartpos.domain.model.MenuItem
import com.autobill.smartpos.domain.model.MenuItemVariant

// Mapper Extension Functions: MenuItem and Variants
// Maps between DTO ↔ Entity ↔ Domain layers

// MenuItemVariant DTO to Domain
fun MenuItemVariantDto.toDomain(): MenuItemVariant = MenuItemVariant(
    id = id,
    name = name,
    priceModifier = priceModifier,
    description = description,
)

// MenuItemVariant Entity to Domain
fun MenuItemVariantEntity.toDomain(): MenuItemVariant = MenuItemVariant(
    id = id,
    name = name,
    priceModifier = priceModifier,
    description = description,
)

// MenuItemVariant DTO to Entity
fun MenuItemVariantDto.toEntity(menuItemId: Int): MenuItemVariantEntity = MenuItemVariantEntity(
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

// MenuItem Entity to Domain
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

// MenuItem DTO to Entity
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

