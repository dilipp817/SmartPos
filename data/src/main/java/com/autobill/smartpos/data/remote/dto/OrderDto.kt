package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Order Item in create/add-item requests
// food_id (NOT menu_item_id), special_requests (NOT special_instructions), no variants
@JsonClass(generateAdapter = true)
data class OrderItemRequestDto(
    @param:Json(name = "food_id")
    val foodId: Long,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "special_requests")
    val specialRequests: String? = null,
)

// DTO: Create Order Request
// POST /api/v1/restaurants/{restaurantId}/orders
@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @param:Json(name = "table_id")
    val tableId: Long,
    @param:Json(name = "items")
    val items: List<OrderItemRequestDto>,
    @param:Json(name = "order_type")
    val orderType: String = "DINE_IN", // DINE_IN  TAKEAWAY  DELIVERY
    @param:Json(name = "notes")
    val notes: String? = null,
    @param:Json(name = "customer_id")
    val customerId: Long? = null,   // always null in v1; send when customer linking ships in v2
)

// DTO: Order Item response
@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "food_id")
    val foodId: Long,
    @param:Json(name = "food_name")
    val foodName: String,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "unit_price")
    val unitPrice: Double,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "item_status")
    val itemStatus: String, // PENDING  IN_PROGRESS  READY  SERVED  CANCELLED (confirmed Q3)
    @param:Json(name = "special_requests")
    val specialRequests: String? = null,
    @param:Json(name = "created_at")
    val createdAt: String,
)

// DTO: Order response
// Status values UPPERCASE: PENDING | IN_PROGRESS | COMPLETED | DELIVERED | CANCELLED | HOLD
// OrderType UPPERCASE: DINE_IN | TAKEAWAY | DELIVERY (changed from OFFLINE/ONLINE — V19 migration)
// customer_id: always null in v1; backend populates in v2 (Q1, BACKEND_ALIGNMENT.md)
@JsonClass(generateAdapter = true)
data class OrderDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "table_id")
    val tableId: Long,
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "order_number")
    val orderNumber: String,
    @param:Json(name = "status")
    val status: String, // PENDING | IN_PROGRESS | COMPLETED | DELIVERED | CANCELLED | HOLD
    @param:Json(name = "order_type")
    val orderType: String, // DINE_IN | TAKEAWAY | DELIVERY
    @param:Json(name = "items")
    val items: List<OrderItemDto>,
    @param:Json(name = "subtotal")
    val subtotal: Double,       // pre-tax item total; equals totalAmount at order stage
    @param:Json(name = "total_amount")
    val totalAmount: Double,
    @param:Json(name = "notes")
    val notes: String? = null,
    @param:Json(name = "customer_id")
    val customerId: Long? = null,   // always null in v1; populated in v2
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
    @param:Json(name = "version")
    val version: Long = 1,
)

// DTO: Order list response wrapper
// GET /api/v1/restaurants/{restaurantId}/orders
// API shape: { "orders": [...], "total": 12 }
// ⚠️ backendapi.md does not include "status" in the data body — default guards against crash.
@JsonClass(generateAdapter = true)
data class OrderListDto(
    @param:Json(name = "orders")
    val orders: List<OrderDto>,
    @param:Json(name = "total")
    val total: Int,
    @param:Json(name = "status")
    val status: String = "success",
)

// DTO: Update Order Status Request
// PATCH /api/v1/restaurants/{restaurantId}/orders/{orderId}/status
@JsonClass(generateAdapter = true)
data class UpdateOrderStatusRequest(
    @param:Json(name = "status")
    val status: String,
)

// DTO: Update Order Item Request
// PUT /api/v1/restaurants/{restaurantId}/orders/{orderId}/items/{itemId}
@JsonClass(generateAdapter = true)
data class UpdateOrderItemRequest(
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "special_requests")
    val specialRequests: String? = null,
)

// DTO: Pending orders count response
// GET .../orders/count/pending
@JsonClass(generateAdapter = true)
data class PendingOrdersCountDto(
    @param:Json(name = "pending_count")
    val pendingCount: Int,
)
