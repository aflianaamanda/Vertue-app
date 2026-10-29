package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class InvoiceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)
        setupHeader("Tagihan")

        // Logika Tombol Rating
        val btnRating = findViewById<Button>(R.id.btnBeriRating)

        // Cek jika tombol ada di layout, pasang aksinya
        btnRating?.setOnClickListener {
            val intent = Intent(this, activity_detail_selesai::class.java)
            startActivity(intent)
        }
    }
}