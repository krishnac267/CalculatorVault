package com.calculator.vault.security.auth

import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

/** Provides a stable passphrase for SQLCipher-backed Room storage. */
@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pinManager: PinManager,
) {
    fun getOrCreatePassphrase(): ByteArray {
        val encoded = pinManager.getString(KEY_DB_PASSPHRASE)
        if (encoded.isNotEmpty()) {
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        pinManager.putString(KEY_DB_PASSPHRASE, Base64.encodeToString(passphrase, Base64.NO_WRAP))
        return passphrase
    }

    fun clearForTesting() {
        pinManager.putString(KEY_DB_PASSPHRASE, "")
    }

    companion object {
        private const val KEY_DB_PASSPHRASE = "db_passphrase_v1"
    }
}
