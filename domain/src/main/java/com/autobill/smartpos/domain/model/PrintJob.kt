package com.autobill.smartpos.domain.model

/**
 * Immutable value object that carries all the data needed to render a receipt.
 *
 * Assembled by [PrintJobFactory] from an [Order] (or [Bill] + [Order]), enriched
 * with restaurant info and cashier name from the local DataStore caches.
 * Passed directly to [BillPrinter.print].
 *
 * Tax fields:
 *  - For Order Detail / Home Screen receipts → estimated via [TaxConstants] (2.5% each).
 *  - For Billing Screen receipts             → taken from server-computed [Bill] values.
 */
data class PrintJob(
    val restaurantName: String,
    val restaurantAddress: String,
    /** Server-assigned order number, e.g. "ORD-1042". */
    val orderNumber: String,
    /** Human-readable label: "Dine-In" / "Takeaway" / "Delivery". */
    val orderType: String,
    /** null for Takeaway / no-table orders. */
    val tableNumber: String?,
    val cashierName: String,
    /** Pre-formatted IST timestamp: "21/04/2026 10:35 AM". */
    val timestamp: String,
    val items: List<PrintLineItem>,
    val subtotal: Double,
    val discountAmount: Double,
    /** CGST amount (2.5% of taxable or server value). */
    val cgstAmount: Double,
    /** SGST amount (2.5% of taxable or server value). */
    val sgstAmount: Double,
    val totalAmount: Double,
)

/** One line on the receipt — mirrors [OrderItem] / [BillItem] but printer-agnostic. */
data class PrintLineItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double,
)

