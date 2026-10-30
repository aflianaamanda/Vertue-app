package id.ac.stmi.vertue

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

/** Layar rating & ulasan setelah pesanan lunas. Semua kontrol selalu aktif dan bisa diubah ulang. */
class activity_detail_selesai : BaseActivity() {

    private var rating = 0
    private lateinit var orderId: String
    private lateinit var stars: List<TextView>
    private lateinit var chips: List<CheckBox>
    private lateinit var tvStarLabel: TextView
    private lateinit var etReview: EditText

    private val labels = listOf("", "Kurang puas", "Cukup", "Baik", "Sangat baik", "Luar biasa")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_selesai)
        setupHeader("Rating & Ulasan")

        orderId = intent.getStringExtra("id") ?: run { finish(); return }
        val o = Repo.find(orderId)
        findViewById<TextView>(R.id.tvRatingOrder).text = o?.let { "${it.title} · #${it.id}" } ?: "#$orderId"

        stars = listOf(R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5)
            .map { findViewById<TextView>(it) }
        chips = listOf(R.id.chipRapi, R.id.chipWaktu, R.id.chipKomunikasi, R.id.chipHarga)
            .map { findViewById<CheckBox>(it) }
        tvStarLabel = findViewById(R.id.tvStarLabel)
        etReview = findViewById(R.id.etReview)

        stars.forEachIndexed { i, s -> s.setOnClickListener { setRating(i + 1) } }

        // Isi ulang kalau pernah memberi rating (bisa diubah lagi).
        val prefs = getSharedPreferences("RatingPrefs", MODE_PRIVATE)
        val saved = prefs.getInt("rating_$orderId", 0)
        val savedTags = prefs.getStringSet("tags_$orderId", emptySet()) ?: emptySet()
        chips.forEach { it.isChecked = it.text.toString() in savedTags }
        etReview.setText(prefs.getString("review_$orderId", ""))
        setRating(saved)

        val submit = findViewById<Button>(R.id.btnSubmitRating)
        submit.text = if (saved > 0) "Perbarui rating" else "Kirim rating"
        submit.setOnClickListener {
            if (rating == 0) {
                toast("Pilih jumlah bintang dulu")
                return@setOnClickListener
            }
            prefs.edit()
                .putInt("rating_$orderId", rating)
                .putStringSet("tags_$orderId", chips.filter { it.isChecked }.map { it.text.toString() }.toSet())
                .putString("review_$orderId", etReview.text.toString().trim())
                .apply()
            toast("Rating terkirim")
            finish()
        }
    }

    private fun setRating(n: Int) {
        rating = n
        stars.forEachIndexed { i, s ->
            s.text = if (i < n) "★" else "☆"
            s.setTextColor(getColor(if (i < n) R.color.gold else R.color.line))
        }
        tvStarLabel.text = labels[n]
    }
}