package com.autobill.smartpos.data.repository

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.printer.PrinterDevice
import com.autobill.smartpos.domain.repository.PrinterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug-only [PrinterRepository] implementation.
 *
 * When the real Bluetooth adapter returns no bonded devices (e.g. on the Android
 * emulator), this implementation injects fake "virtual printer" entries so the
 * printer-selection UI can be fully exercised without physical hardware.
 *
 * All other operations (DataStore persistence, observeSelectedPrinter, etc.)
 * delegate directly to [AppPrefsDataStore] — same as the release implementation.
 */
@Singleton
class DebugPrinterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefsDataStore: AppPrefsDataStore,
) : PrinterRepository {

    private val bluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    override fun getPairedDevices(): List<PrinterDevice> {
        val real = try {
            val adapter = bluetoothAdapter ?: return MOCK_DEVICES
            if (!adapter.isEnabled) return MOCK_DEVICES
            @Suppress("MissingPermission")
            adapter.bondedDevices
                .filter { it.name != null }
                .map { PrinterDevice(name = it.name, macAddress = it.address) }
                .sortedBy { it.name }
        } catch (e: SecurityException) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing — returning mock devices", e)
            emptyList()
        }
        // Fall back to mock devices when no real devices are bonded (emulator scenario)
        return real.ifEmpty { MOCK_DEVICES }
    }

    override fun observeSelectedPrinter(): Flow<PrinterDevice?> =
        appPrefsDataStore.observeSelectedPrinter()

    override suspend fun saveSelectedPrinter(device: PrinterDevice) =
        appPrefsDataStore.saveSelectedPrinter(device)

    override suspend fun clearSelectedPrinter() =
        appPrefsDataStore.clearSelectedPrinter()

    private companion object {
        const val TAG = "DebugPrinterRepository"
        val MOCK_DEVICES = listOf(
            PrinterDevice(name = "[Debug] Xprinter XP-58 (Virtual)",  macAddress = "00:11:22:33:44:55"),
            PrinterDevice(name = "[Debug] GOOJPRT PT-210 (Virtual)",  macAddress = "00:11:22:33:44:66"),
            PrinterDevice(name = "[Debug] TVS RP80 (Virtual)",         macAddress = "00:11:22:33:44:77"),
        )
    }
}


