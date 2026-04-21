package com.autobill.smartpos.domain.printer

import com.autobill.smartpos.domain.common.TaxConstants
import com.autobill.smartpos.domain.model.Bill
import com.autobill.smartpos.domain.model.Order
import com.autobill.smartpos.domain.model.PrintJob
import com.autobill.smartpos.domain.model.PrintLineItem
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveSessionUseCase
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain service: assembles a [PrintJob] from available data.
 *
 * Centralises receipt-building logic so [BillingViewModel], [OrderDetailViewModel]
 * and [PlaceOrderViewModel] do not duplicate it (DRY / SRP).
 *
 * ## Tax handling
 *  - [fromOrder]          — estimated CGST + SGST via [TaxConstants] (no bill yet).
 *  - [fromBillAndOrder]   — server-computed values from the [Bill] object.
 *
 * ## Data sources
 *  | Field           | Source                     |
 *  |-----------------|----------------------------|
 *  | Restaurant name | [ObserveRestaurantUseCase] |
 *  | Address         | [ObserveRestaurantUseCase] |
 *  | Cashier name    | [ObserveSessionUseCase]    |
   Timestamp        Device clock (IST / Asia/Kolkata, always)
 *  | Items / totals  | Passed-in [Order] / [Bill] |
 */
@Singleton
class PrintJobFactory @Inject constructor(
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val observeSessionUseCase: ObserveSessionUseCase,
) {

    /**
     * Build a receipt from an [Order] (Home Screen / Order Detail).
     * Tax is estimated locally — the bill has not been generated yet.
     */
    suspend fun fromOrder(order: Order): PrintJob {
        val (restaurantName, restaurantAddress, cashierName) = loadPrintContext()
        val subtotal = order.subtotal
        val cgst     = subtotal * TaxConstants.RECEIPT_CGST_RATE
        val sgst     = subtotal * TaxConstants.RECEIPT_SGST_RATE
        return PrintJob(
            restaurantName    = restaurantName,
            restaurantAddress = restaurantAddress,
            orderNumber       = order.orderNumber,
            orderType         = order.orderType.displayLabel,
            tableNumber       = order.tableNumber,
            cashierName       = cashierName,
            timestamp         = currentTimestamp(),
            items             = order.items.map {
                PrintLineItem(it.foodName, it.quantity, it.unitPrice, it.subtotal)
            },
            subtotal          = subtotal,
            discountAmount    = 0.0,
            cgstAmount        = cgst,
            sgstAmount        = sgst,
            totalAmount       = subtotal + cgst + sgst,
        )
    }

    /**
     * Build a receipt from a [Bill] + [Order] (Billing Screen).
     * Uses server-computed tax values — never estimates.
     */
    suspend fun fromBillAndOrder(bill: Bill, order: Order): PrintJob {
        val (restaurantName, restaurantAddress, cashierName) = loadPrintContext()
        return PrintJob(
            restaurantName    = restaurantName,
            restaurantAddress = restaurantAddress,
            orderNumber       = order.orderNumber,
            orderType         = order.orderType.displayLabel,
            tableNumber       = order.tableNumber,
            cashierName       = cashierName,
            timestamp         = currentTimestamp(),
            items             = bill.billItems.map {
                PrintLineItem(it.foodName, it.quantity, it.unitPrice, it.itemTotal)
            },
            subtotal          = bill.subtotal,
            discountAmount    = bill.discountAmount,
            cgstAmount        = bill.cgstAmount,
            sgstAmount        = bill.sgstAmount,
            totalAmount       = bill.totalAmount,
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Loads restaurant info and session user in a single suspend call.
     * Both flows are collected once — avoids two separate [first] subscriptions per build method.
     */
    private suspend fun loadPrintContext(): PrintContext {
        val restaurant = observeRestaurantUseCase().first()
        val user       = observeSessionUseCase().first()
        return PrintContext(
            restaurantName    = restaurant?.outletName ?: "",
            restaurantAddress = restaurant?.address?.formatted ?: "",
            cashierName       = user?.username ?: "",
        )
    }

    private fun currentTimestamp(): String =
        SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            .also { it.timeZone = TimeZone.getTimeZone("Asia/Kolkata") }
            .format(Date())

    /** Aggregates the data sources that are the same for every receipt variant. */
    private data class PrintContext(
        val restaurantName: String,
        val restaurantAddress: String,
        val cashierName: String,
    )
}

