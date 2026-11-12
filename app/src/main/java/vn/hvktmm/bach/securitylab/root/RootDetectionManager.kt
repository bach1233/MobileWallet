package vn.hvktmm.bach.securitylab.root

import android.content.Context
import android.os.Build
import java.io.File

object RootDetectionManager {
    private val suPaths = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su", "/data/adb/magisk")
    private val rootPackages = listOf("com.topjohnwu.magisk", "eu.chainfire.supersu", "com.kingroot.kinguser")
    @JvmStatic fun checkSuBinary(): Boolean = suPaths.any { File(it).exists() }
    @JvmStatic fun checkRootPackages(context: Context): Boolean = rootPackages.any { pkg ->
        runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess
    }
    @JvmStatic fun checkTestKeys(): Boolean = Build.TAGS?.contains("test-keys") == true
    @JvmStatic fun checkDangerousProperties(): Boolean = Build.TYPE == "eng" || Build.TYPE == "userdebug"
    @JvmStatic fun isDeviceRooted(context: Context): Boolean =
        checkSuBinary() || checkRootPackages(context) || checkTestKeys() || checkDangerousProperties()
}
