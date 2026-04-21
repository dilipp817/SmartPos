package com.autobill.smartpos.domain.printer

/**
 * A Bluetooth printer device discovered from the OS-level bonded-devices list.
 *
 * [name]       — display name the cashier recognises (e.g. "Xprinter XP-58").
 * [macAddress] — BT MAC used internally to open the socket; never shown in UI.
 */
data class PrinterDevice(
    val name: String,
    val macAddress: String,
)

