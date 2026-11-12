package vn.hvktmm.bach.securitylab.logic

object TransferPolicy {
    fun limit(isAdmin: Boolean, isPremium: Boolean): Long = when {
        isAdmin -> 50_000_000L
        isPremium -> 10_000_000L
        else -> 2_000_000L
    }
    fun canTransfer(amount: Long, isAdmin: Boolean, isPremium: Boolean): Boolean = amount > 0 && amount <= limit(isAdmin, isPremium)
}
