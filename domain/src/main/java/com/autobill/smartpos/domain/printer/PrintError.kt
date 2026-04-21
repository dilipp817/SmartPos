package com.autobill.smartpos.domain.printer

/**
 * Maps printer exceptions to structured [PrintError] so every ViewModel and UI
 * can show the right message / action without duplicating `when (e)` logic.
 */
sealed class PrintError {
    /** No printer has been saved in Settings yet. UI should offer a "Set up" action. */
    data object NoPrinterConfigured : PrintError()

    /** Bluetooth is disabled on the device. UI should tell the user to enable it. */
    data object BluetoothDisabled : PrintError()

    /** The socket connection attempt failed (printer off, out of range, etc.). */
    data object ConnectionFailed : PrintError()

    /** Any other unexpected error. */
    data class Unknown(val message: String) : PrintError()
}

/**
 * Classifies a [Throwable] from [BillPrinter.print] into a [PrintError].
 *
 * Uses `is` type checks against [PrinterException] subtypes — safe under
 * ProGuard / R8 minification (unlike `javaClass.simpleName` string matching).
 */
fun Throwable.toPrintError(): PrintError = when (this) {
    is NoPrinterSelectedException -> PrintError.NoPrinterConfigured
    is BluetoothDisabledException  -> PrintError.BluetoothDisabled
    is PrinterConnectionException  -> PrintError.ConnectionFailed
    else                           -> PrintError.Unknown(message ?: "Unknown print error")
}

