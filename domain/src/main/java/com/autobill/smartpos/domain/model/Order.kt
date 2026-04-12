package com.autobill.smartpos.domain.model

// Domain Model: Order Item
// Item 7: renamed fields, removed variants, added itemStatus + specialRequests + createdAt
// Q3 fix: itemStatus is now ItemStatus enum (confirmed by backend April 12, 2026)
data class OrderItem(
    val id: Long,
    val foodId: Long,
    val foodName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val itemStatus: ItemStatus,
    val specialRequests: String?,
    val createdAt: String,
)

// Item-level status enum — confirmed by backend (Q3, April 12, 2026)
// Drives the Kitchen Display Screen (KDS).
//
// Valid progressions:
//   PENDING → IN_PROGRESS → READY → SERVED   (normal flow)
//   PENDING → CANCELLED                       (removed before cooking)
//   IN_PROGRESS → CANCELLED                   (rare)
//
// ⚠️ Backend does NOT enforce transition order — any value can be set from any other.
//    KDS UI is responsible for enforcing forward-only progression.
// ⚠️ Items in READY, SERVED, or CANCELLED state are LOCKED on the server —
//    quantity/special_requests edits return 400 BAD REQUEST.
// ⚠️ Order status does NOT auto-update when all items reach a final state —
//    must be updated manually via PATCH .../orders/{id}/status.
enum class ItemStatus(val value: String) {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    READY("READY"),           // item prepared, waiting to be picked up/served
    SERVED("SERVED"),         // item delivered to table (final)
    CANCELLED("CANCELLED");   // item removed from order (final)

    companion object {
        fun fromValue(value: String): ItemStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

// Domain Model: Order
// Item 8 (complete): removed customerId/tax/discount, renamed total→totalAmount,
//                    added tableNumber + version + subtotal (pre-tax item sum, = totalAmount at order stage)
// Item 9: OrderType values now UPPERCASE — backend confirmed DINE_IN/TAKEAWAY/DELIVERY
// Q1 (BACKEND_ALIGNMENT.md April 12, 2026): customerId re-added as nullable for v2 readiness.
//   Always null in v1 — backend will populate when customer-linking ships in v2.
data class Order(
    val id: Long,
    val restaurantId: Long,
    val tableId: Long,
    val tableNumber: String,
    val orderNumber: String,
    val status: OrderStatus,
    val orderType: OrderType,
    val items: List<OrderItem>,
    val subtotal: Double,       // pre-tax item total — equals totalAmount at order stage
    val totalAmount: Double,
    val notes: String?,
    val createdAt: String,
    val updatedAt: String,
    val version: Long,
    val customerId: Long? = null,   // always null in v1; populated in v2 when customer linking ships
)

// Order type enum — Item 9: values confirmed UPPERCASE by backend (was OFFLINE/ONLINE)
enum class OrderType(val value: String) {
    DINE_IN("DINE_IN"),
    TAKEAWAY("TAKEAWAY"),
    DELIVERY("DELIVERY");

    companion object {
        fun fromValue(value: String): OrderType {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: DINE_IN
        }
    }
}

// Order status enum
// Item 2: replaced CONFIRMED/PREPARING/READY with backend-aligned statuses (all UPPERCASE)
//
// Valid transitions:
//   PENDING     → IN_PROGRESS, HOLD, CANCELLED
//   IN_PROGRESS → COMPLETED, CANCELLED
//   HOLD        → IN_PROGRESS, CANCELLED
//   COMPLETED   → DELIVERED
//   DELIVERED   → (final)
//   CANCELLED   → (final)
enum class OrderStatus(val value: String) {
    PENDING("PENDING"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED"),
    HOLD("HOLD");

    companion object {
        fun fromValue(value: String): OrderStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

// Domain Model: Order Summary (for list views)
// Item 14: added tableId, renamed total→totalAmount, added updatedAt
data class OrderSummary(
    val id: Long,
    val orderNumber: String,
    val tableId: Long,
    val tableNumber: String,
    val status: OrderStatus,
    val totalAmount: Double,
    val createdAt: String,
    val updatedAt: String,
)
