package com.autobill.smartpos.data.printer

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.PrintJob
import com.autobill.smartpos.domain.printer.BillPrinter
import com.autobill.smartpos.domain.printer.BluetoothDisabledException
import com.autobill.smartpos.domain.printer.NoPrinterSelectedException
import com.autobill.smartpos.domain.printer.PrinterConnectionException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Release-build printer — sends raw ESC/POS bytes over a Bluetooth RFCOMM socket.
 *
 * ## Requirements
 *  - Device must be paired via Android OS Settings before use.
 *  - Printer MAC stored via [AppPrefsDataStore] (selected from Settings screen).
 *  - [android.Manifest.permission.BLUETOOTH_CONNECT] granted (API 31+).
 *
 * ## Error handling — all errors returned as [Result.Failure], never thrown
 *  - No printer selected   → [NoPrinterSelectedException]
 *  - Bluetooth off         → [BluetoothDisabledException]
 *  - Socket connect fails  → [PrinterConnectionException]
 *  - Any other I/O error   → the raw [IOException]
 */
@Singleton
class BluetoothEscPosPrinterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefsDataStore: AppPrefsDataStore,
    private val receiptBuilder: ReceiptBuilder,
) : BillPrinter {

    override suspend fun print(job: PrintJob): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val mac = appPrefsDataStore.getSelectedPrinterMac()
                ?: return@withContext Result.Failure(NoPrinterSelectedException())

            val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
                ?.adapter
                ?: return@withContext Result.Failure(IOException("Bluetooth not available on this device"))

            if (!adapter.isEnabled) {
                return@withContext Result.Failure(BluetoothDisabledException())
            }

            @Suppress("MissingPermission")   // caller (SettingsRoute) has requested BLUETOOTH_CONNECT
            val device = adapter.getRemoteDevice(mac)

            @Suppress("MissingPermission")
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            try {
                try {
                    @Suppress("MissingPermission")
                    socket.connect()
                } catch (e: IOException) {
                    throw PrinterConnectionException(e)
                }
                socket.outputStream.write(receiptBuilder.build(job))
                socket.outputStream.flush()
                Result.Success(Unit)
            } finally {
                runCatching { socket.close() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Print failed", e)
            Result.Failure(e)
        }
    }

    private companion object {
        const val TAG = "BluetoothEscPosPrinter"
        /** Standard Serial Port Profile UUID — works with all generic ESC/POS printers. */
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}





