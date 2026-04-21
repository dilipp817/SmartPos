package com.autobill.smartpos.util

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Utility helpers for Bluetooth permission and state checks.
 *
 * All checks are pure (no side effects). The Settings screen / Routes are
 * responsible for requesting missing permissions via the Activity Result API.
 */
object BluetoothPermissionHelper {

    /**
     * Returns `true` when the app holds the [Manifest.permission.BLUETOOTH_CONNECT]
     * permission (API 31+) **or** is running on an older device where the
     * install-time [Manifest.permission.BLUETOOTH] permission is sufficient.
     */
    fun hasBluetoothConnectPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // On API < 31 the manifest-declared BLUETOOTH permission is enough.
            true
        }
    }

    /**
     * Returns `true` when the device has a Bluetooth adapter **and** it is
     * currently enabled. Does NOT require any special permission.
     */
    fun isBluetoothEnabled(context: Context): Boolean {
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
            ?.adapter ?: return false
        return adapter.isEnabled
    }
}

