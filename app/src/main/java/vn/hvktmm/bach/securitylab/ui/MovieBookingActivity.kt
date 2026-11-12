package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MovieBookingActivity : AppCompatActivity() {
    private var movieName = "Phim"
    private var ticketPrice = 0L
    private val selectedSeats = mutableListOf<String>()
    private var selectedDate = ""
    private var selectedTime = "19:00"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_booking)
        movieName = intent.getStringExtra("MOVIE_NAME") ?: "Phim"
        ticketPrice = intent.getLongExtra("TICKET_PRICE", 0L)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarBooking).setNavigationOnClickListener { finish() }
        findViewById<TextView>(R.id.tvBookingMovieName).text = movieName
        intent.getIntExtra("MOVIE_IMAGE", 0).takeIf { it != 0 }?.let { findViewById<ImageView>(R.id.ivBookingMovie).setImageResource(it) }
        setupDates(); setupTimes(); setupSeats()
        findViewById<MaterialButton>(R.id.btnConfirmBooking).setOnClickListener { showPinBottomSheet() }
    }

    private fun setupDates() {
        val layout = findViewById<LinearLayout>(R.id.layoutDateSelection)
        val calendar = Calendar.getInstance()
        repeat(5) {
            val value = SimpleDateFormat("dd/MM", Locale.getDefault()).format(calendar.time)
            val button = MaterialButton(this).apply { text = value }
            button.setOnClickListener { selectedDate = value }
            layout.addView(button)
            if (selectedDate.isEmpty()) selectedDate = value
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun setupTimes() {
        val layout = findViewById<LinearLayout>(R.id.layoutTimeSelection)
        listOf("10:00", "14:30", "19:00", "21:45").forEach { value ->
            layout.addView(MaterialButton(this).apply { text = value; setOnClickListener { selectedTime = value } })
        }
    }

    private fun setupSeats() {
        val grid = findViewById<GridLayout>(R.id.gridSeats)
        for (row in listOf("A", "B", "C", "D", "E")) for (col in 1..8) {
            val seat = "$row$col"
            val view = View(this).apply {
                setBackgroundResource(R.drawable.bg_seat_available)
                layoutParams = GridLayout.LayoutParams().apply { width = 32.dp(); height = 32.dp(); setMargins(4.dp(), 4.dp(), 4.dp(), 4.dp()) }
                setOnClickListener { if (selectedSeats.remove(seat)) setBackgroundResource(R.drawable.bg_seat_available) else { selectedSeats.add(seat); setBackgroundResource(R.drawable.bg_seat_selected) }; updateTotal() }
            }
            grid.addView(view)
        }
        findViewById<TextView>(R.id.tvAvailableSeats).text = "Còn trống: 40/40"
    }

    private fun updateTotal() {
        findViewById<TextView>(R.id.tvSelectedSeats).text = if (selectedSeats.isEmpty()) "Chưa chọn ghế nào" else selectedSeats.joinToString(", ")
        findViewById<TextView>(R.id.tvBookingTotal).text = String.format(Locale.getDefault(), "%,dđ", ticketPrice * selectedSeats.size)
    }

    private fun showPinBottomSheet() {
        if (selectedSeats.isEmpty()) { Notify.show(this, "Vui lòng chọn ghế", true); return }
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_pin_bottom_sheet, null)
        dialog.setContentView(view)
        view.findViewById<MaterialButton>(R.id.btnBottomSheetConfirm).setOnClickListener {
            val pin = view.findViewById<EditText>(R.id.etBottomSheetPin).text.toString()
            if (AuthManager.verifyTransactionPin(pin)) { dialog.dismiss(); performPayment(pin) } else Notify.show(this, "Mã PIN không chính xác", true)
        }
        dialog.show()
    }

    private fun performPayment(pin: String) {
        val total = ticketPrice * selectedSeats.size
        Notify.show(this, "Đang xử lý thanh toán...")
        Thread {
            try {
                WalletApiClient.payment(AuthManager.sessionToken ?: "", "Mua vé xem phim", "Rạp phim CGV Cinema", total, pin, "$movieName ($selectedDate $selectedTime)")
                runOnUiThread {
                    Notify.dismiss()
                    startActivity(Intent(this, SuccessActivity::class.java).apply { putExtra("AMOUNT", total); putExtra("TYPE", "Mua vé phim thành công"); putExtra("RECEIVER", "Rạp phim CGV Cinema"); putExtra("MESSAGE", "Phim: $movieName ($selectedDate $selectedTime)") })
                    finish()
                }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Lỗi: ${error.message}", true) } }
        }.start()
    }

    private fun Int.dp() = (this * resources.displayMetrics.density).toInt()
}
