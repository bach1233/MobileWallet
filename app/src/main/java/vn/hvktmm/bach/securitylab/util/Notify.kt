package vn.hvktmm.bach.securitylab.util

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.card.MaterialCardView
import vn.hvktmm.bach.securitylab.R

object Notify {
    private var currentToast: Toast? = null

    fun show(activity: Activity?, message: String, isError: Boolean = false, isLong: Boolean = false, isSuccess: Boolean = false) {
        if (activity == null) return
        
        activity.runOnUiThread {
            try {
                currentToast?.cancel() // Hủy thông báo cũ ngay lập tức

                val inflater = LayoutInflater.from(activity)
                val layout = inflater.inflate(R.layout.layout_custom_toast, null)
                
                val text: TextView = layout.findViewById(R.id.text)
                text.text = message
                
                val card: MaterialCardView = layout.findViewById(R.id.toast_layout_root)
                val color = when {
                    isError -> R.color.danger
                    isSuccess -> R.color.ok
                    else -> R.color.primary
                }
                card.setCardBackgroundColor(activity.getColor(color))

                val toast = Toast(activity.applicationContext)
                toast.setGravity(Gravity.BOTTOM, 0, 200)
                toast.duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                toast.view = layout
                currentToast = toast
                toast.show()
            } catch (e: Exception) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun dismiss() {
        currentToast?.cancel()
    }
}
