package vn.hvktmm.bach.securitylab.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarProfile).setNavigationOnClickListener { finish() }
        val name = findViewById<TextView>(R.id.tvProfileName)
        val email = findViewById<TextView>(R.id.tvProfileEmail)
        val phone = findViewById<TextView>(R.id.tvProfilePhone)
        val type = findViewById<TextView>(R.id.tvAccountType)
        Thread {
            try {
                val profile = WalletApiClient.profile(AuthManager.sessionToken ?: "")
                AuthManager.profile = profile
                runOnUiThread {
                    name.text = profile.displayName
                    email.text = profile.email ?: "${profile.username}@lab.local"
                    phone.text = profile.phone ?: "Chưa cập nhật"
                    type.text = if (profile.admin) "Quản trị viên (Admin)" else "Thành viên Thường"
                    type.setTextColor(getColor(if (profile.admin) R.color.danger else R.color.accent))
                }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Không tải được hồ sơ: ${error.message}", true) } }
        }.start()
        findViewById<MaterialButton>(R.id.btnProfileLogout).setOnClickListener {
            AuthManager.logout()
            startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            finish()
        }
    }
}
