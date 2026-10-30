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
                findViewById<View>(R.id.ivUploadIcon).visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val o = Repo.find(intent.getStringExtra("id"))
        if (o == null) { finish(); return }
        order = o

        if (!o.canPay) {
            toast("Pembayaran sedang diverifikasi atau tidak diperlukan")
            finish()
            return
        }

        setContentView(R.layout.activity_payment)

        val settle = o.dpPaid
        setupHeader(if (settle) "Pelunasan" else "Pembayaran DP")
        findViewById<TextView>(R.id.tvAmountLbl).text = if (settle) "SISA PELUNASAN" else "TOTAL DP (50%)"
        findViewById<TextView>(R.id.tvAmount).text = o.payAmount.rp()

        val subText = "Pesanan #${o.id} · ${o.title}" + if (!settle) "\nBayar sebelum ${o.dpDeadline}" else ""
        findViewById<TextView>(R.id.tvAmountSub).text = subText

        val tvAcc = findViewById<TextView>(R.id.tvAccount)
        tvAcc.text = "$ACCOUNT_NO\na.n. $ACCOUNT_HOLDER"

        findViewById<Button>(R.id.btnCopy).setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("rek", ACCOUNT_NO.replace(" ", "")))
            toast("Nomor rekening disalin")
        }

        tvAcc.setOnClickListener {
            (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("rek", ACCOUNT_NO.replace(" ", "")))
            toast("Nomor rekening disalin")
        }

        val btn = findViewById<Button>(R.id.btnSend)
        findViewById<View>(R.id.boxUpload).setOnClickListener { picker.launch("image/*") }

        btn.setOnClickListener {
            if (proof == null) {
                toast("Upload bukti transfer terlebih dahulu")
                return@setOnClickListener
            }
            simulateAutoVerify()
        }
    }

    private fun simulateAutoVerify() {
        // Simulasi: Langsung Terkonfirmasi (Auto-Verify)
        if (order.dpPaid) {
            Repo.confirmPayment(order, true, true)
        } else {
            Repo.confirmPayment(order, false, true)
        }

        showSuccessDialog()
    }

    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val title = dialogView.findViewById<TextView>(R.id.tvSuccessTitle)
        val desc = dialogView.findViewById<TextView>(R.id.tvSuccessDesc)

        if (order.dpPaid && order.finalStatus == PaymentStatus.CONFIRMED) {
            title.text = "Pelunasan Berhasil!"
            desc.text = "Pembayaran pelunasan Anda telah diverifikasi. Terima kasih telah menggunakan layanan Vertue."
        } else {
            title.text = "DP Berhasil Diverifikasi!"
            desc.text = "Pembayaran DP Anda telah kami terima. Driver akan segera menghubungi Anda untuk penjemputan mobil."
        }

        dialogView.findViewById<Button>(R.id.btnSuccessOk).setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, OrdersActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            finish()
        }

        dialog.show()
    }
}