package id.ac.stmi.vertue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView

class NotificationsActivity : BaseActivity() {
    private data class Notif(val title: String, val body: String, val time: String, val orderId: String, val unread: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        setupHeader("Notifikasi")
    }

    override fun onResume() {
        super.onResume();
        bind()
    }

    private fun bind() {
        val ready = Repo.orders.firstOrNull { it.status == OrderStatus.READY }
        val banner = findViewById<View>(R.id.banner)
        banner.visibility = if (ready != null) View.VISIBLE else View.GONE
        if (ready != null) {
            findViewById<TextView>(R.id.tvBannerSub).text = "${ready.title} sudah lolos QC dan siap diambil."
            findViewById<Button>(R.id.btnBanner).apply {
                text = "Lihat hasil & lunasi ${ready.remaining.rp()}"
                setOnClickListener { go(InvoiceActivity::class.java, ready.id) }
            }
        }

        val list = mutableListOf<Notif>()
        Repo.orders.forEach { o ->
            val n = when (o.status) {
                OrderStatus.WAITING_DP -> Notif("Menunggu pembayaran DP", "Bayar DP ${o.dp.rp()} sebelum ${o.dpDeadline} agar #${o.id} diproses.", o.created, o.id, true)
                OrderStatus.VERIFYING_DP -> Notif("DP sedang diverifikasi", "Bukti transfer #${o.id} sedang dicek petugas.", o.created, o.id, false)
                OrderStatus.DP_REJECTED -> Notif("Bukti DP ditolak", "Unggah ulang bukti transfer untuk #${o.id}.", o.created, o.id, true)
                OrderStatus.IN_PROGRESS -> Notif("Tahap: ${Repo.steps[o.step - 1]}", "Pesanan #${o.id} tahap ${o.step} dari 8.", "Hari ini", o.id, true)
                OrderStatus.READY -> Notif("Mobil kamu siap diambil", "Pesanan #${o.id} lolos QC. Lunasi ${o.remaining.rp()}.", o.estimate, o.id, true)
                OrderStatus.VERIFYING_FINAL -> Notif("Pelunasan diverifikasi", "Bukti pelunasan #${o.id} sedang dicek.", o.estimate, o.id, false)
                OrderStatus.FINAL_REJECTED -> Notif("Bukti pelunasan ditolak", "Unggah ulang bukti untuk #${o.id}.", o.estimate, o.id, true)
                OrderStatus.DELIVERING -> Notif("Mobil sedang diantar", "Driver mengantar mobil #${o.id} ke alamat Anda.", o.estimate, o.id, true)
                OrderStatus.COMPLETED -> Notif("Pesanan selesai", "Pesanan #${o.id} selesai.", o.estimate, o.id, false)
                OrderStatus.CANCELLED -> Notif("Pesanan dibatalkan", "Pesanan #${o.id} dibatalkan.", o.created, o.id, false)
            }
            list.add(n)
        }

        val box = findViewById<LinearLayout>(R.id.notifList)
        box.removeAllViews()
        findViewById<View>(R.id.tvNoNotif).visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

        list.forEach { n ->
            val v = LayoutInflater.from(this).inflate(R.layout.item_notif, box, false)
            v.findViewById<TextView>(R.id.tvNTitle).text = n.title
            v.findViewById<TextView>(R.id.tvNBody).text = n.body
            v.findViewById<TextView>(R.id.tvNTime).text = n.time
            v.findViewById<View>(R.id.unread).visibility = if (n.unread) View.VISIBLE else View.INVISIBLE
            if (n.unread) v.setBackgroundResource(R.drawable.bg_gold_strip)
            v.setOnClickListener { go(TrackingActivity::class.java, n.orderId) }
            v.findViewById<ImageButton>(R.id.btnNMore).setOnClickListener { a ->
                PopupMenu(this, a).apply {
                    menuInflater.inflate(R.menu.menu_popup_order, menu)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.pop_track -> go(TrackingActivity::class.java, n.orderId)
                            R.id.pop_invoice -> go(InvoiceActivity::class.java, n.orderId)
                            else -> showHelp()
                        }
                        true
                    }
                }.show()
            }
            (v.layoutParams as LinearLayout.LayoutParams).bottomMargin = dp(10)
            box.addView(v)
        }
    }
}