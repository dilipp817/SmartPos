package com.autobill.smartpos.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted storage for the JWT bearer token using Android Keystore.
 *
 * Security properties:
 *  - Master key stored in the Android Keystore (hardware-backed TEE/SE on supported devices).
 *  - Values encrypted with AES-256-GCM before writing to disk.
 *  - File is inaccessible to other apps (Android private data sandbox).
 *  - Excluded from ADB backup and Google Cloud Backup (backup_rules.xml +
 *    data_extraction_rules.xml).
 *
 * Why separate from DataStore?
 *  There is no official EncryptedDataStore API. Non-sensitive session fields
 *  (role, restaurantId, username) remain in DataStore. Only the JWT — the credential
 *  that grants API access — lives here.
 */
@Singleton
class SecureTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val FILE_NAME = "smartpos_secure_session"
        private const val KEY_TOKEN  = "jwt_token"
    }

    private val prefs: SharedPreferences by lazy { createEncryptedPrefs() }

    /** Save (or overwrite) the JWT token. Encrypted with AES-256-GCM before write. */
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    /** Return the decrypted JWT token, or null if no session exists. */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Wipe the stored token on logout. */
    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        // AES256_GCM_SPEC creates / retrieves an AES-256 key in the Android Keystore.
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            FILE_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }
}
