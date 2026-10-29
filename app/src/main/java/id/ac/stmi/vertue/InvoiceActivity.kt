package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class InvoiceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)
        setupHeader("Tagihan")
    }

    override fun onResume() { super.onResume(); bind() }

    private fun chip(v: TextView, text: String, ok: Boolean) {
        v.text = text
        v.setBackgroundResource(if (ok) R.drawable.bg_pill_green else R.drawable.bg_pill_red)
        v.setTextColor(getColor(if (ok) R.color.green else R.color.red))
    }

    private fun bind() {
        val o = Repo.find(intent.getStringExtra("id")) ?: run { finish(); return }
        findViewById<TextView>(R.id.tvInvId).text = "#${o.id}"
        findViewById<TextView>(R.id.tvDue).text = if (o.estimate == "menyusul") "-" else o.estimate
        findViewById<TextView>(R.id.tvItem).text = "${o.title} x1"
        findViewById<TextView>(R.id.tvItemPrice).text = o.total.rp()
        findViewById<TextView>(R.id.tvTotal).text = o.total.rp()
        findViewById<TextView>(R.id.tvDpSub).text = "${o.created} · ${o.dp.rp()}"
        chip(findViewById(R.id.chipDp), if (o.dpPaid) "Lunas" else "Belum", o.dpPaid)
        findViewById<TextView>(R.id.tvPaySub).text = "Saat pengerjaan selesai · ${(o.total - o.dp).rp()}"
        chip(findViewById(R.id.chipPay), if (o.paidFull) "Lunas" else "Belum", o.paidFull)
        findViewById<TextView>(R.id.tvRemain).text = o.remaining.rp()
        findViewById<TextView>(R.id.tvSpelled).text =
            "Terbilang: ${terbilangRupiah(o.remaining)}. Hasil instalasi interior tidak dapat dikembalikan, namun dilindungi garansi sesuai S&K."

        val pay = findViewById<Button>(R.id.btnPay)
        when {
            !o.dpPaid -> {
                pay.text = "Bayar DP ${o.dp.rp()}"
                pay.isEnabled = true
            }
            o.paidFull -> {
                pay.text = "Sudah lunas"
                pay.isEnabled = false
            }
            !o.done -> {
                pay.text = "Pelunasan setelah selesai"
                pay.isEnabled = true
            }
            else -> {
                pay.text = "Bayar pelunasan"
                pay.isEnabled = true
            }
        }

        pay.setOnClickListener {
            if (!o.dpPaid) {
                go(PaymentActivity::class.java, o.id)
            } else if (o.paidFull) {
                toast("Pesanan sudah lunas")
            } else if (!o.done) {
                AlertDialog.Builder(this)
                    .setTitle("Belum Bisa Pelunasan")
                    .setMessage("Pelunasan hanya bisa dilakukan setelah pengerjaan interior selesai. Saat ini mobil Anda masih dalam proses pengerjaan di workshop.")
                    .setPositiveButton("Mengerti", null)
                    .show()
            } else {
                go(PaymentActivity::class.java, o.id)
            }
        }

        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            val text = "INVOICE #${o.id} — Vertue Interior Mobil\n${o.title} x1\nTotal: ${o.total.rp()}\n" +
                    "DP 50%: ${if (o.dpPaid) "Lunas" else "Belum"}\nSisa tagihan: ${o.remaining.rp()}"
            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Bagikan invoice"))
        }
    }
}