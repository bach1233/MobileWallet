package vn.hvktmm.bach.securitylab.anti

import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

object HookDetectionManager {
    @JvmStatic fun detectFridaServer(): Boolean = listOf(27042, 27043).any { port ->
        runCatching { Socket().use { it.connect(InetSocketAddress("127.0.0.1", port), 150) } }.isSuccess
    }
    @JvmStatic fun inspectMaps(): Boolean = runCatching {
        File("/proc/self/maps").readText().contains("frida", ignoreCase = true)
    }.getOrDefault(false)
    @JvmStatic fun detectFridaLibraries(): Boolean = inspectMaps()
    @JvmStatic fun isHookDetected(): Boolean = detectFridaServer() || inspectMaps() || detectFridaLibraries()
}
