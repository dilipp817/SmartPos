package com.autobill.smartpos.core.device

/**
 * Platform-agnostic contract for reading physical device identity.
 *
 * Lives in :core so it is accessible to every module without pulling in
 * Android framework classes.  The concrete Android implementation lives in
 * :data, which is the only module allowed to touch platform APIs.
 *
 * SOLID alignment:
 *  - SRP  : sole responsibility is device identification
 *  - DIP  : ViewModels / use-cases depend on this interface, never on Context
 *  - OCP  : swap implementations (Android, Firebase IID, test fake) without
 *           touching any caller
 *
 * Testability:
 *  Unit tests pass a simple fake:
 *      val fakeDevice = object : DeviceInfoProvider {
 *          override fun getDeviceId()   = "test-device-id"
 *          override fun getDeviceType() = "tablet"
 *      }
 *  No Robolectric or AndroidJUnit4 runner required.
 */
interface DeviceInfoProvider {

    /**
     * Returns a stable, unique identifier for this physical device.
     * Implementations decide the source (ANDROID_ID, Firebase IID, etc.).
     */
    fun getDeviceId(): String

    /**
     * Returns the form-factor of this device.
     * e.g. "tablet", "mobile", "desktop"
     */
    fun getDeviceType(): String
}

