package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify
import java.util.Locale

class BillActivity : AppCompatActivity() {
    private var billAmount = 350_000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarBill).setNavigationOnClickListener { finish() }
        val info = findViewById<View>(R.id.cardBillInfo)
        val pin = findViewById<View>(R.id.cardBillPin)
        info.visibility = View.GONE
        pin.visibility = View.GONE
        findViewById<MaterialButton>(R.id.btnLookupBill).setOnClickListener {
            info.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvBillCustomerName).text = "Khách hàng MobiWallet"
            findViewById<TextView>(R.id.tvBillAmount).text = String.format(Locale.getDefault(), "%,d VND", billAmount)
        }
        findViewById<MaterialButton>(R.id.btnPayBill).setOnClickListener { pin.visibility = View.VISIBLE }
        findViewById<MaterialButton>(R.id.btnFinalBill).setOnClickListener {
            val code = findViewById<EditText>(R.id.etBillCode).text.toString().trim()
            val value = findViewById<EditText>(R.id.etBillPinNew).text.toString()
            if (code.isEmpty() || value != "2468") { Notify.show(this, "Mã hóa đơn hoặc PIN không hợp lệ", true); return@setOnClickListener }
            Thread {
                try {
                    WalletApiClient.payment(AuthManager.sessionToken ?: "", "Thanh toán tiền điện", code, billAmount, value)
                    runOnUiThread { Notify.show(this, "Thanh toán hóa đơn thành công"); finish() }
                } catch (error: Exception) { runOnUiThread { Notify.show(this, "Thanh toán thất bại: ${error.message}", true) } }
            }.start()
        }
    }
}
