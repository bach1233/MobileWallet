package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify

class DataActivity : AppCompatActivity() {
    private var selectedAmount = 50_000L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarData).setNavigationOnClickListener { finish() }
        val phone = findViewById<EditText>(R.id.etDataPhone)
        listOf(R.id.cardDataDay to 50_000L, R.id.cardData3Day to 100_000L, R.id.cardDataWeek to 200_000L, R.id.cardDataMonth to 500_000L).forEach { (id, amount) ->
            findViewById<android.view.View>(id).setOnClickListener { selectedAmount = amount }
        }
        findViewById<MaterialButton>(R.id.btnDataConfirmPayment).setOnClickListener {
            val target = phone.text.toString().trim()
            if (target.isEmpty()) { Notify.show(this, "Vui lòng nhập số điện thoại", true); return@setOnClickListener }
            Thread {
                try {
                    WalletApiClient.payment(AuthManager.sessionToken ?: "", "Mua Data", target, selectedAmount)
                    runOnUiThread { Notify.show(this, "Mua gói Data thành công"); finish() }
                } catch (error: Exception) { runOnUiThread { Notify.show(this, "Mua Data thất bại: ${error.message}", true) } }
            }.start()
        }
    }
}
