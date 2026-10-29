package id.ac.stmi.vertue

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class activity_detail_selesai : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_selesai)

        // ID ini PASTI COCOK dengan XML di atas
        val ratingBar = findViewById<RatingBar>(R.id.ratingBarDetail)
        val etUlasan = findViewById<EditText>(R.id.etUlasanDetail)
        val btnKirim = findViewById<Button>(R.id.btnKirimUlasan)

        btnKirim.setOnClickListener {
            val bintang = ratingBar.rating
            val ulasan = etUlasan.text.toString()

            if (bintang == 0f) {
                Toast.makeText(this, "Silakan pilih bintang minimal 1", Toast.LENGTH_SHORT).show()
            } else {
                // Simpan ke SharedPreferences lokal (100% aman)
                val sharedPref = getSharedPreferences("RatingPrefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putFloat("bintang", bintang)
                editor.putString("ulasan", ulasan)
                editor.apply()

                Toast.makeText(this, "Terima kasih! Penilaian $bintang bintang tersimpan.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}