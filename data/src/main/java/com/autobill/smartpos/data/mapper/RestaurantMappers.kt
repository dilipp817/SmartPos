package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.RestaurantEntity
import com.autobill.smartpos.data.remote.dto.RestaurantDto
import com.autobill.smartpos.domain.model.Restaurant

// Mapper Extension Functions: Restaurant
// Maps between DTO ↔ Entity ↔ Domain layers

// DTO to Domain
fun RestaurantDto.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    address = address,
    phone = phone,
    email = email,
    logoUrl = logoUrl,
    timezone = timezone,
    currency = currency,
    taxRate = taxRate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// Entity to Domain
fun RestaurantEntity.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    address = address,
    phone = phone,
    email = email,
    logoUrl = logoUrl,
    timezone = timezone,
    currency = currency,
    taxRate = taxRate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// DTO to Entity
fun RestaurantDto.toEntity(): RestaurantEntity = RestaurantEntity(
    id = id,
    name = name,
    address = address,
    phone = phone,
    email = email,
    logoUrl = logoUrl,
    timezone = timezone,
    currency = currency,
    taxRate = taxRate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// Domain to Entity
fun Restaurant.toEntity(): RestaurantEntity = RestaurantEntity(
    id = id,
    name = name,
    address = address,
    phone = phone,
    email = email,
    logoUrl = logoUrl,
    timezone = timezone,
    currency = currency,
    taxRate = taxRate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

