package vn.hvktmm.bach.securitylab.security

/** Hardened path: synthetic secrets are not retained in process fields. */
object SensitiveStore {
    @JvmField var cardNumber: String = ""

    @JvmStatic fun seedLabSecrets() = Unit
    @JvmStatic fun rememberPassword(password: String) = Unit
    @JvmStatic fun rememberToken(token: String) = Unit
    @JvmStatic fun rememberCard(number: String) = Unit
    @JvmStatic fun getBearerToken(): String = ""
    @JvmStatic fun getSensitiveSummary(): String = "SensitiveStore: retention minimized"
    @JvmStatic fun clearSession() { cardNumber = "" }
}
