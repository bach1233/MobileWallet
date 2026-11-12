package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import java.text.SimpleDateFormat
import java.util.*

class SuccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        val amount = intent.getLongValue("AMOUNT", 0L)
        val receiver = intent.getStringExtra("RECEIVER") ?: "Người nhận"
        val sender = intent.getStringExtra("SENDER") ?: "Ví MobiWallet"
        val message = intent.getStringExtra("MESSAGE") ?: "Chuyển tiền"
        val type = intent.getStringExtra("TYPE") ?: "Giao dịch thành công"

        findViewById<TextView>(R.id.tvSuccessTitle).text = type
        findViewById<TextView>(R.id.tvSuccessAmount).text = if (type.contains("Nạp tiền")) {
            "+ ${String.format("%,d", amount)} VND"
        } else {
            "- ${String.format("%,d", amount)} VND"
        }
        findViewById<TextView>(R.id.tvSuccessReceiver).text = receiver
        findViewById<TextView>(R.id.tvSuccessSender).text = sender
        findViewById<TextView>(R.id.tvSuccessMessage).text = message
        
        // Tạo mã giao dịch ngẫu nhiên
        val randomId = "MBW" + (10000000..99999999).random().toString()
        findViewById<TextView>(R.id.tvSuccessId).text = randomId
        
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvSuccessTime).text = sdf.format(Date())

        findViewById<MaterialButton>(R.id.btnBackHome).setOnClickListener {
            finish()
        }
    }
    
    // Hàm mở rộng để lấy long từ intent an toàn hơn
    private fun android.content.Intent.getLongValue(key: String, def: Long): Long {
        return getLongExtra(key, def)
    }
}
