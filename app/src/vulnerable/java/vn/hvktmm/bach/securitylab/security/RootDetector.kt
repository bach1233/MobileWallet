package vn.hvktmm.bach.securitylab.security

import java.io.File

/**
 * PHIÊN BẢN VULNERABLE (YẾU) - Dành cho demo bypass
 */
object RootDetector {
    @JvmStatic
    fun isDeviceRooted(context: android.content.Context): Boolean {
        // Chỉ check 1 đường dẫn duy nhất, cực dễ bypass qua Frida
        return File("/system/bin/su").exists()
    }
}
