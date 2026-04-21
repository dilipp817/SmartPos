package com.autobill.smartpos.domain.repository

import com.autobill.smartpos.domain.printer.PrinterDevice
import kotlinx.coroutines.flow.Flow

/**
 * Repository for printer selection state.
 *
 * Paired-device enumeration is delegated to the OS Bluetooth stack.
 * Selection persistence is handled by the underlying implementation.
 */
interface PrinterRepository {
    /**
     * Returns the list of already-paired (bonded) Bluetooth devices.
     * Requires Bluetooth connect permission on API 31+.
     * Returns an empty list when permission is denied or Bluetooth is off.
     */
    fun getPairedDevices(): List<PrinterDevice>

    /** Observe the currently selected printer. Emits null if none selected. */
    fun observeSelectedPrinter(): Flow<PrinterDevice?>

    /** Persist the user's printer choice (name + MAC). */
    suspend fun saveSelectedPrinter(device: PrinterDevice)

    /** Remove the stored printer selection (e.g. device was unpaired). */
    suspend fun clearSelectedPrinter()
}

