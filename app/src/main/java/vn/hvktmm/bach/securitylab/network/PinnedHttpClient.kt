package vn.hvktmm.bach.securitylab.network

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import vn.hvktmm.bach.securitylab.LabConfig
import vn.hvktmm.bach.securitylab.security.LabTlsPolicy
import java.util.concurrent.TimeUnit

object PinnedHttpClient {
    private var client: OkHttpClient? = null

    @JvmStatic
    fun get(): OkHttpClient {
        if (client == null) {
            val builder = OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)

            val labInterception = LabConfig.VULNERABLE_MODE && LabTlsPolicy.isInterceptionAllowed()
            if (labInterception) {
                builder.hostnameVerifier { _, _ -> true }
            }

            val pinner = if (labInterception) {
                CertificatePinner.Builder().build()
            } else {
                val pin = if (LabConfig.VULNERABLE_MODE) NetworkConfig.LAB_CERT_PIN else NetworkConfig.HARDENED_CERT_PIN
                CertificatePinner.Builder().add(LabConfig.API_HOST, pin).build()
            }
            builder.certificatePinner(pinner)
            
            client = builder.build()
        }
        return client!!
    }
}
