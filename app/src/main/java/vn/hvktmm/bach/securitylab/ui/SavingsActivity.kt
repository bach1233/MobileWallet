package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify
import java.util.Locale

class SavingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarSavings).setNavigationOnClickListener { finish() }
        loadBalances()
        findViewById<MaterialButton>(R.id.btnSavingsDeposit).setOnClickListener { showAmountSheet(true) }
        findViewById<MaterialButton>(R.id.btnSavingsWithdraw).setOnClickListener { showAmountSheet(false) }
    }

    private fun loadBalances() {
        Thread {
            try {
                val profile = WalletApiClient.profile(AuthManager.sessionToken ?: "")
                AuthManager.profile = profile
                runOnUiThread {
                    findViewById<TextView>(R.id.tvSavingsBalance).text = String.format(Locale.getDefault(), "%,d VND", profile.savingsBalance)
                    findViewById<TextView>(R.id.tvSavingsProfit).text = String.format(Locale.getDefault(), "+ %,d đ", (profile.savingsBalance * 0.06 / 365).toLong())
                }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Không tải được số dư: ${error.message}", true) } }
        }.start()
    }

    private fun showAmountSheet(deposit: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_amount_bottom_sheet, null)
        dialog.setContentView(view)
        view.findViewById<TextView>(R.id.tvAmountSheetTitle).text = if (deposit) "Gửi tiền vào túi" else "Rút tiền về ví"
        view.findViewById<MaterialButton>(R.id.btnBottomSheetAmountNext).setOnClickListener {
            val amount = view.findViewById<EditText>(R.id.etBottomSheetAmount).text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
            if (amount <= 0) { Notify.show(this, "Số tiền không hợp lệ", true); return@setOnClickListener }
            dialog.dismiss(); showPinSheet(amount, deposit)
        }
        dialog.show()
    }

    private fun showPinSheet(amount: Long, deposit: Boolean) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_pin_bottom_sheet, null)
        dialog.setContentView(view)
        view.findViewById<MaterialButton>(R.id.btnBottomSheetConfirm).setOnClickListener {
            val pin = view.findViewById<EditText>(R.id.etBottomSheetPin).text.toString()
            if (AuthManager.verifyTransactionPin(pin)) { dialog.dismiss(); performTransaction(amount, deposit, pin) } else Notify.show(this, "Mã PIN không chính xác", true)
        }
        dialog.show()
    }

    private fun performTransaction(amount: Long, deposit: Boolean, pin: String) {
        Thread {
            try {
                AuthManager.profile = WalletApiClient.savings(AuthManager.sessionToken ?: "", amount, deposit, pin)
                runOnUiThread { Notify.show(this, if (deposit) "Gửi tiền thành công" else "Rút tiền thành công"); loadBalances() }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Giao dịch thất bại: ${error.message}", true) } }
        }.start()
    }
}
