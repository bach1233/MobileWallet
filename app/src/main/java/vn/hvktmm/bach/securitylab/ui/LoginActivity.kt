package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.security.FridaDetector
import vn.hvktmm.bach.securitylab.security.RootDetector
import vn.hvktmm.bach.securitylab.storage.SecretStore
import vn.hvktmm.bach.securitylab.util.Notify

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userField = findViewById<TextInputEditText>(R.id.etUsername)
        val passwordField = findViewById<TextInputEditText>(R.id.etPassword)
        val button = findViewById<MaterialButton>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.loginProgress)
        val warning = findViewById<TextView>(R.id.tvSecurityWarning)
        updateSecurityWarning(warning)

        val enter = TextView.OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT ||
                (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.action == android.view.KeyEvent.ACTION_DOWN)) {
                performLogin(userField, passwordField, button, progress)
                true
            } else false
        }
        userField.setOnEditorActionListener(enter)
        passwordField.setOnEditorActionListener(enter)
        button.setOnClickListener { performLogin(userField, passwordField, button, progress) }
        findViewById<TextView>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin(userField: TextInputEditText, passwordField: TextInputEditText, button: MaterialButton, progress: ProgressBar) {
        val username = userField.text?.toString().orEmpty().trim()
        val password = passwordField.text?.toString().orEmpty()
        if (username.isEmpty() || password.length < 6) {
            Notify.show(this, "Vui lòng nhập tài khoản và mật khẩu", true)
            return
        }
        button.isEnabled = false
        progress.visibility = View.VISIBLE
        Notify.show(this, "Đang đăng nhập...")
        Thread {
            try {
                val (token, profile) = WalletApiClient.login(username, password)
                runOnUiThread {
                    AuthManager.establishSession(token, profile, password)
                    handleSecurityAfterLogin(password, button, progress)
                }
            } catch (error: Exception) {
                runOnUiThread {
                    button.isEnabled = true
                    progress.visibility = View.GONE
                    Notify.show(this, error.message ?: "Không thể kết nối máy chủ REST", true)
                }
            }
        }.start()
    }

    private fun handleSecurityAfterLogin(password: String, button: MaterialButton, progress: ProgressBar) {
        val rooted = RootDetector.isDeviceRooted(this)
        val hooked = FridaDetector.isFridaPresent()
        updateSecurityWarning(findViewById(R.id.tvSecurityWarning))
        if (rooted) {
            rejectRootAndExit(hooked)
            return
        }
        vn.hvktmm.bach.securitylab.security.SensitiveStore.seedLabSecrets()
        vn.hvktmm.bach.securitylab.security.SensitiveStore.rememberPassword(password)
        SecretStore.rememberPinAndPassword("2468", password)
        completeLogin(hooked)
    }

    private fun updateSecurityWarning(view: TextView) {
        val rooted = RootDetector.isDeviceRooted(this)
        val hooked = FridaDetector.isFridaPresent()
        val message = when {
            rooted && hooked -> "CẢNH BÁO: thiết bị có dấu hiệu ROOT và FRIDA/hooking."
            rooted -> "CẢNH BÁO: phát hiện thiết bị ROOT."
            hooked -> "CẢNH BÁO: phát hiện FRIDA hoặc công cụ can thiệp runtime."
            else -> ""
        }
        view.text = message
        view.visibility = if (message.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun clearAuthenticatedSession() {
        AuthManager.logout()
        SecretStore.clearSession()
    }

    private fun rejectRootAndExit(isHooked: Boolean) {
        clearAuthenticatedSession()
        val reason = if (isHooked) "ROOT và FRIDA/hooking" else "ROOT"
        val warning = findViewById<TextView>(R.id.tvSecurityWarning)
        warning.text = "Đăng nhập bị hủy vì phát hiện $reason. Ứng dụng sẽ thoát để bảo vệ phiên đăng nhập."
        warning.visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnLogin).isEnabled = false
        findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
        Notify.show(this, "Đăng nhập bị hủy: phát hiện $reason", true, true)
        window.decorView.postDelayed({ finishAffinity() }, 1500L)
    }

    private fun completeLogin(isHooked: Boolean) {
        findViewById<ProgressBar>(R.id.loginProgress).visibility = View.GONE
        val intent = Intent(this, HomeActivity::class.java)
            .putExtra("LOGIN_SUCCESS", true)
        if (isHooked) {
            findViewById<TextView>(R.id.tvLoginStatus).apply {
                text = "Đăng nhập thành công!"
                visibility = View.VISIBLE
            }
            Notify.show(this, "Đăng nhập thành công!", isSuccess = true)
            intent.putExtra("SECURITY_RESTRICTED", true)
            window.decorView.postDelayed({ startActivity(intent); finish() }, 1200L)
        } else {
            startActivity(intent)
            finish()
        }
    }
}
