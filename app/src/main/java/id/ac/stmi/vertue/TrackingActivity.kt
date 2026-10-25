package id.ac.stmi.vertue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class TrackingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)
        setupHeader("Tracking Pesanan")
        setupBottomNav(R.id.nav_tracking)
        findViewById<Button>(R.id.btnContact).setOnClickListener { showHelp() }
    }

    override fun onResume() { super.onResume(); bind() }

    private fun bind() {
        val o = Repo.find(intent.getStringExtra("id")) ?: Repo.active() ?: Repo.orders.firstOrNull()
        val content = findViewById<View>(R.id.content)
        val empty = findViewById<View>(R.id.tvEmpty)
        if (o == null) { content.visibility = View.GONE; empty.visibility = View.VISIBLE; return }
        content.visibility = View.VISIBLE; empty.visibility = View.GONE

        findViewById<TextView>(R.id.tvOrderId).text = "PESANAN #${o.id}"
        findViewById<TextView>(R.id.tvOrderTitle).text = o.title
        findViewById<TextView>(R.id.tvOrderNote).text = o.note.ifBlank { "Dipesan ${o.created}" }
        findViewById<TextView>(R.id.tvEstimate).text = if (o.estimate == "menyusul") "Menyusul" else o.estimate
        val stage = if (!o.dpPaid) 0 else if (o.done) 8 else o.step
        findViewById<TextView>(R.id.tvStage).text = "$stage / 8"
        findViewById<ProgressBar>(R.id.pbTrack).progress = stage * 100 / 8
        findViewById<TextView>(R.id.tvHint).text = when {
            !o.dpPaid -> "Bayar DP agar pesanan mulai diproses."
            o.done -> "Mobil sudah siap diambil. Lunasi sisa tagihan sebelum pengambilan."
            else -> "Tinggal ${8 - o.step} tahap lagi. Kamu akan dapat notifikasi saat mobil siap diambil."
        }

        val bill = findViewById<Button>(R.id.btnBill)
        findViewById<TextView>(R.id.tvPayInfo).text = when {
            !o.dpPaid -> "DP belum dibayar · ${o.dp.rp()}"
            o.paidFull -> "Lunas"
            else -> "DP 50% lunas · sisa ${o.remaining.rp()}"
        }
        bill.text = if (!o.dpPaid) "Bayar DP" else "Lihat tagihan"
        bill.setOnClickListener { go(if (!o.dpPaid) PaymentActivity::class.java else InvoiceActivity::class.java, o.id) }

        val box = findViewById<LinearLayout>(R.id.timeline)
        box.removeAllViews()
        for (i in 1..8) {
            val row = LayoutInflater.from(this).inflate(R.layout.item_timeline, box, false)
            val done = o.dpPaid && (o.done || i < o.step)
            val active = o.dpPaid && !o.done && i == o.step
            val dot = row.findViewById<View>(R.id.dot)
            val line = row.findViewById<View>(R.id.line)
            val title = row.findViewById<TextView>(R.id.tvStepTitle)
            dot.setBackgroundResource(if (done) R.drawable.dot_done else if (active) R.drawable.dot_active else R.drawable.dot_todo)
            line.setBackgroundColor(getColor(if (done) R.color.gold else R.color.line))
            if (i == 8) line.visibility = View.INVISIBLE
            title.text = Repo.steps[i - 1]
            title.setTextColor(getColor(if (done || active) R.color.ink else R.color.muted))
            row.findViewById<TextView>(R.id.tvStepSub).text =
                o.info.getOrNull(i - 1) ?: (if (done) "Selesai" else "Estimasi menyusul")
            row.findViewById<View>(R.id.chipNow).visibility = if (active) View.VISIBLE else View.GONE
            row.findViewById<View>(R.id.photos).visibility = if (active && i == 5) View.VISIBLE else View.GONE
            box.addView(row)
        }
    }
}
