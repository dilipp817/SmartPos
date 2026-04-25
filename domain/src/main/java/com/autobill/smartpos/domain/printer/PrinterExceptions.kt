package com.autobill.smartpos.domain.printer

/**
 * Typed exceptions for printer operations.
 *
 * Defined in the **domain** layer so:
 *  - ViewModels can `is`-check without importing data-layer classes.
 *  - `toPrintError()` uses safe `is` type checks, immune to ProGuard/R8 minification.
 *
 * All are subtypes of [PrinterException] for easy `catch` grouping.
 */
sealed class PrinterException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

/** Thrown when the user tries to print but no printer has been selected in Settings. */
class NoPrinterSelectedException : PrinterException(
    "No printer selected. Please select a printer in Settings → Printer.",
)

/** Thrown when the user tries to print but Bluetooth is turned off on the device. */
class BluetoothDisabledException : PrinterException(
    "Bluetooth is turned off. Please enable Bluetooth and try again.",
)

/**
 * Thrown when the Bluetooth RFCOMM socket connection attempt fails.
 * Usually means the printer is off, out of range, or already connected to another device.
 */
class PrinterConnectionException(cause: Throwable) : PrinterException(
    "Could not connect to printer. Please check it is switched on and nearby.",
    cause,
)

