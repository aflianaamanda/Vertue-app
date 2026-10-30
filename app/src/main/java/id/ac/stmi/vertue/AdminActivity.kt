package id.ac.stmi.vertue

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    private lateinit var box: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Panel Workshop (Demo)"
        box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = (16 * resources.displayMetrics.density).toInt()
            setPadding(p, p, p, p)
        }
        setContentView(ScrollView(this).apply { addView(box) })
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        box.removeAllViews()

        val header = TextView(this).apply {
            text = "Daftar Pesanan Aktif"
            textSize = 18f
            setTextColor(getColor(R.color.ink))
            setPadding(0, 0, 0, 16)
        }
        box.addView(header)

        if (Repo.orders.isEmpty()) {
            box.addView(TextView(this).apply { text = "Belum ada pesanan" })
            return
        }

        Repo.orders.forEach { o ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(32, 32, 32, 32)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = 24
                layoutParams = lp
            }

            val info = TextView(this).apply {
                text = "#${o.id} · ${o.title}\n" +
                        "Mobil: ${o.carModel.ifBlank { "-" }} (${o.plate.ifBlank { "-" }})\n" +
                        "Status: ${o.status.label}\n" +
                        "Tahap: ${o.step} / 8"
                textSize = 14f
                setTextColor(getColor(R.color.ink))
            }
            card.addView(info)

            if (o.status == OrderStatus.IN_PROGRESS || o.status == OrderStatus.READY) {
                val btn = Button(this).apply {
                    text = if (o.step < 8) "Majukan Tahap → ${Repo.steps[o.step]}" else "Selesai Pengerjaan"
                    setBackgroundResource(R.drawable.bg_navy_card)
                    setTextColor(getColor(R.color.gold))
                    isEnabled = o.step < 8
                    setOnClickListener {
                        if (o.step < 8) {
                            Repo.advance(o)
                            render()
                        }
                    }
                }
                card.addView(btn)
            } else if (o.status == OrderStatus.DELIVERING) {
                val btn = Button(this).apply {
                    text = "Konfirmasi Mobil Sudah Diantar"
                    setBackgroundResource(R.drawable.bg_gold_strip)
                    setTextColor(getColor(R.color.ink))
                    setOnClickListener {
                        o.delivered = true
                        render()
                    }
                }
                card.addView(btn)
            } else {
                val infoStatus = TextView(this).apply {
                    text = "Tidak ada aksi untuk status ini."
                    textSize = 12f
                    setTextColor(getColor(R.color.muted))
                    setPadding(0, 16, 0, 0)
                }
                card.addView(infoStatus)
            }

            box.addView(card)
        }
    }
}