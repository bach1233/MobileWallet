package vn.hvktmm.bach.securitylab.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Hardened crypto path for the lab.
 * The AES key is retained by Android Keystore and the data uses GCM.
 */
object CryptoManager {
    private const val KEY_ALIAS = "mobiwallet_lab_secret"
    private const val PREFS = "hardened_secret_blob"
    private const val SECRET = "MOBILAB_HARDENED_SECRET_2026"
    private var context: Context? = null

    @JvmStatic fun initialize(appContext: Context) {
        context = appContext.applicationContext
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    @JvmStatic fun decryptSecret(): String {
        val ctx = requireNotNull(context) { "CryptoManager is not initialized" }
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        var iv = prefs.getString("iv", null)?.let { Base64.decode(it, Base64.NO_WRAP) }
        var ciphertext = prefs.getString("ciphertext", null)?.let { Base64.decode(it, Base64.NO_WRAP) }

        if (iv == null || ciphertext == null) {
            val encryptor = Cipher.getInstance("AES/GCM/NoPadding")
            encryptor.init(Cipher.ENCRYPT_MODE, key())
            iv = encryptor.iv
            ciphertext = encryptor.doFinal(SECRET.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                .putString("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                .apply()
        }

        val storedIv = requireNotNull(iv)
        val storedCiphertext = requireNotNull(ciphertext)
        val decryptor = Cipher.getInstance("AES/GCM/NoPadding")
        decryptor.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, storedIv))
        return String(decryptor.doFinal(storedCiphertext), Charsets.UTF_8)
    }
}
