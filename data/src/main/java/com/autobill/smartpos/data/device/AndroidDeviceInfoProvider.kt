package com.autobill.smartpos.data.device

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.autobill.smartpos.core.device.DeviceInfoProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [DeviceInfoProvider].
 *
 * Uses ANDROID_ID as the device identifier:
 *  - Unique per (device × app signing key)
 *  - Survives reboots; resets only on factory reset — acceptable for a POS tablet
 *  - No network call required; available immediately
 *
 * deviceType is hardcoded to "tablet" because this app exclusively targets
 * tablet-mounted billing counters.
 *
 * Context is injected HERE (infrastructure layer), not in ViewModels,
 * keeping the presentation layer free from Android framework dependencies
 * and fully unit-testable with plain JUnit + Mockito.
 *
 * The @SuppressLint lives here — the only place that knows or cares about
 * Android hardware IDs.  No caller ever sees it.
 */
@Singleton
class AndroidDeviceInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : DeviceInfoProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    override fun getDeviceType(): String = "tablet"
}

