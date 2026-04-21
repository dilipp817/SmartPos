package com.autobill.smartpos.domain.usecase

import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PrintJob
import com.autobill.smartpos.domain.printer.BillPrinter
import com.autobill.smartpos.domain.printer.PrinterDevice
import com.autobill.smartpos.domain.repository.PrinterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Sends a [PrintJob] to the configured [BillPrinter].
 *
 * Single use case shared across all three print-trigger points:
 *  1. Home Screen — after TAKEAWAY order placed
 *  2. Order Detail — print button
 *  3. Billing Screen — print button
 *
 * Callers are responsible for assembling the [PrintJob] via [PrintJobFactory].
 */
class PrintBillUseCase @Inject constructor(
    private val printer: BillPrinter,
) {
    suspend operator fun invoke(job: PrintJob): Result<Unit> = printer.print(job)
}

/** Returns the list of OS-bonded Bluetooth devices available for selection. */
class GetPairedBluetoothDevicesUseCase @Inject constructor(
    private val repository: PrinterRepository,
) {
    operator fun invoke(): List<PrinterDevice> = repository.getPairedDevices()
}

/** Observes the currently saved printer selection (name + MAC). Emits null when none saved. */
class ObserveSelectedPrinterUseCase @Inject constructor(
    private val repository: PrinterRepository,
) {
    operator fun invoke(): Flow<PrinterDevice?> = repository.observeSelectedPrinter()
}

/** Persists the cashier's printer choice to [AppPrefsDataStore]. */
class SaveSelectedPrinterUseCase @Inject constructor(
    private val repository: PrinterRepository,
) {
    suspend operator fun invoke(device: PrinterDevice) = repository.saveSelectedPrinter(device)
}

/** Clears the stored printer selection (e.g. after the device is unpaired). */
class ClearSelectedPrinterUseCase @Inject constructor(
    private val repository: PrinterRepository,
) {
    suspend operator fun invoke() = repository.clearSelectedPrinter()
}

