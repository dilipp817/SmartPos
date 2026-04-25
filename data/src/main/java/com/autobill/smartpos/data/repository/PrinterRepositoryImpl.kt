package com.autobill.smartpos.data.repository

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.autobill.smartpos.data.local.AppPrefsDataStore
import com.autobill.smartpos.domain.printer.PrinterDevice
import com.autobill.smartpos.domain.repository.PrinterRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements [PrinterRepository].
 *
 * Paired-device enumeration delegates to the OS Bluetooth stack via [BluetoothManager].
 * Selection persistence uses [AppPrefsDataStore].
 *
 * ## Permission note
 * [getPairedDevices] requires [android.Manifest.permission.BLUETOOTH_CONNECT] on API 31+.
 * On older APIs [android.Manifest.permission.BLUETOOTH] suffices (declared in manifest).
 * When permission is missing, the method returns an empty list and logs a warning —
 * it never crashes. The Settings screen is responsible for requesting the runtime permission
 * before calling this.
 */
@Singleton
class PrinterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPrefsDataStore: AppPrefsDataStore,
) : PrinterRepository {

    private val bluetoothAdapter by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    override fun getPairedDevices(): List<PrinterDevice> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return try {
            @Suppress("MissingPermission")   // caller must hold BLUETOOTH_CONNECT (API 31+)
            adapter.bondedDevices
                .filter { it.name != null }
                .map { PrinterDevice(name = it.name, macAddress = it.address) }
                .sortedBy { it.name }
        } catch (e: SecurityException) {
            Log.w(TAG, "BLUETOOTH_CONNECT permission missing — cannot list paired devices", e)
            emptyList()
        }
    }

    override fun observeSelectedPrinter(): Flow<PrinterDevice?> =
        appPrefsDataStore.observeSelectedPrinter()

    override suspend fun saveSelectedPrinter(device: PrinterDevice) =
        appPrefsDataStore.saveSelectedPrinter(device)

    override suspend fun clearSelectedPrinter() =
        appPrefsDataStore.clearSelectedPrinter()

    private companion object {
        const val TAG = "PrinterRepository"
    }
}




