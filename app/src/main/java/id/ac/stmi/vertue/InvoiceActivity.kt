package id.ac.stmi.vertue

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton

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

    /** Tombol rating: di bawah tombol bayar, hanya tampil kalau sudah lunas. */
    private fun bindRating(o: Order, pay: View, lunas: Boolean) {
        val parent = pay.parent as ViewGroup

        // Pakai tombol yang sudah ada kalau bind() dipanggil lagi (onResume), kalau belum buat baru.
        val btn: Button = parent.findViewWithTag<Button>("btnRating") ?: MaterialButton(
            this, null, com.google.android.material.R.attr.materialButtonStyle
        ).apply {
            tag = "btnRating"
            isAllCaps = false
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.white))
            backgroundTintList = ColorStateList.valueOf(getColor(R.color.gold))
            cornerRadius = dp(12)
            minimumHeight = dp(52)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
            parent.addView(this, parent.indexOfChild(pay) + 1)
        }

        btn.visibility = if (lunas && o.status != OrderStatus.CANCELLED) View.VISIBLE else View.GONE

        val sudahRating = getSharedPreferences("RatingPrefs", MODE_PRIVATE).contains("rating_${o.id}")
        btn.text = if (sudahRating) "Lihat Rating & Ulasan" else "Beri Rating & Ulasan"

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

        // Tombol rating muncul di bawah tombol bayar setelah pelunasan terkonfirmasi.
        bindRating(o, pay, o.paidFull)

        findViewById<Button>(R.id.btnDownload).text = "Bagikan tagihan"
        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            val text = "INVOICE #${o.id} — Vertue Interior Mobil\n${o.title} x1\nTotal: ${o.total.rp()}\n" +
                    "DP 50%: ${if (o.dpPaid) "Lunas" else "Belum"}\nSisa tagihan: ${o.remaining.rp()}"
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text),
                    "Bagikan invoice"
                )
            )
        }
    }
}