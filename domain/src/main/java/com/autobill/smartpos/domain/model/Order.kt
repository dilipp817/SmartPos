package com.autobill.smartpos.domain.model

// Domain Model: Order Item
data class OrderItem(
    val id: Int,
    val menuItemId: Int,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantId: Int?,
    val variantName: String?,
    val specialInstructions: String?,
    val subtotal: Double,
)

// Domain Model: Order
// Independent of database or API structure
data class Order(
    val id: Int,
    val orderNumber: String,
    val tableId: Int,
    val customerId: Int?,
    val restaurantId: Int,
    val orderType: OrderType,
    val status: OrderStatus,
    val subtotal: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val notes: String?,
    val items: List<OrderItem>,
    val createdAt: String,
    val updatedAt: String,
)

// Order type enum
enum class OrderType(val value: String) {
    DINE_IN("dine_in"),
    TAKEAWAY("takeaway"),
    DELIVERY("delivery");

    companion object {
        fun fromValue(value: String): OrderType {
            return entries.find { it.value == value } ?: DINE_IN
        }
    }
}

// Order status enum
enum class OrderStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    PREPARING("preparing"),
    READY("ready"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    companion object {
        fun fromValue(value: String): OrderStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

// Domain Model: Order Summary (for list views)
data class OrderSummary(
    val id: Int,
    val orderNumber: String,
    val tableNumber: String,
    val status: OrderStatus,
    val total: Double,
    val createdAt: String,
)

