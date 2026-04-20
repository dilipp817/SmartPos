package com.autobill.smartpos.data.mapper

import com.autobill.smartpos.data.local.entity.OrderEntity
import com.autobill.smartpos.data.local.entity.OrderItemEntity
import com.autobill.smartpos.data.remote.dto.OrderDto
import com.autobill.smartpos.data.remote.dto.OrderItemDto
import com.autobill.smartpos.domain.model.ItemStatus
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.OrderItem
import com.autobill.smartpos.domain.model.OrderStatus
import com.autobill.smartpos.domain.model.OrderType

// ── OrderItemDto → Domain ────────────────────────────────────────────────────

fun OrderItemDto.toDomain(): OrderItem = OrderItem(
    id             = id,
    foodId         = foodId,
    foodName       = foodName,
    quantity       = quantity,
    unitPrice      = unitPrice,
    subtotal       = subtotal,
    itemStatus     = ItemStatus.fromValue(itemStatus),
    specialRequests = specialRequests,
    createdAt      = createdAt,
)

// ── OrderDto → Domain ────────────────────────────────────────────────────────

fun OrderDto.toDomain(): Order = Order(
    id          = id,
    restaurantId = restaurantId,
    tableId     = tableId,
    tableNumber = tableNumber,
    orderNumber = orderNumber,
    status      = OrderStatus.fromValue(status),
    orderType   = OrderType.fromValue(orderType),
    items       = items.map { it.toDomain() },
    subtotal    = subtotal,
    totalAmount = totalAmount,
    notes       = notes,
    createdAt   = createdAt,
    updatedAt   = updatedAt,
    version     = version,
)

// ── OrderDto → Entity (header only — items stored separately) ────────────────

fun OrderDto.toEntity(): OrderEntity = OrderEntity(
    id          = id,
    orderNumber = orderNumber,
    restaurantId = restaurantId,
    tableId     = tableId,
    tableNumber = tableNumber,
    orderType   = orderType,
    status      = status,
    subtotal    = subtotal,
    totalAmount = totalAmount,
    notes       = notes,
    createdAt   = createdAt,
    updatedAt   = updatedAt,
    version     = version,
)

// ── OrderItemDto → Entity ────────────────────────────────────────────────────

fun OrderItemDto.toEntity(orderId: Long): OrderItemEntity = OrderItemEntity(
    id              = id,
    orderId         = orderId,
    foodId          = foodId,
    foodName        = foodName,
    quantity        = quantity,
    unitPrice       = unitPrice,
    subtotal        = subtotal,
    itemStatus      = itemStatus,
    specialRequests = specialRequests,
    createdAt       = createdAt,
)

// ── Entity → Domain ──────────────────────────────────────────────────────────

fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    id              = id,
    foodId          = foodId,
    foodName        = foodName,
    quantity        = quantity,
    unitPrice       = unitPrice,
    subtotal        = subtotal,
    itemStatus      = ItemStatus.fromValue(itemStatus),
    specialRequests = specialRequests,
    createdAt       = createdAt,
)

fun OrderEntity.toDomain(items: List<OrderItemEntity> = emptyList()): Order = Order(
    id           = id,
    restaurantId = restaurantId,
    tableId      = tableId,
    tableNumber  = tableNumber,
    orderNumber  = orderNumber,
    status       = OrderStatus.fromValue(status),
    orderType    = OrderType.fromValue(orderType),
    items        = items.map { it.toDomain() },
    subtotal     = subtotal,
    totalAmount  = totalAmount,
    notes        = notes,
    createdAt    = createdAt,
    updatedAt    = updatedAt,
    version      = version,
)

// ── Domain → Entity (used by WebSocket cache writes in Phase 9.1) ────────────

fun Order.toEntity(): OrderEntity = OrderEntity(
    id          = id,
    orderNumber = orderNumber,
    restaurantId = restaurantId,
    tableId     = tableId,
    tableNumber = tableNumber,
    orderType   = orderType.value,
    status      = status.value,
    subtotal    = subtotal,
    totalAmount = totalAmount,
    notes       = notes,
    createdAt   = createdAt,
    updatedAt   = updatedAt,
    version     = version,
)

fun OrderItem.toEntity(orderId: Long): OrderItemEntity = OrderItemEntity(
    id              = id,
    orderId         = orderId,
    foodId          = foodId,
    foodName        = foodName,
    quantity        = quantity,
    unitPrice       = unitPrice,
    subtotal        = subtotal,
    itemStatus      = itemStatus.value,
    specialRequests = specialRequests,
    createdAt       = createdAt,
)
