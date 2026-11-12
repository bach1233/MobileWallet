package vn.hvktmm.bach.securitylab.network

import vn.hvktmm.bach.securitylab.logic.AuthManager

/** Shared REST session state for the Firebase-free lab copy. */
object RestSession {
    val token: String?
        get() = AuthManager.sessionToken
}
