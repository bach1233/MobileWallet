package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.CompoundBarcodeView
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager

class ScanActivity : AppCompatActivity() {
    private lateinit var barcodeView: CompoundBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarScan)
        toolbar.setNavigationOnClickListener { finish() }

        val ivMyQr = findViewById<ImageView>(R.id.ivMyQr)
        val tvMyEmail = findViewById<TextView>(R.id.tvMyEmail)
        
        // 1. Tạo QR của tôi
        val myEmail = AuthManager.profile?.username + "@lab.com"
        tvMyEmail.text = myEmail
        generateQrCode(myEmail, ivMyQr)

        // 2. Thiết lập Quét mã qua Camera
        barcodeView = findViewById(R.id.barcodeScanner)
        barcodeView.decodeContinuous { result ->
            barcodeView.pause() // Dừng quét khi tìm thấy
            val intent = Intent(this, TransferActivity::class.java)
            intent.putExtra("QR_EMAIL", result.text)
            startActivity(intent)
            finish()
        }
    }

    private fun generateQrCode(text: String, imageView: ImageView) {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}
