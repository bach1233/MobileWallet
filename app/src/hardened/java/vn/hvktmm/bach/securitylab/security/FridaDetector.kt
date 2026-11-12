package vn.hvktmm.bach.securitylab.security

import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

/**
 * PHIÊN BẢN HARDENED (MẠNH) - Đã gia cố bảo mật
 */
object FridaDetector {
    @JvmStatic
    fun isFridaPresent(): Boolean {
        // 1. Check Port
        val portDetected = listOf(27042, 27043).any { port ->
            runCatching { Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 100) } }.isSuccess
        }
        
        // 2. Check /proc/self/maps (phát hiện thư viện frida-agent)
        val mapsDetected = runCatching {
            File("/proc/self/maps").readText().contains("frida", ignoreCase = true)
        }.getOrDefault(false)

        return portDetected || mapsDetected
    }
}
