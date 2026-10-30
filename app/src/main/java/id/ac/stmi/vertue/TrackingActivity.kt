package id.ac.stmi.vertue

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class TrackingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)
        setupHeader("Tracking Pesanan")
        setupBottomNav(R.id.nav_tracking)
        findViewById<Button>(R.id.btnContact).setOnClickListener { showHelp() }
    }

    override fun onResume() {
        super.onResume()
        bind()
    }

    private fun bind() {
        val o = Repo.find(intent.getStringExtra("id")) ?: Repo.active() ?: Repo.orders.firstOrNull()
        val content = findViewById<View>(R.id.content)
        val empty = findViewById<View>(R.id.tvEmpty)

        if (o == null) {
            content.visibility = View.GONE
            empty.visibility = View.VISIBLE
            return
        }

        content.visibility = View.VISIBLE
        empty.visibility = View.GONE

        val cardDriver = findViewById<View>(R.id.cardDriver)
        val d = o.driver

        val phase = when {
            o.status == OrderStatus.DELIVERING -> 3
            o.isPickup && o.dpPaid && !o.cancelled && o.step <= 2 -> o.step
            else -> 0
        }

        if (d != null && phase != 0) {
            cardDriver.visibility = View.VISIBLE
            findViewById<TextView>(R.id.tvDriverName).text = d.name
            findViewById<TextView>(R.id.tvDriverPlate).text = "Plat: ${d.plate}"

            val addrLabel = if (phase == 3) "Alamat antar: " else "Lokasi jemput: "
            findViewById<TextView>(R.id.tvPickupAddress).text = "$addrLabel${o.pickupAddress}"

            val tvStatus = findViewById<TextView>(R.id.tvPickupStatus)
            val tvEta = findViewById<TextView>(R.id.tvEta)
            val btnConfirm = findViewById<Button>(R.id.btnConfirmPickup)

            if (phase == 1) {
                tvStatus.text = "Driver sedang menuju lokasi Anda"
                tvEta.visibility = View.VISIBLE
                tvEta.text = "Estimasi tiba: 15 menit lagi"
                btnConfirm.visibility = View.VISIBLE
                btnConfirm.text = "Konfirmasi serah terima"
                btnConfirm.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Konfirmasi Penjemputan")
                        .setMessage("Apakah driver sudah menjemput mobil Anda?")
                        .setPositiveButton("Ya, Sudah") { _, _ ->
                            o.step = 2
                            Repo.orders.remove(o)
                            Repo.orders.add(0, o)
                            toast("Status diperbarui: Mobil menuju workshop")
                            bind()
                        }
                        .setNegativeButton("Belum", null)
                        .show()
                }
            } else if (phase == 2) {
                tvStatus.text = "Mobil dalam perjalanan ke workshop"
                tvEta.visibility = View.VISIBLE
                tvEta.text = "Estimasi tiba di workshop: 20 menit lagi"
                btnConfirm.visibility = View.GONE
            } else if (phase == 3) {
                tvStatus.text = "Driver sedang mengantar mobil Anda"
                tvEta.visibility = View.VISIBLE
                tvEta.text = "Estimasi tiba di lokasi: 30 menit lagi"
                btnConfirm.visibility = View.VISIBLE
                btnConfirm.text = "Mobil sudah diterima"
                btnConfirm.setOnClickListener {
                    o.delivered = true
                    toast("Terima kasih! Pesanan selesai.")
                    bind()
                }
            }

            val btnCall = findViewById<Button>(R.id.btnCallDriver)
            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${d.phone}"))
                try { startActivity(intent) } catch (e: Exception) { toast("Aplikasi Telepon tidak tersedia") }
            }

            val btnMap = findViewById<Button>(R.id.btnMapDriver)
            btnMap.text = if (phase == 3) "Lacak Pengantaran" else "Lacak Posisi"
            btnMap.setOnClickListener {
                if (phase == 1 || phase == 3) {
                    val dialogView = layoutInflater.inflate(R.layout.dialog_tracking_driver, null)
                    val dialog = AlertDialog.Builder(this).setView(dialogView).create()
                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                    val titleMsg = if (phase == 3) "Driver (${d.name}) sedang mengantar mobil Anda." else "Driver (${d.name}) sedang dalam perjalanan menuju lokasi Anda."
                    dialogView.findViewById<TextView>(R.id.tvTrackingStatus).text = titleMsg
                    dialogView.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
                    dialog.show()
                } else {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=Vertue+Interior+Workshop")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try { startActivity(mapIntent) } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=Vertue+Interior+Workshop")))
                    }
                }
            }
        } else {
            cardDriver.visibility = View.GONE
        }

        findViewById<TextView>(R.id.tvOrderId).text = "PESANAN #${o.id}"
        findViewById<TextView>(R.id.tvOrderTitle).text = o.title
        findViewById<TextView>(R.id.tvOrderNote).text = o.note.ifBlank { "Dipesan ${o.created}" }
        findViewById<TextView>(R.id.tvEstimate).text = if (o.estimate == "menyusul") "Menyusul" else o.estimate

        val stage = if (!o.dpPaid) 0 else if (o.done) 8 else o.step
        findViewById<TextView>(R.id.tvStage).text = "$stage / 8"
        findViewById<ProgressBar>(R.id.pbTrack).progress = stage * 100 / 8

        findViewById<TextView>(R.id.tvHint).text = when (o.status) {
            OrderStatus.WAITING_DP      -> "Bayar DP sebelum ${o.dpDeadline} agar pesanan diproses."
            OrderStatus.VERIFYING_DP    -> "Bukti DP sedang diverifikasi petugas."
            OrderStatus.DP_REJECTED     -> "Bukti DP ditolak. Unggah ulang bukti transfer."
            OrderStatus.IN_PROGRESS     -> "Tinggal ${8 - o.step} tahap lagi. Kamu akan dapat notifikasi saat mobil siap."
            OrderStatus.READY           -> "Mobil siap. Lunasi sisa tagihan sebelum pengambilan/pengantaran."
            OrderStatus.VERIFYING_FINAL -> "Bukti pelunasan sedang diverifikasi."
            OrderStatus.FINAL_REJECTED  -> "Bukti pelunasan ditolak. Unggah ulang bukti transfer."
            OrderStatus.DELIVERING      -> "Mobil sedang diantar kembali ke alamat Anda."
            OrderStatus.COMPLETED       -> "Pesanan selesai. Terima kasih!"
            OrderStatus.CANCELLED       -> "Pesanan dibatalkan."
        }

        val bill = findViewById<Button>(R.id.btnBill)
        val toPay = o.status == OrderStatus.WAITING_DP || o.status == OrderStatus.DP_REJECTED
        bill.text = if (toPay) "Bayar DP" else "Lihat tagihan"
        bill.setOnClickListener { go(if (toPay) PaymentActivity::class.java else InvoiceActivity::class.java, o.id) }

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