package com.autobill.smartpos.domain.model

// Domain Model: Bill Item
data class BillItem(
    val id: Int,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: Double,
    val variantName: String?,
    val subtotal: Double,
)

// Domain Model: Discount
data class Discount(
    val type: DiscountType,
    val value: Double, // Percentage or fixed amount
    val amount: Double, // Calculated discount amount
)

enum class DiscountType(val value: String) {
    PERCENTAGE("percentage"),
    FIXED("fixed");

    companion object {
        fun fromValue(value: String): DiscountType {
            return entries.find { it.value == value } ?: PERCENTAGE
        }
    }
}

// Domain Model: Tax Detail
data class TaxDetail(
    val name: String,
    val rate: Double,
    val amount: Double,
)

// Domain Model: Bill
// Independent of database or API structure
data class Bill(
    val id: Int,
    val billNumber: String,
    val orderId: Int,
    val tableNumber: String,
    val restaurantId: Int,
    val items: List<BillItem>,
    val subtotal: Double,
    val discount: Discount,
    val taxDetails: List<TaxDetail>,
    val totalTax: Double,
    val totalAmount: Double,
    val status: BillStatus,
    val createdAt: String,
    val printedAt: String?,
)

enum class BillStatus(val value: String) {
    UNPAID("unpaid"),
    PARTIAL("partial"),
    PAID("paid");

    companion object {
        fun fromValue(value: String): BillStatus {
            return entries.find { it.value == value } ?: UNPAID
        }
    }
}

