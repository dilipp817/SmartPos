package com.autobill.smartpos.domain.printer

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PrintJob

/**
 * Printer port — abstracts the physical transport layer.
 *
 * Implementations:
 *  - Debug builds → [FakePrinterImpl]  — logs formatted receipt to Logcat.
 *  - Release builds → [BluetoothEscPosPrinterImpl] — raw ESC/POS over BT RFCOMM socket.
 *
 * Contract:
 *  - Must be called from a coroutine (may do I/O on the calling dispatcher).
 *  - Returns [Result.Success] on successful transmission.
 *  - Returns [Result.Failure] with a descriptive exception on any error
 *    (no printer selected, BT off, socket error, permission denied, etc.).
 *  - Never throws — callers rely on the Result type for error handling.
 */
interface BillPrinter {
    suspend fun print(job: PrintJob): Result<Unit>
}

