package id.ac.stmi.vertue

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class PaymentActivity : BaseActivity() {
    private var proof: Uri? = null
    private lateinit var order: Order

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        val mime = contentResolver.getType(uri) ?: ""
        val size = contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        when {
            mime != "image/jpeg" && mime != "image/png" -> toast("Format harus JPG atau PNG")
            size > 2 * 1024 * 1024 -> toast("Ukuran foto maksimal 2 MB")
            else -> {
                proof = uri
                findViewById<ImageView>(R.id.ivPreview).apply { setImageURI(uri); visibility = View.VISIBLE }
                findViewById<View>(R.id.tvUploadHint).visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val o = Repo.find(intent.getStringExtra("id"))
        if (o == null) { finish(); return }
        order = o
        setContentView(R.layout.activity_payment)
        val settle = o.dpPaid
        setupHeader(if (settle) "Pelunasan" else "Pembayaran DP")
        findViewById<TextView>(R.id.tvAmountLbl).text = if (settle) "SISA PELUNASAN" else "TOTAL DP (50%)"
        findViewById<TextView>(R.id.tvAmount).text = o.payAmount.rp()
        findViewById<TextView>(R.id.tvAmountSub).text = "Pesanan #${o.id} · ${o.title}"
        val tvAcc = findViewById<TextView>(R.id.tvAccount)
        tvAcc.text = ACCOUNT_NO
        tvAcc.setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("rek", ACCOUNT_NO))
            toast("Nomor rekening disalin")
        }
        val btn = findViewById<Button>(R.id.btnSend)
        val boxBank = findViewById<View>(R.id.boxBank)
        findViewById<RadioGroup>(R.id.rgMethod).setOnCheckedChangeListener { _, id ->
            val bank = id == R.id.rbBank
            boxBank.visibility = if (bank) View.VISIBLE else View.GONE
            btn.text = if (bank) "Kirim bukti pembayaran" else "Bayar dengan QRIS / e-wallet"
        }
        findViewById<View>(R.id.boxUpload).setOnClickListener { picker.launch("image/*") }
        btn.setOnClickListener {
            val bank = findViewById<RadioGroup>(R.id.rgMethod).checkedRadioButtonId == R.id.rbBank
            if (bank && proof == null) { toast("Upload bukti transfer terlebih dahulu"); return@setOnClickListener }
            if (bank) success("Bukti transfer terkirim. Petugas akan memverifikasi pembayaranmu.")
            else AlertDialog.Builder(this).setTitle("Simulasi QRIS")
                .setMessage("Demo: pembayaran ${o.payAmount.rp()} dianggap berhasil.")
                .setPositiveButton("Bayar") { _, _ -> success("Pembayaran diterima otomatis.") }
                .setNegativeButton("Batal", null).show()
        }
    }

    private fun success(msg: String) {
        if (!order.dpPaid) order.dpPaid = true else order.paidFull = true
        AlertDialog.Builder(this).setTitle("Berhasil").setMessage(msg).setCancelable(false)
            .setPositiveButton("Lihat pesanan") { _, _ ->
                startActivity(Intent(this, OrdersActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
                finish()
            }.show()
    }
}
