package vn.hvktmm.bach.securitylab.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import vn.hvktmm.bach.securitylab.R
import vn.hvktmm.bach.securitylab.logic.AuthManager
import vn.hvktmm.bach.securitylab.model.LabTransaction
import vn.hvktmm.bach.securitylab.network.WalletApiClient
import vn.hvktmm.bach.securitylab.util.Notify
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarHistory).setNavigationOnClickListener { finish() }
        val list = findViewById<RecyclerView>(R.id.rvHistory)
        val empty = findViewById<TextView>(R.id.tvEmptyHistory)
        Thread {
            try {
                val values = WalletApiClient.transactions(AuthManager.sessionToken ?: "")
                runOnUiThread {
                    empty.visibility = if (values.isEmpty()) View.VISIBLE else View.GONE
                    list.adapter = HistoryAdapter(values)
                }
            } catch (error: Exception) { runOnUiThread { Notify.show(this, "Không tải được lịch sử: ${error.message}", true) } }
        }.start()
    }

    class HistoryAdapter(private val list: List<LabTransaction>) : RecyclerView.Adapter<HistoryAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val title = v.findViewById<TextView>(R.id.tvTitle)
            val date = v.findViewById<TextView>(R.id.tvDate)
            val amount = v.findViewById<TextView>(R.id.tvAmount)
            val icon = v.findViewById<ImageView>(R.id.ivType)
            val cardIcon = v.findViewById<MaterialCardView>(R.id.cardIcon)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false))
        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            val incoming = item.recipient.equals(AuthManager.profile?.username, true) || item.type.contains("Nhận") || item.type.contains("Nạp")
            holder.title.text = item.type
            holder.date.text = formatDate(item.timestamp)
            holder.amount.text = (if (incoming) "+" else "-") + String.format(Locale.getDefault(), "%,d VND", item.amount)
            holder.amount.setTextColor(holder.itemView.context.getColor(if (incoming) R.color.accent else R.color.danger))
            holder.icon.setImageResource(if (item.type.contains("Nạp")) R.drawable.ic_topup else R.drawable.ic_transfer)
            holder.cardIcon.setCardBackgroundColor(holder.itemView.context.getColor(R.color.primary_light))
        }
        override fun getItemCount() = list.size
        private fun formatDate(value: String): String = try {
            val parsed = java.time.Instant.parse(value).toEpochMilli()
            SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()).format(Date(parsed))
        } catch (_: Exception) { value }
    }
}
