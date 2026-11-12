package vn.hvktmm.bach.securitylab.security

import android.os.Build
import java.io.File

/**
 * PHIÊN BẢN HARDENED (MẠNH) - Đã gia cố bảo mật
 */
object RootDetector {
    @JvmStatic
    fun isDeviceRooted(context: android.content.Context): Boolean {
        val rootPaths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su", 
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su"
        )
        val binaryExists = rootPaths.any { File(it).exists() }
        val testKeys = Build.TAGS?.contains("test-keys") ?: false
        
        // Check thêm cả package manager cho Magisk/SuperSU
        val pm = context.packageManager
        val rootPackages = listOf("com.topjohnwu.magisk", "eu.chainfire.supersu")
        val pkgExists = rootPackages.any { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }

        return binaryExists || testKeys || pkgExists
    }
}
