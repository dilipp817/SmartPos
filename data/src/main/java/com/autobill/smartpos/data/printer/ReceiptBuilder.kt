package com.autobill.smartpos.data.printer

import com.autobill.smartpos.domain.model.PrintJob
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Converts a [PrintJob] into a raw ESC/POS byte sequence ready to write
 * to a thermal printer's output stream.
 *
 * Layout (80 mm / 48-char width):
 * ```
 * ================================================
 *           RESTAURANT NAME  (centered, bold)
 *   Address line             (centered)
 * ================================================
 *   Dine-In  |  Table: 5
 *   Date: 21/04/2026  10:35 AM
 *   Order: #ORD-001   Cashier: John
 * ------------------------------------------------
 *   ITEM                  QTY   PRICE    TOTAL
 * ------------------------------------------------
 *   Paneer Butter Masala    2  160.00   320.00
 *   Garlic Naan             3   25.00    75.00
 * ------------------------------------------------
 *                     Subtotal:        395.00
 *                     Discount:          0.00
 *                     CGST 2.5%:         9.88
 *                     SGST 2.5%:         9.88
 * ================================================
 *                        TOTAL:        414.76
 * ================================================
 *           Thank you! Visit again!
 * ================================================
 * [3 blank lines + partial cut]
 * ```
 */
@Singleton
class ReceiptBuilder @Inject constructor() {


    fun build(job: PrintJob): ByteArray {
        val out = ByteArrayOutputStream()

        fun write(bytes: ByteArray) = out.write(bytes)
        fun writeln(bytes: ByteArray) { write(bytes); write(EscPosCommands.LF) }
        fun writeln(text: String) = writeln(text.toByteArray(Charsets.UTF_8))
        fun blankLine() = write(EscPosCommands.LF)

        // ── Init ──────────────────────────────────────────────────────────────
        write(EscPosCommands.INIT)

        // ── Header ───────────────────────────────────────────────────────────
        writeln(EscPosCommands.DIVIDER_HEAVY)

        write(EscPosCommands.ALIGN_CENTER)
        write(EscPosCommands.BOLD_ON)
        write(EscPosCommands.SIZE_DOUBLE)
        writeln(job.restaurantName.take(24))          // double-width = 24 chars max
        write(EscPosCommands.SIZE_NORMAL)
        write(EscPosCommands.BOLD_OFF)

        if (job.restaurantAddress.isNotBlank()) {
            job.restaurantAddress.chunked(WIDTH).forEach { writeln(it) }
        }

        write(EscPosCommands.ALIGN_LEFT)
        writeln(EscPosCommands.DIVIDER_HEAVY)

        // ── Order meta ────────────────────────────────────────────────────────
        val tableLabel = if (job.tableNumber != null) "Table: ${job.tableNumber}" else "Takeaway"
        writeln("  ${job.orderType.padEnd(20)}$tableLabel")
        writeln("  ${job.timestamp}")
        writeln("  Order: #${job.orderNumber}")
        writeln("  Cashier: ${job.cashierName}")
        writeln(EscPosCommands.DIVIDER_LIGHT)

        // ── Column header ─────────────────────────────────────────────────────
        write(EscPosCommands.BOLD_ON)
        writeln(colHeader())
        write(EscPosCommands.BOLD_OFF)
        writeln(EscPosCommands.DIVIDER_LIGHT)

        // ── Items ─────────────────────────────────────────────────────────────
        for (item in job.items) {
            itemLines(item.name, item.quantity, item.unitPrice, item.total)
                .forEach { writeln(it) }
        }

        // ── Totals ────────────────────────────────────────────────────────────
        writeln(EscPosCommands.DIVIDER_LIGHT)
        writeln(amountLine("Subtotal:", job.subtotal))
        if (job.discountAmount > 0.0) {
            writeln(amountLine("Discount:", -job.discountAmount))
        }
        writeln(amountLine("CGST 2.5%:", job.cgstAmount))
        writeln(amountLine("SGST 2.5%:", job.sgstAmount))

        writeln(EscPosCommands.DIVIDER_HEAVY)

        write(EscPosCommands.BOLD_ON)
        writeln(amountLine("TOTAL:", job.totalAmount))
        write(EscPosCommands.BOLD_OFF)

        writeln(EscPosCommands.DIVIDER_HEAVY)

        // ── Footer ────────────────────────────────────────────────────────────
        write(EscPosCommands.ALIGN_CENTER)
        blankLine()
        writeln("Thank you! Visit again!")
        blankLine()
        write(EscPosCommands.ALIGN_LEFT)
        writeln(EscPosCommands.DIVIDER_HEAVY)

        // ── Feed + cut ────────────────────────────────────────────────────────
        write(EscPosCommands.feedLines(4))
        write(EscPosCommands.PARTIAL_CUT)

        return out.toByteArray()
    }

    // ── Formatting helpers ────────────────────────────────────────────────────

    /** Column headers row. */
    private fun colHeader(): String {
        val item  = "ITEM".padEnd(NAME_COL_WIDTH)
        val qty   = "QTY".padStart(4)
        val price = "PRICE".padStart(8)
        val total = "TOTAL".padStart(9)
        return "  $item$qty$price$total"
    }

    /**
     * Formats one order item, wrapping the name if it exceeds [NAME_COL_WIDTH] characters.
     *
     * First line:  "  <name-chunk-1>  QTY  PRICE  TOTAL"
     * Overflow:    "  <name-chunk-2>"   (no numbers on continuation lines)
     *
     * Example — "Paneer Butter Masala Special" (28 chars):
     * ```
     *   Paneer Butter Masala    2  160.00   320.00
     *   Special
     * ```
     */
    private fun itemLines(name: String, qty: Int, unitPrice: Double, total: Double): List<String> {
        val nameChunks = name.chunked(NAME_COL_WIDTH)
        val q = qty.toString().padStart(4)
        val p = "%.2f".format(unitPrice).padStart(8)
        val t = "%.2f".format(total).padStart(9)
        return buildList {
            // First line: name + numbers
            add("  ${nameChunks[0].padEnd(NAME_COL_WIDTH)}$q$p$t")
            // Continuation lines: name only, no numbers
            for (i in 1 until nameChunks.size) {
                add("  ${nameChunks[i]}")
            }
        }
    }

    /**
     * "                 CGST 2.5%:        9.88"
     * Label is right-aligned in the left 30 chars; amount in the right 14.
     */
    private fun amountLine(label: String, amount: Double): String {
        val l = label.padStart(LABEL_COL_WIDTH)
        val a = "%.2f".format(amount).padStart(AMOUNT_COL_WIDTH)
        return "$l$a"
    }

    companion object {
        /** Total printable width for 80 mm thermal paper (48 chars). */
        const val WIDTH = 48

        /** Characters available for item name column. */
        const val NAME_COL_WIDTH = 22

        /** Characters for the label (left) column in amount lines. */
        private const val LABEL_COL_WIDTH = 30

        /** Characters for the amount (right) column in amount lines. */
        private const val AMOUNT_COL_WIDTH = 14
    }
}



