package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val email = findViewById<TextInputEditText>(R.id.etRegEmail)
        val name = findViewById<TextInputEditText>(R.id.etRegName)
        val phone = findViewById<TextInputEditText>(R.id.etRegPhone)
        val password = findViewById<TextInputEditText>(R.id.etRegPassword)
        val button = findViewById<MaterialButton>(R.id.btnDoRegister)
        findViewById<android.widget.TextView>(R.id.btnBackToLogin).setOnClickListener { finish() }

        button.setOnClickListener {
            val emailValue = email.text.toString().trim()
            val nameValue = name.text.toString().trim()
            val phoneValue = phone.text.toString().trim()
            val passwordValue = password.text.toString()
            if (emailValue.isEmpty() || nameValue.isEmpty() || phoneValue.isEmpty() || passwordValue.length < 6) {
                Notify.show(this, "Vui lòng nhập đầy đủ thông tin (MK >= 6 ký tự)", true)
                return@setOnClickListener
            }
            button.isEnabled = false
            Notify.show(this, "Đang khởi tạo tài khoản...")
            Thread {
                try {
                    val (token, profile) = WalletApiClient.register(emailValue, nameValue, phoneValue, passwordValue)
                    runOnUiThread {
                        AuthManager.establishSession(token, profile, passwordValue)
                        startActivity(Intent(this, HomeActivity::class.java).putExtra("LOGIN_SUCCESS", true))
                        finishAffinity()
                    }
                } catch (error: Exception) {
                    runOnUiThread {
                        button.isEnabled = true
                        Notify.show(this, error.message ?: "Đăng ký thất bại", true)
                    }
                }
            }.start()
        }
    }
}
