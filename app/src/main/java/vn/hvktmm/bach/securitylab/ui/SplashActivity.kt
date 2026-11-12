package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.security.FridaDetector
import vn.hvktmm.bach.securitylab.security.RootDetector

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val rooted = RootDetector.isDeviceRooted(this)
        val hooked = FridaDetector.isFridaPresent()
        findViewById<TextView>(R.id.tvSplashStatus).text = "MobiWallet Lab\nRoot: ${if (rooted) "detected" else "clear"} | Hook: ${if (hooked) "detected" else "clear"}"
        findViewById<TextView>(R.id.tvSplashStatus).postDelayed({ startActivity(Intent(this, LoginActivity::class.java)); finish() }, 700)
    }
}
