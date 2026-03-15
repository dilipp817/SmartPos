package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Order Item Request
// Used in Create/Update Order requests
@JsonClass(generateAdapter = true)
data class OrderItemRequestDto(
    @param:Json(name = "menu_item_id")
    val menuItemId: Int,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "special_instructions")
    val specialInstructions: String?,
    @param:Json(name = "variant_id")
    val variantId: Int?,
)

// DTO: Create Order Request
// Maps to API request for POST /restaurants/{restaurantId}/orders
@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @param:Json(name = "table_id")
    val tableId: Int,
    @param:Json(name = "customer_id")
    val customerId: Int?,
    @param:Json(name = "order_type")
    val orderType: String, // "dine_in", "takeaway", "delivery"
    @param:Json(name = "notes")
    val notes: String?,
    @param:Json(name = "items")
    val items: List<OrderItemRequestDto>,
)

// DTO: Order Item Response
// Included in Order response
@JsonClass(generateAdapter = true)
data class OrderItemDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "menu_item_id")
    val menuItemId: Int,
    @param:Json(name = "menu_item_name")
    val menuItemName: String,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "unit_price")
    val unitPrice: Double,
    @param:Json(name = "variant_id")
    val variantId: Int?,
    @param:Json(name = "variant_name")
    val variantName: String?,
    @param:Json(name = "special_instructions")
    val specialInstructions: String?,
    @param:Json(name = "subtotal")
    val subtotal: Double,
)

// DTO: Order Response
// Maps to API response from GET /restaurants/{restaurantId}/orders/{orderId}
@JsonClass(generateAdapter = true)
data class OrderDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "order_number")
    val orderNumber: String,
    @param:Json(name = "table_id")
    val tableId: Int,
    @param:Json(name = "customer_id")
    val customerId: Int?,
    @param:Json(name = "restaurant_id")
    val restaurantId: Int,
    @param:Json(name = "order_type")
    val orderType: String,
    @param:Json(name = "status")
    val status: String, // "pending", "confirmed", "completed", "cancelled"
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "tax")
    val tax: Double,
    @param:Json(name = "discount")
    val discount: Double,
    @param:Json(name = "total")
    val total: Double,
    @param:Json(name = "notes")
    val notes: String?,
    @param:Json(name = "items")
    val items: List<OrderItemDto>,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Update Order Request
@JsonClass(generateAdapter = true)
data class UpdateOrderRequest(
    @param:Json(name = "status")
    val status: String?,
    @param:Json(name = "notes")
    val notes: String?,
    @param:Json(name = "items_to_add")
    val itemsToAdd: List<OrderItemRequestDto>?,
    @param:Json(name = "items_to_remove")
    val itemsToRemove: List<Int>?,
)

// DTO: Complete Order Response
@JsonClass(generateAdapter = true)
data class CompleteOrderResponse(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "status")
    val status: String,
    @param:Json(name = "bill_id")
    val billId: Int,
    @param:Json(name = "completed_at")
    val completedAt: String,
)

// DTO: Paginated Orders Response
@JsonClass(generateAdapter = true)
data class PaginatedOrdersDto(
    @param:Json(name = "orders")
    val orders: List<OrderDto>,
    @param:Json(name = "pagination")
    val pagination: PaginationDto,
)

// DTO: Order Summary (for list views)
@JsonClass(generateAdapter = true)
data class OrderSummaryDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "order_number")
    val orderNumber: String,
    @param:Json(name = "table_id")
    val tableId: Int,
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "status")
    val status: String,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "tax")
    val tax: Double,
    @param:Json(name = "discount")
    val discount: Double,
    @param:Json(name = "total")
    val total: Double,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

