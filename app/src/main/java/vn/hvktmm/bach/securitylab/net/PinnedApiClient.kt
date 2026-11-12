package vn.hvktmm.bach.securitylab.net

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * SSL / Certificate Pinning via OkHttp CertificatePinner.
 * Thesis scenario 2: Bypass SSL Pinning (hook TrustManager / unpin).
 *
 * NOTE: pin is intentionally a FAKE hash so real https://httpbin.org will fail
 * until pinning is bypassed with Frida — perfect for lab demos.
 */
object PinnedApiClient {

    // Fake pin — will reject real httpbin.org cert until Frida bypass
    private const val HOST = "httpbin.org"
    private const val FAKE_PIN = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="

    private val pinner: CertificatePinner = CertificatePinner.Builder()
        .add(HOST, FAKE_PIN)
        .build()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .certificatePinner(pinner)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /** Returns body or throws — pinning failure is expected before bypass. */
    @JvmStatic
    fun fetchLabEndpoint(): String {
        val request = Request.Builder()
            .url("https://$HOST/get")
            .header("X-Lab-Token", vn.hvktmm.bach.securitylab.security.SensitiveStore.getBearerToken())
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                return "HTTP ${response.code}: $body"
            }
            return body.take(500)
        }
    }

    fun describe(): String {
        return "SSL Pinning: ON\n  host=$HOST\n  pin=$FAKE_PIN (fake — expect fail until bypass)"
    }
}
