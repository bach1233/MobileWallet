package vn.hvktmm.bach.securitylab.security

import android.content.Context
import java.io.File

/**
 * PHIÊN BẢN VULNERABLE (YẾU)
 * Thiết kế đơn giản, dễ bị bypass qua Frida Hooking.
 */
object SecurityProvider {
    
    // Check root sơ sài
    fun isDeviceRooted(context: Context): Boolean {
        return File("/system/bin/su").exists() || File("/system/xbin/su").exists()
    }

    // Check Frida hời hợt
    fun isFridaDetected(): Boolean {
        return false // Mặc định không làm gì để dễ demo
    }

    // Logic Premium lỏng lẻo
    fun isPremiumUser(): Boolean {
        return true // Luôn trả về true để demo logic manipulation
    }
}
