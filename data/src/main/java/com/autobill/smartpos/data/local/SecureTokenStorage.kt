package com.autobill.smartpos.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encrypted storage for the JWT bearer token using Android Keystore.
 *
 * Security properties:
 *  - Master key stored in the Android Keystore (hardware-backed TEE/SE on supported devices).
 *  - Keys encrypted with AES256-SIV; values encrypted with AES-256-GCM before writing to disk.
 *  - File is inaccessible to other apps (Android private data sandbox).
 *  - Excluded from ADB backup and Google Cloud Backup (backup_rules.xml +
 *    data_extraction_rules.xml + android:allowBackup="false").
 *
 * Why separate from DataStore?
 *  There is no official EncryptedDataStore API. Non-sensitive session fields
 *  (role, restaurantId, username) remain in plain DataStore. Only the JWT — the credential
 *  that grants API access — lives here.
 *
 * Keystore-corruption recovery:
 *  On certain events (factory reset, ROM flash, device-admin removal) the Keystore can
 *  become unusable. Rather than crashing the app, we detect the failure, wipe the corrupted
 *  prefs file, and force re-authentication. This is the safe, security-correct behaviour:
 *  the session is lost but the app stays alive.
 */
@Singleton
class SecureTokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "SecureTokenStorage"
        private const val FILE_NAME = "smartpos_secure_session"
        private const val KEY_TOKEN  = "jwt_token"
    }

    private var prefs: SharedPreferences? = null

    init {
        prefs = openOrRecreate()
    }

    /** Save (or overwrite) the JWT token. Encrypted with AES-256-GCM before write. */
    fun saveToken(token: String) {
        getPrefs().edit { putString(KEY_TOKEN, token) }
    }

    /** Return the decrypted JWT token, or null if no session exists. */
    fun getToken(): String? = try {
        getPrefs().getString(KEY_TOKEN, null)
    } catch (e: Exception) {
        Log.e(TAG, "getToken failed — Keystore may be corrupted, wiping session", e)
        wipeAndReset()
        null
    }

    /** Wipe the stored token on logout. */
    fun clearToken() {
        try {
            getPrefs().edit { remove(KEY_TOKEN) }
        } catch (e: Exception) {
            Log.e(TAG, "clearToken failed — wiping entire prefs file", e)
            wipeAndReset()
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun getPrefs(): SharedPreferences = prefs ?: openOrRecreate().also { prefs = it }

    /**
     * Try to open EncryptedSharedPreferences. If the Keystore key is corrupted or
     * the prefs file is unusable, delete the file and recreate a fresh set of prefs.
     * The caller loses the stored token but the app remains functional.
     */
    private fun openOrRecreate(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Keystore key corrupted — wiping prefs and recreating", e)
            deletePrefsFile()
            createEncryptedPrefs()          // second attempt after wipe
        } catch (e: IOException) {
            Log.e(TAG, "Prefs file unreadable — wiping and recreating", e)
            deletePrefsFile()
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun deletePrefsFile() {
        try {
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            File(prefsDir, "$FILE_NAME.xml").delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete corrupted prefs file", e)
        }
    }

    private fun wipeAndReset() {
        deletePrefsFile()
        prefs = try { createEncryptedPrefs() } catch (_: Exception) { null }
    }
}
