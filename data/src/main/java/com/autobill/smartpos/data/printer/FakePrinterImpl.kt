package com.autobill.smartpos.data.printer

import android.util.Log
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PrintJob
import com.autobill.smartpos.domain.printer.BillPrinter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug-build printer — formats the receipt as human-readable text and logs it to Logcat.
 *
 * No Bluetooth / hardware interaction. Use the "Serial Bluetooth Terminal" Android app
 * to verify actual ESC/POS bytes against a real printer before switching to release build.
 *
 * Tag: "FakePrinter"  — filter in Logcat to see the full receipt.
 */
@Singleton
class FakePrinterImpl @Inject constructor() : BillPrinter {

    override suspend fun print(job: PrintJob): Result<Unit> {
        val receipt = buildString {
            val W = 48
            fun divH() = appendLine("=".repeat(W))
            fun divL() = appendLine("-".repeat(W))
            fun center(text: String) = appendLine(text.padStart((W + text.length) / 2).padEnd(W))

            divH()
            center(job.restaurantName)
            if (job.restaurantAddress.isNotBlank()) center(job.restaurantAddress)
            divH()

            val tableLabel = if (job.tableNumber != null) "Table: ${job.tableNumber}" else "Takeaway"
            appendLine("  ${job.orderType.padEnd(20)}$tableLabel")
            appendLine("  ${job.timestamp}")
            appendLine("  Order: #${job.orderNumber}")
            appendLine("  Cashier: ${job.cashierName}")
            divL()

            appendLine("  ${"ITEM".padEnd(ReceiptBuilder.NAME_COL_WIDTH)}${"QTY".padStart(4)}${"PRICE".padStart(8)}${"TOTAL".padStart(9)}")
            divL()

            for (item in job.items) {
                val nameChunks = item.name.chunked(ReceiptBuilder.NAME_COL_WIDTH)
                val q = item.quantity.toString().padStart(4)
                val p = "%.2f".format(item.unitPrice).padStart(8)
                val t = "%.2f".format(item.total).padStart(9)
                appendLine("  ${nameChunks[0].padEnd(ReceiptBuilder.NAME_COL_WIDTH)}$q$p$t")
                for (i in 1 until nameChunks.size) {
                    appendLine("  ${nameChunks[i]}")
                }
            }

            divL()
            appendLine("${"Subtotal:".padStart(30)}${"%.2f".format(job.subtotal).padStart(14)}")
            if (job.discountAmount > 0.0) {
                appendLine("${"Discount:".padStart(30)}${"-%.2f".format(job.discountAmount).padStart(14)}")
            }
            appendLine("${"CGST 2.5%:".padStart(30)}${"%.2f".format(job.cgstAmount).padStart(14)}")
            appendLine("${"SGST 2.5%:".padStart(30)}${"%.2f".format(job.sgstAmount).padStart(14)}")
            divH()
            appendLine("${"TOTAL:".padStart(30)}${"%.2f".format(job.totalAmount).padStart(14)}")
            divH()
            center("Thank you! Visit again!")
            divH()
        }

        Log.d(TAG, "\n$receipt")
        return Result.Success(Unit)
    }

    private companion object {
        const val TAG = "FakePrinter"
    }
}


