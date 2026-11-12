package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify

class TopUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarTopUp).setNavigationOnClickListener { finish() }
        val amount = findViewById<EditText>(R.id.etTopUpAmount)
        val pinCard = findViewById<View>(R.id.cardTopUpPin)
        pinCard.visibility = View.GONE
        val quick = mapOf(R.id.btn50k to 50_000L, R.id.btn100k to 100_000L, R.id.btn200k to 200_000L, R.id.btn500k to 500_000L, R.id.btn1tr to 1_000_000L, R.id.btn2tr to 2_000_000L)
        quick.forEach { (id, value) -> findViewById<MaterialButton>(id).setOnClickListener { amount.setText(value.toString()) } }
        findViewById<MaterialButton>(R.id.btnConfirmTopUp).setOnClickListener { pinCard.visibility = View.VISIBLE }
        findViewById<MaterialButton>(R.id.btnFinalTopUp).setOnClickListener {
            val value = amount.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
            val pin = findViewById<EditText>(R.id.etTopUpPinNew).text.toString()
            if (value <= 0 || pin != "2468") { Notify.show(this, "Số tiền hoặc mã PIN không hợp lệ", true); return@setOnClickListener }
            val token = AuthManager.sessionToken ?: return@setOnClickListener
            Thread {
                try {
                    AuthManager.profile = WalletApiClient.topUp(token, value, pin)
                    runOnUiThread { Notify.show(this, "Nạp tiền thành công"); finish() }
                } catch (error: Exception) { runOnUiThread { Notify.show(this, "Nạp tiền thất bại: ${error.message}", true) } }
            }.start()
        }
    }
}
