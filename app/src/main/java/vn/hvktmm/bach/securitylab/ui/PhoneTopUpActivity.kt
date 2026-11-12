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

class PhoneTopUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_top_up)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarPhoneTopUp).setNavigationOnClickListener { finish() }
        val phone = findViewById<EditText>(R.id.etPhoneNumber)
        val amount = findViewById<EditText>(R.id.etCustomAmount)
        val pinCard = findViewById<View>(R.id.cardPhonePin)
        pinCard.visibility = View.GONE
        mapOf(R.id.btn10k to 10_000L, R.id.btn20k to 20_000L, R.id.btn50k to 50_000L, R.id.btn100k to 100_000L, R.id.btn200k to 200_000L, R.id.btn500k to 500_000L).forEach { (id, value) ->
            findViewById<MaterialButton>(id).setOnClickListener { amount.setText(value.toString()) }
        }
        findViewById<MaterialButton>(R.id.btnConfirmPhoneTopUp).setOnClickListener { pinCard.visibility = View.VISIBLE }
        findViewById<MaterialButton>(R.id.btnFinalPhoneTopUp).setOnClickListener {
            val value = amount.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
            val target = phone.text.toString().trim()
            val pin = findViewById<EditText>(R.id.etPhonePinNew).text.toString()
            if (target.isEmpty() || value <= 0 || pin != "2468") { Notify.show(this, "Thông tin hoặc mã PIN không hợp lệ", true); return@setOnClickListener }
            callPayment("Nạp ĐT", target, value, pin)
        }
    }

    private fun callPayment(type: String, recipient: String, amount: Long, pin: String) {
        Thread {
            try {
                val item = WalletApiClient.payment(AuthManager.sessionToken ?: "", type, recipient, amount, pin)
                runOnUiThread { Notify.show(this, "${item.type} thành công"); finish() }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Thanh toán thất bại: ${error.message}", true) } }
        }.start()
    }
}
