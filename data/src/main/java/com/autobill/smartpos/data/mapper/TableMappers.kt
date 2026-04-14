package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.TableEntity
import com.autobill.smartpos.data.remote.dto.TableDto
import com.autobill.smartpos.domain.model.Table
import com.autobill.smartpos.domain.model.TableStatus

// Mapper Extension Functions: Table
// Maps between DTO ↔ Entity ↔ Domain layers

// Table DTO to Domain
// floor and lastOccupiedAt are now in the API response (backend V22 — BACKEND_ALIGNMENT.md Item 3)
fun TableDto.toDomain(): Table = Table(
    id = id,
    tableNumber = tableNumber,
    floor = floor,
    capacity = capacity,
    status = TableStatus.fromValue(status),
    currentOrderId = currentOrderId,
    lastOccupiedAt = lastOccupiedAt,
    restaurantId = restaurantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// Table Entity to Domain
fun TableEntity.toDomain(): Table = Table(
    id = id,
    tableNumber = tableNumber,
    floor = floor,
    capacity = capacity,
    status = TableStatus.fromValue(status),
    currentOrderId = currentOrderId,
    lastOccupiedAt = lastOccupiedAt,
    restaurantId = restaurantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

// Table DTO to Entity
fun TableDto.toEntity(): TableEntity = TableEntity(
    id             = id,
    tableNumber    = tableNumber,
    floor          = floor,
    capacity       = capacity,
    status         = status,
    currentOrderId = currentOrderId,
    lastOccupiedAt = lastOccupiedAt,
    restaurantId   = restaurantId,
    createdAt      = createdAt,
    updatedAt = updatedAt,
)

// Table Domain to Entity
fun Table.toEntity(): TableEntity = TableEntity(
    id = id,
    tableNumber = tableNumber,
    floor = floor,
    capacity = capacity,
    status = status.value,
    currentOrderId = currentOrderId,
    lastOccupiedAt = lastOccupiedAt,
    restaurantId = restaurantId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
