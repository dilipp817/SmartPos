package com.autobill.smartpos.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DTO: Bill Item
@JsonClass(generateAdapter = true)
data class BillItemDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "menu_item_name")
    val menuItemName: String,
    @param:Json(name = "quantity")
    val quantity: Int,
    @param:Json(name = "unit_price")
    val unitPrice: Double,
    @param:Json(name = "variant_name")
    val variantName: String?,
    @param:Json(name = "subtotal")
    val subtotal: Double,
)

// DTO: Discount
@JsonClass(generateAdapter = true)
data class DiscountDto(
    @param:Json(name = "type")
    val type: String, // "percentage", "fixed"
    @param:Json(name = "value")
    val value: Double,
    @param:Json(name = "amount")
    val amount: Double,
)

// DTO: Tax Detail
@JsonClass(generateAdapter = true)
data class TaxDetailDto(
    @param:Json(name = "name")
    val name: String,
    @param:Json(name = "rate")
    val rate: Double,
    @param:Json(name = "amount")
    val amount: Double,
)

// DTO: Bill
// Maps to API response from GET /restaurants/{restaurantId}/bills/{billId}
@JsonClass(generateAdapter = true)
data class BillDto(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "bill_number")
    val billNumber: String,
    @param:Json(name = "order_id")
    val orderId: Int,
    @param:Json(name = "table_id")
    val tableId: Int,
    @param:Json(name = "table_number")
    val tableNumber: String,
    @param:Json(name = "restaurant_id")
    val restaurantId: Int,
    @param:Json(name = "items")
    val items: List<BillItemDto>,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "discount")
    val discount: DiscountDto,
    @param:Json(name = "tax_details")
    val taxDetails: List<TaxDetailDto>,
    @param:Json(name = "total_tax")
    val totalTax: Double,
    @param:Json(name = "total_amount")
    val totalAmount: Double,
    @param:Json(name = "status")
    val status: String, // "unpaid", "partial", "paid"
    @param:Json(name = "created_at")
    val createdAt: String,
    @param:Json(name = "printed_at")
    val printedAt: String?,
)

// DTO: Apply Discount Request
@JsonClass(generateAdapter = true)
data class ApplyDiscountRequest(
    @param:Json(name = "discount_type")
    val discountType: String, // "percentage", "fixed"
    @param:Json(name = "discount_value")
    val discountValue: Double,
    @param:Json(name = "reason")
    val reason: String,
)

// DTO: Apply Discount Response
@JsonClass(generateAdapter = true)
data class ApplyDiscountResponse(
    @param:Json(name = "id")
    val id: Int,
    @param:Json(name = "subtotal")
    val subtotal: Double,
    @param:Json(name = "discount")
    val discount: DiscountDto,
    @param:Json(name = "subtotal_after_discount")
    val subtotalAfterDiscount: Double,
    @param:Json(name = "total_tax")
    val totalTax: Double,
    @param:Json(name = "total_amount")
    val totalAmount: Double,
)

// DTO: Bill Print Response
@JsonClass(generateAdapter = true)
data class BillPrintResponse(
    @param:Json(name = "bill_html")
    val billHtml: String,
    @param:Json(name = "bill_text")
    val billText: String,
)

