package vn.hvktmm.bach.securitylab.storage

import android.content.Context
import android.util.Log
import vn.hvktmm.bach.securitylab.LabConfig

/**
 * PHIÊN BẢN VULNERABLE (YẾU)
 * Lưu trữ mã PIN và Token ở dạng văn bản thuần (Plaintext).
 */
object SecretStore {
    private var context: Context? = null
    
    @JvmField var tokenInMemory = ""
    @JvmField var transactionPinInMemory = ""

    fun initialize(appContext: Context) { 
        context = appContext.applicationContext 
    }

    @JvmStatic fun saveToken(token: String) {
        tokenInMemory = token
        // LỖI: Lưu token bản rõ vào SharedPreferences
        context?.getSharedPreferences("mobiwallet_lab", Context.MODE_PRIVATE)
            ?.edit()?.putString("access_token", token)?.apply()
    }

    @JvmStatic fun getSavedPin(): String {
        // LỖI: Mã PIN cứng hoặc lưu lỏng lẻo
        return "2468"
    }

    @JvmStatic fun rememberPinAndPassword(pin: String, password: String) { 
        transactionPinInMemory = pin
        // LỖI: Lưu cả password vào SharedPreferences để demo trích xuất
        context?.getSharedPreferences("mobiwallet_lab", Context.MODE_PRIVATE)
            ?.edit()?.putString("last_password", password)?.apply()
    }

    fun clearSession() { 
        tokenInMemory = ""
        transactionPinInMemory = ""
        context?.getSharedPreferences("mobiwallet_lab", Context.MODE_PRIVATE)?.edit()?.clear()?.apply() 
    }
}
