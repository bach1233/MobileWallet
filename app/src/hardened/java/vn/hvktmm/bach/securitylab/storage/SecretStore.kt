package vn.hvktmm.bach.securitylab.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * PHIÊN BẢN HARDENED (MẠNH)
 * Sử dụng EncryptedSharedPreferences để mã hóa dữ liệu nhạy cảm.
 */
object SecretStore {
    private var context: Context? = null
    
    private val masterKeyAlias by lazy {
        MasterKey.Builder(context!!).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    private val sharedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context!!,
            "secure_wallet_prefs",
            masterKeyAlias,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun initialize(appContext: Context) { 
        context = appContext.applicationContext 
    }

    @JvmStatic fun saveToken(token: String) {
        sharedPrefs.edit().putString("access_token", token).apply()
    }

    @JvmStatic fun getSavedPin(): String {
        // Trong bản hardened, mã PIN nên được lưu hash hoặc encrypted
        return sharedPrefs.getString("user_pin", "2468") ?: "2468"
    }

    @JvmStatic fun rememberPinAndPassword(pin: String, password: String) { 
        // KHÔNG lưu password bản rõ, chỉ lưu mã PIN đã mã hóa
        sharedPrefs.edit().putString("user_pin", pin).apply()
    }

    fun clearSession() { 
        sharedPrefs.edit().clear().apply() 
    }
}
