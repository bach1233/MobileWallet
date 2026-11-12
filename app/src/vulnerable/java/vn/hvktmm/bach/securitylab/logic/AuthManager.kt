package vn.hvktmm.bach.securitylab.logic

import android.util.Log
import vn.hvktmm.bach.securitylab.LabConfig
import vn.hvktmm.bach.securitylab.model.WalletProfile
import vn.hvktmm.bach.securitylab.storage.SecretStore

/** 
 * PHIÊN BẢN VULNERABLE (YẾU) 
 * Các hàm được đánh dấu @JvmStatic để dễ dàng bị Frida hook và thay đổi kết quả.
 */
object AuthManager {
    @Volatile var sessionToken: String? = null
    @Volatile var profile: WalletProfile? = null
    @Volatile var premiumOverride = false
    @JvmStatic fun isLoggedIn(): Boolean = sessionToken != null
    
    // Mục tiêu Hook: Ép trả về true để có quyền Admin
    @JvmStatic fun isAdmin(): Boolean = profile?.admin == true
    
    // Mục tiêu Hook: Ép trả về true để dùng Premium miễn phí
    @JvmStatic fun isPremium(): Boolean = premiumOverride || profile?.premium == true
    
    // Mục tiêu Hook: Nâng hạn mức chuyển tiền lên vô hạn
    @JvmStatic fun transferLimit(): Long {
        return when {
            isAdmin() -> 50000000L
            isPremium() -> 10000000L
            else -> 2000000L
        }
    }

    /** Client-side policy intentionally exposed for the runtime-hook lab. */
    @JvmStatic fun canTransfer(amount: Long): Boolean = amount > 0 && amount <= transferLimit()

    // Mục tiêu Hook: Cho phép vào kho bí mật
    @JvmStatic fun isSecretVaultUnlocked(): Boolean = isAdmin() || isPremium()

    @JvmStatic fun verifyTransactionPin(pin: String): Boolean = pin == "2468"

    fun establishSession(token: String, newProfile: WalletProfile, password: String) {
        sessionToken = token
        profile = newProfile
        SecretStore.saveToken(token)
        // Lưu cả password vào log để dễ "soi"
        Log.d(LabConfig.LOG_TAG, "Vulnerable Session: user=${newProfile.username}, pass=$password")
    }

    fun logout() {
        sessionToken = null
        profile = null
        premiumOverride = false
        SecretStore.clearSession()
    }
}
