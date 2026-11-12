package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import vn.hvktmm.bach.securitylab.LabConfig
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.model.LabTransaction
import vn.hvktmm.bach.securitylab.network.PinnedHttpClient
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.security.FridaDetector
import vn.hvktmm.bach.securitylab.security.RootDetector
import vn.hvktmm.bach.securitylab.util.Notify
import vn.hvktmm.bach.securitylab.crypto.CryptoManager
import java.io.IOException
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private var isBalanceHidden = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AuthManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_home)
        if (intent.getBooleanExtra("LOGIN_SUCCESS", false)) Notify.show(this, "Đăng nhập thành công!", isSuccess = true)

        val scroll = findViewById<NestedScrollView>(R.id.scrollView)
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { scroll.smoothScrollTo(0, 0); true }
                R.id.nav_history -> { startActivity(Intent(this, HistoryActivity::class.java)); false }
                R.id.nav_wallet -> { startActivity(Intent(this, WalletActivity::class.java)); false }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); false }
                else -> false
            }
        }

        findViewById<TextView>(R.id.btnRefreshChecks).setOnClickListener { refreshChecks(); refreshData() }
        findViewById<ImageView>(R.id.btnToggleBalance).setOnClickListener {
            isBalanceHidden = !isBalanceHidden
            updateBalanceDisplay()
        }
        findViewById<View>(R.id.btnActionTransfer).setOnClickListener { openPaymentActivity(Intent(this, TransferActivity::class.java)) }
        findViewById<View>(R.id.btnActionTopUp).setOnClickListener { openPaymentActivity(Intent(this, TopUpActivity::class.java)) }
        findViewById<View>(R.id.btnActionScan).setOnClickListener { startActivity(Intent(this, ScanActivity::class.java)) }
        findViewById<View>(R.id.btnActionPhoneTopUp).setOnClickListener { openPaymentActivity(Intent(this, PhoneTopUpActivity::class.java)) }
        findViewById<View>(R.id.btnActionBill).setOnClickListener { openPaymentActivity(Intent(this, BillActivity::class.java)) }
        findViewById<View>(R.id.btnActionData).setOnClickListener { openPaymentActivity(Intent(this, DataActivity::class.java)) }
        findViewById<View>(R.id.btnActionSavings).setOnClickListener { openPaymentActivity(Intent(this, SavingsActivity::class.java)) }
        findViewById<View>(R.id.btnActionMovie).setOnClickListener { openPaymentActivity(Intent(this, MovieActivity::class.java)) }
        findViewById<View>(R.id.tvResult).setOnClickListener {
            if (AuthManager.isSecretVaultUnlocked()) {
                AlertDialog.Builder(this).setTitle("🔓 Kho lưu trữ bí mật")
                    .setMessage("Dữ liệu đã giải mã: \n${CryptoManager.decryptSecret()}")
                    .setPositiveButton("Đóng", null).show()
            } else Notify.show(this, "Tính năng này chỉ dành cho tài khoản Premium!", true)
        }
        refreshChecks()
        refreshData()
    }

    override fun onResume() {
        super.onResume()
        if (AuthManager.isLoggedIn()) refreshData()
    }

    private fun refreshData() {
        val token = AuthManager.sessionToken ?: return
        Thread {
            try {
                val profile = WalletApiClient.profile(token)
                val transactions = WalletApiClient.transactions(token)
                AuthManager.profile = profile
                runOnUiThread {
                    findViewById<TextView>(R.id.tvUserDisplayName).text = profile.displayName
                    updateBalanceDisplay()
                    val text = transactions.take(3).joinToString("\n\n") { transactionLine(it) }
                    findViewById<TextView>(R.id.tvResult).text = text.ifEmpty { "Chưa có giao dịch nào gần đây" }
                }
            } catch (error: Exception) {
                runOnUiThread { Notify.show(this, "Không tải được dữ liệu ví: ${error.message}", true) }
            }
        }.start()
    }

    private fun transactionLine(item: LabTransaction): String {
        val incoming = item.recipient.equals(AuthManager.profile?.username, true) || item.type.contains("Nạp") || item.type.contains("Nhận")
        val sign = if (incoming) "🟢" else "🔴"
        return "$sign ${item.type}: ${String.format(Locale.getDefault(), "%,d", item.amount)} VND"
    }

    private fun refreshChecks() {
        val rooted = RootDetector.isDeviceRooted(this)
        val hooked = FridaDetector.isFridaPresent()
        findViewById<TextView>(R.id.tvSecurityStatus).text = "🛡️ Hệ thống: ${if (LabConfig.VULNERABLE_MODE) "VULNERABLE" else "HARDENED"}\n🔍 Root: $rooted\n⚓ Hook: $hooked"
        if (rooted || hooked) Notify.show(this, "CẢNH BÁO: Thiết bị của bạn không an toàn!", true, true)
        checkServerConnectivity()
    }

    private fun isPaymentRestricted() = RootDetector.isDeviceRooted(this) || FridaDetector.isFridaPresent()

    private fun openPaymentActivity(intent: Intent) {
        if (isPaymentRestricted()) Notify.show(this, "Thanh toán bị khóa: thiết bị đang ROOT hoặc bị FRIDA/hooking.", true, true)
        else startActivity(intent)
    }

    private fun checkServerConnectivity() {
        val request = Request.Builder().url("https://${LabConfig.API_HOST}:${LabConfig.API_PORT}/status").build()
        PinnedHttpClient.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { if (e.message?.contains("Certificate pinning failure") == true) Toast.makeText(this@HomeActivity, "SSL Pinning: Đã chặn kết nối giả mạo!", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                response.close()
                runOnUiThread { if (response.isSuccessful) Toast.makeText(this@HomeActivity, "REST API: Kết nối an toàn OK", Toast.LENGTH_SHORT).show() }
            }
        })
    }

    private fun updateBalanceDisplay() {
        val balance = AuthManager.profile?.balance ?: 0L
        val text = findViewById<TextView>(R.id.tvSession)
        text.text = if (isBalanceHidden) "******** VND" else String.format(Locale.getDefault(), "%,d VND", balance)
        findViewById<ImageView>(R.id.btnToggleBalance).setImageResource(R.drawable.ic_eye)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
