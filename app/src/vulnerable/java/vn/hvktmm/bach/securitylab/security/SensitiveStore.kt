package vn.hvktmm.bach.securitylab.security

/** Vulnerable data-retention path using synthetic lab values. */
object SensitiveStore {
    @JvmField var apiKey: String = ""
    @JvmField var lastPassword: String = ""
    @JvmField var bearerToken: String = ""
    @JvmField var cardNumber: String = ""

    @JvmStatic fun seedLabSecrets() {
        apiKey = "AKIA_LAB_DEMO_KEY_DO_NOT_USE_IN_PROD"
        cardNumber = "4111111111111111"
    }

    @JvmStatic fun rememberPassword(password: String) { lastPassword = password }
    @JvmStatic fun rememberToken(token: String) { bearerToken = token }
    @JvmStatic fun rememberCard(number: String) { cardNumber = number }
    @JvmStatic fun getBearerToken(): String = bearerToken

    @JvmStatic fun getSensitiveSummary(): String = buildString {
        appendLine("SensitiveStore (in-memory):")
        appendLine("  apiKey     = $apiKey")
        appendLine("  password   = $lastPassword")
        appendLine("  token      = $bearerToken")
        appendLine("  cardNumber = $cardNumber")
    }

    @JvmStatic fun clearSession() {
        lastPassword = ""
        bearerToken = ""
    }
}
