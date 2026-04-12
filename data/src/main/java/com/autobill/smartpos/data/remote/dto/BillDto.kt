package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Bill Item (line item on a bill)
@JsonClass(generateAdapter = true)
data class BillItemDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "bill_id")
    val billId: Long,
    @param:Json(name = "food_id")
    val foodId: Long,
    @param:Json(name = "food_name")
    val foodName: String,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "unit_price")
    val unitPrice: Double,
    @param:Json(name = "item_total")
    val itemTotal: Double,
    @param:Json(name = "created_at")
    val createdAt: String,
)

// DTO: Full Bill response
// GET /api/v1/bills/{id}  |  POST .../generate-bill  |  POST /api/v1/bills
// Status: ISSUED | PARTIAL | PAID | CANCELLED
// paid_amount + remaining_amount added by backend in V22 migration (BACKEND_ALIGNMENT.md Item 4)
@JsonClass(generateAdapter = true)
data class BillDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "bill_number")
    val billNumber: String,
    @param:Json(name = "order_id")
    val orderId: Long,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "restaurant_name")
    val restaurantName: String? = null,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "tax_amount")
    val taxAmount: Double,
    @param:Json(name = "cgst_amount")
    val cgstAmount: Double,
    @param:Json(name = "sgst_amount")
    val sgstAmount: Double,
    @param:Json(name = "discount_amount")
    val discountAmount: Double,
    @param:Json(name = "total_amount")
    val totalAmount: Double,
    @param:Json(name = "paid_amount")
    val paidAmount: Double = 0.0,       // cumulative paid; 0.0 on ISSUED
    @param:Json(name = "remaining_amount")
    val remainingAmount: Double = 0.0,  // = totalAmount - paidAmount; 0.0 on PAID
    @param:Json(name = "status")
    val status: String, // ISSUED | PARTIAL | PAID | CANCELLED
    @param:Json(name = "bill_items")
    val billItems: List<BillItemDto>,
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "updated_at")
    val updatedAt: String,
)

// DTO: Bill summary (used in GET /api/v1/bills list)
@JsonClass(generateAdapter = true)
data class BillSummaryDto(
    @param:Json(name = "id")
    val id: Long,
    @param:Json(name = "bill_number")
    val billNumber: String,
    @param:Json(name = "order_id")
    val orderId: Long,
    @param:Json(name = "restaurant_name")
    val restaurantName: String? = null,
    @param:Json(name = "total_amount")
    val totalAmount: Double,
    @param:Json(name = "status")
    val status: String,
    @param:Json(name = "created_at")
    val createdAt: String,
)

// DTO: Manual Bill Create Request
// POST /api/v1/bills  (prefer POST .../generate-bill for auto-computed amounts)
@JsonClass(generateAdapter = true)
data class CreateBillRequest(
    @param:Json(name = "bill_number")
    val billNumber: String? = null,
    @param:Json(name = "order_id")
    val orderId: Long,
    @param:Json(name = "restaurant_id")
    val restaurantId: Long,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "tax_amount")
    val taxAmount: Double = 0.0,
    @param:Json(name = "cgst_amount")
    val cgstAmount: Double = 0.0,
    @param:Json(name = "sgst_amount")
    val sgstAmount: Double = 0.0,
    @param:Json(name = "discount_amount")
    val discountAmount: Double = 0.0,
    @param:Json(name = "total_amount")
    val totalAmount: Double,
    @param:Json(name = "status")
    val status: String = "ISSUED",
)

// DTO: Add Bill Item Request (array body)
// POST /api/v1/bills/{id}/items
@JsonClass(generateAdapter = true)
data class AddBillItemRequest(
    @param:Json(name = "food_id")
    val foodId: Long,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "unit_price")
    val unitPrice: Double,
)
