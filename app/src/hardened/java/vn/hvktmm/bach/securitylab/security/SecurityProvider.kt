package vn.hvktmm.bach.securitylab.security

import android.content.Context
import android.os.Build
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

/**
 * PHIÊN BẢN HARDENED (MẠNH)
 * Kiểm tra đa lớp, khó bypass hơn nhiều.
 */
object SecurityProvider {

    fun isDeviceRooted(context: Context): Boolean {
        val paths = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/data/adb/magisk")
        val packages = listOf("com.topjohnwu.magisk", "eu.chainfire.supersu")
        
        val binaryExists = paths.any { File(it).exists() }
        val packageExists = packages.any { pkg ->
            runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
        }
        val testKeys = Build.TAGS?.contains("test-keys") == true
        
        return binaryExists || packageExists || testKeys
    }

    fun isFridaDetected(): Boolean {
        // Quét port và maps thực sự
        val portDetected = listOf(27042, 27043).any { port ->
            runCatching { Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 100) } }.isSuccess
        }
        val mapsDetected = runCatching { 
            File("/proc/self/maps").readText().contains("frida", ignoreCase = true) 
        }.getOrDefault(false)
        
        return portDetected || mapsDetected
    }

    fun isPremiumUser(): Boolean {
        // Giả lập check server token thực sự, không chỉ là biến flag
        return false 
    }
}
