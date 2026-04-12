package com.autobill.smartpos.domain.model

// Domain Model: Bill Item
// Item 10: added billId/foodId, renamed menuItemName→foodName, renamed subtotal→itemTotal,
//          removed variantName, added createdAt
data class BillItem(
    val id: Long,
    val billId: Long,
    val foodId: Long,
    val foodName: String,
    val quantity: Int,
    val unitPrice: Double,
    val itemTotal: Double,
    val createdAt: String,
)

// Domain Model: Bill
// Item 11: flattened structure — removed Discount/TaxDetail objects;
//          added restaurantName/taxAmount/cgstAmount/sgstAmount/discountAmount;
//          removed tableNumber + printedAt (not in API); renamed items→billItems
data class Bill(
    val id: Long,
    val billNumber: String,
    val orderId: Long,
    val restaurantId: Long,
    val restaurantName: String?,
    val subtotal: Double,
    val taxAmount: Double,
    val cgstAmount: Double,       // 9% CGST
    val sgstAmount: Double,       // 9% SGST
    val discountAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,         // cumulative amount paid so far; server-managed
    val remainingAmount: Double,    // totalAmount - paidAmount; server-managed — never compute locally
    val status: BillStatus,
    val billItems: List<BillItem>,
    val createdAt: String,
    val updatedAt: String,
)

// Item 12 (complete): ISSUED/PARTIAL/PAID/CANCELLED — all UPPERCASE
// Backend auto-sets PARTIAL when a payment covers < bill.totalAmount,
// and transitions to PAID once cumulative payments ≥ bill.totalAmount.
enum class BillStatus(val value: String) {
    ISSUED("ISSUED"),       // bill generated, no payment yet
    PARTIAL("PARTIAL"),     // at least one payment made, but total not yet reached
    PAID("PAID"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromValue(value: String): BillStatus {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: ISSUED
        }
    }
}

// Discount and TaxDetail domain classes REMOVED (Item 11) — no longer needed.
// Discount info is represented as discountAmount: Double on Bill.
// Tax info is represented as taxAmount/cgstAmount/sgstAmount on Bill.
