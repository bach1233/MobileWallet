package vn.hvktmm.bach.securitylab.logic

import vn.hvktmm.bach.securitylab.model.WalletProfile
import vn.hvktmm.bach.securitylab.storage.SecretStore

/** 
 * PHIÊN BẢN HARDENED (MẠNH)
 * Loại bỏ các điểm yếu có thể bị khai thác runtime.
 */
object AuthManager {
    @Volatile var sessionToken: String? = null
    @Volatile var profile: WalletProfile? = null
    @JvmStatic fun isLoggedIn(): Boolean = sessionToken != null
    
    // Check gắt gao hơn, không dễ bị overwrite
    @JvmStatic fun isAdmin(): Boolean {
        if (profile == null) return false
        return profile!!.admin && profile!!.username == "admin"
    }
    
    @JvmStatic fun isPremium(): Boolean = profile?.premium == true

    @JvmStatic fun transferLimit(): Long {
        return when {
            isAdmin() -> 50000000L
            isPremium() -> 10000000L
            else -> 2000000L
        }
    }

    /** UI pre-check only; the authoritative policy must remain server-side. */
    @JvmStatic fun canTransfer(amount: Long): Boolean = amount > 0 && amount <= transferLimit()

    @JvmStatic fun isSecretVaultUnlocked(): Boolean = isAdmin() || isPremium()
    
    // Pin không để cứng trong code
    @JvmStatic fun verifyTransactionPin(pin: String): Boolean {
        // Trong bản hardened, nên check hash hoặc gọi lên server
        return pin == SecretStore.getSavedPin() 
    }

    fun establishSession(token: String, newProfile: WalletProfile, password: String) {
        sessionToken = token
        profile = newProfile
        SecretStore.saveToken(token)
        // KHÔNG log password ra ngoài
    }

    fun logout() {
        sessionToken = null
        profile = null
        SecretStore.clearSession()
    }
}
