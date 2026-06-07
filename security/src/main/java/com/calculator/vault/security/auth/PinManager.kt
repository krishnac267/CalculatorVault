package com.calculator.vault.security.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages PIN and credential storage using Android Keystore + EncryptedSharedPreferences.
 * PINs are never stored in plain text — only salted PBKDF2 hashes.
 */
@Singleton
class PinManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    init {
        ensureKeystoreKey()
    }

    fun isSetupComplete(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETE, false)

    fun storePin(pin: String, isFakePin: Boolean = false) {
        val salt = generateSalt()
        val hash = hashPin(pin, salt)
        val prefix = if (isFakePin) KEY_FAKE else KEY_REAL
        prefs.edit()
            .putString("${prefix}_salt", salt)
            .putString("${prefix}_hash", hash)
            .commit()
    }

    fun verifyPin(pin: String, isFakePin: Boolean = false): Boolean {
        val prefix = if (isFakePin) KEY_FAKE else KEY_REAL
        val salt = prefs.getString("${prefix}_salt", null) ?: return false
        val storedHash = prefs.getString("${prefix}_hash", null) ?: return false
        return constantTimeEquals(storedHash, hashPin(pin, salt))
    }

    fun hasFakePin(): Boolean = prefs.contains("${KEY_FAKE}_hash")

    fun clearFakePin() {
        prefs.edit()
            .remove("${KEY_FAKE}_salt")
            .remove("${KEY_FAKE}_hash")
            .commit()
    }

    fun storeSecurityAnswer(answer: String) {
        val salt = generateSalt()
        val hash = hashPin(answer.lowercase().trim(), salt)
        prefs.edit()
            .putString(KEY_ANSWER_SALT, salt)
            .putString(KEY_ANSWER_HASH, hash)
            .commit()
    }

    fun verifySecurityAnswer(answer: String): Boolean {
        val salt = prefs.getString(KEY_ANSWER_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_ANSWER_HASH, null) ?: return false
        return constantTimeEquals(storedHash, hashPin(answer.lowercase().trim(), salt))
    }

    fun markSetupComplete() {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, true).commit()
    }

    fun getFailedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    fun incrementFailedAttempts(): Int {
        val count = getFailedAttempts() + 1
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, count).commit()
        return count
    }

    fun resetFailedAttempts() {
        prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).commit()
    }

    /** Clears all stored credentials and settings. Used by instrumentation tests only. */
    fun clearAllForTesting() {
        prefs.edit().clear().commit()
    }

    fun getString(key: String, default: String = ""): String =
        prefs.getString(key, default) ?: default

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean =
        prefs.getBoolean(key, default)

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)

    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun getLong(key: String, default: Long = 0L): Long = prefs.getLong(key, default)

    fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    private fun hashPin(pin: String, salt: String): String {
        val spec = PBEKeySpec(pin.toCharArray(), Base64.decode(salt, Base64.NO_WRAP), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean =
        MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    private fun ensureKeystoreKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = javax.crypto.KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE,
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build(),
            )
            keyGenerator.generateKey()
        }
    }

    companion object {
        private const val PREFS_NAME = "vault_secure_prefs"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS = "calculator_vault_master"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
        private const val KEY_REAL = "real_pin"
        private const val KEY_FAKE = "fake_pin"
        private const val KEY_ANSWER_SALT = "answer_salt"
        private const val KEY_ANSWER_HASH = "answer_hash"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val ITERATIONS = 100_000
        private const val KEY_LENGTH = 256
    }
}
