package vn.hvktmm.bach.securitylab.security

/** Lab-only switch. It is disabled by default and only exists in the vulnerable build path. */
object LabTlsPolicy {
    @JvmStatic
    fun isInterceptionAllowed(): Boolean = false
}
