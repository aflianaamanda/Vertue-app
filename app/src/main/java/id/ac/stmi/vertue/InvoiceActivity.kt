package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class InvoiceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)
        setupHeader("Tagihan")
    }

    override fun onResume() {
        super.onResume()
        bind()
    }

    private fun chipFor(v: TextView, s: PaymentStatus) {
        val (t, bg, col) = when (s) {
            PaymentStatus.CONFIRMED -> Triple("Lunas", R.drawable.bg_pill_green, R.color.green)
            PaymentStatus.VERIFYING -> Triple("Diverifikasi", R.drawable.bg_pill_gold, R.color.ink)
            PaymentStatus.REJECTED  -> Triple("Ditolak", R.drawable.bg_pill_red, R.color.red)
            PaymentStatus.UNPAID    -> Triple("Belum", R.drawable.bg_pill_red, R.color.red)
        }
        v.text = t
        v.setBackgroundResource(bg)
        v.setTextColor(getColor(col))
    }

    /** Tombol rating: hanya tampil kalau pelunasan sudah dikonfirmasi. */
    private fun bindRating(o: Order, anchor: View) {
        val parent = anchor.parent as ViewGroup
        var btn = parent.findViewWithTag<Button>("btnRating")
        if (btn == null) {
            btn = Button(this, null, 0, R.style.Btn_Gold).apply {
                tag = "btnRating"
                text = "Beri Rating & Ulasan"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            parent.addView(btn, parent.indexOfChild(anchor))
        }
        val lunas = o.finalStatus == PaymentStatus.CONFIRMED && o.status != OrderStatus.CANCELLED
        btn.visibility = if (lunas) View.VISIBLE else View.GONE
        btn.setOnClickListener { go(activity_detail_selesai::class.java, o.id) }
    }

    private fun bind() {
        val o = Repo.find(intent.getStringExtra("id")) ?: run { finish(); return }
        findViewById<TextView>(R.id.tvInvId).text = "#${o.id}"
        findViewById<TextView>(R.id.tvDue).text = if (o.estimate == "menyusul") "-" else o.estimate
        findViewById<TextView>(R.id.tvItem).text = "${o.title} x1"
        findViewById<TextView>(R.id.tvItemPrice).text = o.total.rp()
        findViewById<TextView>(R.id.tvTotal).text = o.total.rp()
        findViewById<TextView>(R.id.tvDpSub).text = "${o.created} · ${o.dp.rp()}"

        chipFor(findViewById(R.id.chipDp), o.dpStatus)
        findViewById<TextView>(R.id.tvPaySub).text = "Saat pengerjaan selesai · ${(o.total - o.dp).rp()}"
        chipFor(findViewById(R.id.chipPay), o.finalStatus)

        findViewById<TextView>(R.id.tvRemain).text = o.remaining.rp()
        findViewById<TextView>(R.id.tvSpelled).text =
            "Terbilang (total): ${terbilangRupiah(o.total)}. Hasil instalasi interior tidak dapat dikembalikan, namun dilindungi garansi sesuai S&K."

        val pay = findViewById<Button>(R.id.btnPay)
        val (label, enabled) = when (o.status) {
            OrderStatus.CANCELLED       -> "Pesanan dibatalkan" to false
            OrderStatus.VERIFYING_DP    -> "Menunggu verifikasi DP" to false
            OrderStatus.WAITING_DP      -> "Bayar DP ${o.dp.rp()}" to true
            OrderStatus.DP_REJECTED     -> "Bukti ditolak, bayar ulang DP" to true
            OrderStatus.IN_PROGRESS     -> "Pelunasan dibuka setelah pengerjaan selesai" to false
            OrderStatus.READY, OrderStatus.FINAL_REJECTED -> "Bayar pelunasan ${o.remaining.rp()}" to true
            OrderStatus.VERIFYING_FINAL -> "Menunggu verifikasi pelunasan" to false
            else                        -> "Sudah lunas" to false
        }
        pay.text = label
        pay.isEnabled = enabled
        pay.setOnClickListener { go(PaymentActivity::class.java, o.id) }

        val share = findViewById<Button>(R.id.btnDownload)
        share.text = "Bagikan tagihan"
        share.setOnClickListener {
            val text = "INVOICE #${o.id} — Vertue Interior Mobil\n${o.title} x1\nTotal: ${o.total.rp()}\n" +
                    "DP 50%: ${if (o.dpPaid) "Lunas" else "Belum"}\nSisa tagihan: ${o.remaining.rp()}"
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
                    "Bagikan invoice"
                )
            )
        }

        bindRating(o, share)
    }
}