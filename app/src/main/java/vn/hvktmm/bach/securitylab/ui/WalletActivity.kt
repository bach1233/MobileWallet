package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.security.SensitiveStore
import vn.hvktmm.bach.securitylab.util.Notify
import java.util.Locale

class WalletActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarWallet).setNavigationOnClickListener { finish() }
        Thread {
            try {
                val profile = WalletApiClient.profile(AuthManager.sessionToken ?: "")
                AuthManager.profile = profile
                runOnUiThread {
                    val total = profile.balance + profile.savingsBalance
                    findViewById<TextView>(R.id.tvTotalAssets).text = String.format(Locale.getDefault(), "%,d VND", total)
                    findViewById<TextView>(R.id.tvMainBalanceWallet).text = String.format(Locale.getDefault(), "%,d VND", profile.balance)
                    findViewById<TextView>(R.id.tvSavingsBalanceWallet).text = String.format(Locale.getDefault(), "%,d VND", profile.savingsBalance)
                    findViewById<TextView>(R.id.tvCardHolder).text = profile.displayName
                    SensitiveStore.rememberCard("4111 2222 3333 4444")
                }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Không tải được ví: ${error.message}", true) } }
        }.start()
    }
}
