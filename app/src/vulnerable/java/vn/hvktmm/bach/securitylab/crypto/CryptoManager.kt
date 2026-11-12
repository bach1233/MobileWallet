package vn.hvktmm.bach.securitylab.crypto

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Vulnerable crypto path for the lab.
 * The key and ECB mode are intentionally recoverable from the APK.
 */
object CryptoManager {
    private const val KEY = "MobiLabKey2026!!"
    private const val ENCRYPTED_SECRET = "Ex6/DXyjLvpKiwhJb69jgHfbYLcmm7ob8YeC79WA5H0="

    @JvmStatic fun initialize(appContext: android.content.Context) = Unit

    @JvmStatic fun decryptSecret(): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(KEY.toByteArray(), "AES"))
        return String(cipher.doFinal(Base64.decode(ENCRYPTED_SECRET, Base64.DEFAULT)))
    }
}
