package vn.hvktmm.bach.securitylab.security

import java.io.File

/**
 * PHIÊN BẢN VULNERABLE (YẾU) - Dành cho demo bypass
 */
object FridaDetector {
    @JvmStatic
    fun isFridaPresent(): Boolean {
        // Bản vulnerable vẫn có tín hiệu Anti-Frida để làm mốc thực nghiệm,
        // nhưng chỉ dùng một dấu hiệu đơn giản nên có thể bypass dễ dàng.
        return runCatching {
            File("/proc/self/maps").readText().contains("frida", ignoreCase = true)
        }.getOrDefault(false)
    }
}
