package vn.hvktmm.bach.securitylab.model

data class WalletProfile(
    val username: String,
    val email: String? = null,
    val displayName: String,
    val balance: Long,
    val premium: Boolean,
    val admin: Boolean,
    val phone: String? = null,
    val savingsBalance: Long = 0L
)

data class LabTransaction(
    val id: String,
    val sender: String,
    val recipient: String,
    val amount: Long,
    val timestamp: String,
    val status: String,
    val type: String = "Giao dịch",
    val message: String? = null
)
