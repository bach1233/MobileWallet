package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.security.FridaDetector
import vn.hvktmm.bach.securitylab.security.RootDetector
import vn.hvktmm.bach.securitylab.util.Notify
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class TransferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarTransfer).setNavigationOnClickListener { finish() }
        val recipient = findViewById<EditText>(R.id.etRecipient)
        val amount = findViewById<EditText>(R.id.etAmount)
        val message = findViewById<EditText>(R.id.etMessage)
        val confirm = findViewById<MaterialButton>(R.id.btnConfirmTransfer)
        val restricted = isPaymentRestricted()
        if (restricted) {
            confirm.isEnabled = false
            confirm.text = "THANH TOÁN BỊ KHÓA"
            confirm.setBackgroundColor(android.graphics.Color.GRAY)
            Notify.show(this, "Thanh toán bị khóa do phát hiện ${if (FridaDetector.isFridaPresent()) "FRIDA/hooking" else "ROOT"}.", true, true)
        }
        amount.addTextChangedListener(MoneyTextWatcher(amount))
        message.setText("${AuthManager.profile?.displayName ?: "User"} chuyển tiền")
        intent.getStringExtra("QR_EMAIL")?.takeIf { it.isNotEmpty() }?.let { recipient.setText(it) }
        confirm.setOnClickListener {
            if (isPaymentRestricted()) {
                Notify.show(this, "Thanh toán bị khóa: thiết bị đang bị can thiệp.", true, true)
                return@setOnClickListener
            }
            val target = recipient.text.toString().trim()
            val value = amount.text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
            val own = AuthManager.profile?.let { listOf(it.username, it.email, it.phone).filterNotNull() } ?: emptyList()
            if (target.isEmpty() || value <= 0) Notify.show(this, "Vui lòng nhập đầy đủ thông tin", true)
            else if (own.any { it.equals(target, true) }) Notify.show(this, "Bạn không thể tự chuyển cho chính mình!", true)
            else if (!AuthManager.canTransfer(value)) Notify.show(this, "Số tiền vượt quá hạn mức (${AuthManager.transferLimit()} VND).", true)
            else showPinBottomSheet()
        }
    }

    private fun showPinBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_pin_bottom_sheet, null)
        dialog.setContentView(view)
        val pin = view.findViewById<EditText>(R.id.etBottomSheetPin)
        val confirm = view.findViewById<MaterialButton>(R.id.btnBottomSheetConfirm)
        confirm.setOnClickListener {
            val value = pin.text.toString()
            if (AuthManager.verifyTransactionPin(value)) {
                dialog.dismiss()
                performFinalTransfer(value)
            } else Notify.show(this, "Mã PIN không chính xác", true)
        }
        dialog.show()
    }

    private fun performFinalTransfer(pin: String) {
        val recipient = findViewById<EditText>(R.id.etRecipient).text.toString().trim()
        val amount = findViewById<EditText>(R.id.etAmount).text.toString().replace(".", "").replace(",", "").toLongOrNull() ?: 0L
        val message = findViewById<EditText>(R.id.etMessage).text.toString()
        val token = AuthManager.sessionToken ?: return Notify.show(this, "Phiên đăng nhập đã hết hạn", true)
        Notify.show(this, "Đang xử lý giao dịch...")
        Thread {
            try {
                val transaction = WalletApiClient.transfer(token, recipient, amount, pin)
                runOnUiThread { Notify.dismiss(); showTransferSuccess(amount, transaction.recipient, message) }
            } catch (error: Exception) {
                runOnUiThread { Notify.show(this, "Giao dịch thất bại: ${error.message}", true) }
            }
        }.start()
    }

    private fun showTransferSuccess(amount: Long, recipient: String, message: String) {
        startActivity(Intent(this, SuccessActivity::class.java).apply {
            putExtra("AMOUNT", amount)
            putExtra("TYPE", "Chuyển tiền thành công")
            putExtra("RECEIVER", recipient)
            putExtra("SENDER", AuthManager.profile?.displayName)
            putExtra("MESSAGE", message)
        })
        finish()
    }

    private fun isPaymentRestricted() = RootDetector.isDeviceRooted(this) || FridaDetector.isFridaPresent()

    class MoneyTextWatcher(private val editText: EditText) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            editText.removeTextChangedListener(this)
            try {
                val value = s.toString().replace(",", "").replace(".", "").toLong()
                val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
                formatter.applyPattern("#,###,###,###")
                editText.setText(formatter.format(value).replace(",", "."))
                editText.setSelection(editText.text.length)
            } catch (_: Exception) { }
            editText.addTextChangedListener(this)
        }
    }
}
