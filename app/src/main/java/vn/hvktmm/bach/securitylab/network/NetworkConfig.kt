package vn.hvktmm.bach.securitylab.network

import vn.hvktmm.bach.securitylab.LabConfig

object NetworkConfig {
    const val BASE_URL = "https://${LabConfig.API_HOST}:${LabConfig.API_PORT}"
    // The app's direct REST connection must work in the baseline condition.
    // Interception through Burp still fails because the proxy certificate has
    // a different public-key hash; bypassing pinning remains the lab scenario.
    const val LAB_CERT_PIN = "sha256/3M2Yt+10hp6Rbu2Vpe5mAEgRI2M1HI/wU7AB6ITs1UQ="
    const val HARDENED_CERT_PIN = "sha256/3M2Yt+10hp6Rbu2Vpe5mAEgRI2M1HI/wU7AB6ITs1UQ="
}
