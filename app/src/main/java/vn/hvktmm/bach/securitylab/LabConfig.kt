package vn.hvktmm.bach.securitylab

object LabConfig {
    const val LOG_TAG = "MOBILAB"
    const val VULNERABLE_MODE = BuildConfig.VULNERABLE_MODE
    // Android Emulator/MuMu route to the host machine used by the lab server.
    // Change this single value when the emulator uses a different host bridge.
    const val API_HOST = "10.0.2.2"
    const val API_PORT = 8443
}
