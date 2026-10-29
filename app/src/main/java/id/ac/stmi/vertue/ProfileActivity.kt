package id.ac.stmi.vertue

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : BaseActivity() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvAvatar: TextView

    // Memilih gambar dari galeri lewat picker sistem (tidak butuh izin storage)
    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) savePhoto(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Wajib dipanggil supaya tab bawah muncul dan aktif
        setupBottomNav(R.id.nav_profile)

        applyStatusBarPadding()

        ivAvatar = findViewById(R.id.ivAvatar)
        tvAvatar = findViewById(R.id.tvAvatar)

        loadPhoto()

        // --- FOTO PROFIL ---
        val openPhotoMenu = View.OnClickListener { showPhotoMenu(it) }
        findViewById<View>(R.id.btnAvatar).setOnClickListener(openPhotoMenu)
        findViewById<View>(R.id.btnEditPhoto).setOnClickListener(openPhotoMenu)
        findViewById<View>(R.id.tvChangePhoto).setOnClickListener(openPhotoMenu)

        // --- TOMBOL ---
        // Tombol back pindah ke Beranda, bukan finish()
        findViewById<View>(R.id.btnBack).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        findViewById<View>(R.id.btnSetting).setOnClickListener { go(ActivitySetting::class.java) }
        findViewById<View>(R.id.btnHelp).setOnClickListener { showHelp() }
        findViewById<View>(R.id.btnDemo).setOnClickListener { go(AdminActivity::class.java) }
        findViewById<View>(R.id.btnLogout).setOnClickListener { confirmLogout() }
    }

    // Dipanggil tiap kali layar tampil lagi, jadi perubahan dari Pengaturan langsung terlihat.
    override fun onResume() {
        super.onResume()
        bindUser()
    }

    private fun bindUser() {
        val name = Session.name(this).ifBlank { "Pengguna" }
        tvAvatar.text = name.take(1).uppercase()
        findViewById<TextView>(R.id.tvName).text = name
        findViewById<TextView>(R.id.tvEmail).text = Session.email(this).ifBlank { "-" }
        findViewById<TextView>(R.id.tvPhone).text = Session.phone(this).ifBlank { "-" }
        findViewById<TextView>(R.id.tvLocation).text = Session.address(this).ifBlank { "-" }
        loadPhoto()
    }

    /** Beri jarak atas seukuran status bar supaya toolbar tidak tertimpa. */
    private fun applyStatusBarPadding() {
        val header = findViewById<View>(R.id.headerProfile)
        val basePadTop = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(v.paddingLeft, basePadTop + top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    // ---------------------------------------------------------------
    // FOTO PROFIL
    // ---------------------------------------------------------------

    /** Satu file foto per pengguna, disimpan di penyimpanan internal aplikasi. */
    private fun photoFile(): File {
        return File(filesDir, "profile_${Session.uid(this)}.jpg")
    }

    private fun showPhotoMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 1, 0, "Pilih dari galeri")
        if (photoFile().exists()) popup.menu.add(0, 2, 1, "Hapus foto")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> pickPhoto.launch("image/*")
                2 -> {
                    photoFile().delete()
                    loadPhoto()
                    toast("Foto profil dihapus")
                }
            }
            true
        }
        popup.show()
    }

    private fun savePhoto(uri: Uri) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                toast("Gagal membuka gambar")
                return
            }

            // Perkecil dulu agar hemat memori (maks sekitar 800 px)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 1600 || bounds.outHeight / sample > 1600) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            if (bmp == null) {
                toast("File bukan gambar yang valid")
                return
            }

            // Perbaiki rotasi foto dari kamera
            val degrees = readRotation(bytes)
            if (degrees != 0f) {
                val m = Matrix().apply { postRotate(degrees) }
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
            }

            // Skala akhir ke maksimal 800 px sisi terpanjang
            val longest = maxOf(bmp.width, bmp.height)
            if (longest > 800) {
                val ratio = 800f / longest
                bmp = Bitmap.createScaledBitmap(
                    bmp, (bmp.width * ratio).toInt(), (bmp.height * ratio).toInt(), true
                )
            }

            FileOutputStream(photoFile()).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            loadPhoto()
            toast("Foto profil diperbarui")
        } catch (e: Exception) {
            toast("Gagal menyimpan foto")
        }
    }

    private fun readRotation(bytes: ByteArray): Float {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return 0f
        return try {
            val exif = ExifInterface(ByteArrayInputStream(bytes))
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    private fun loadPhoto() {
        val file = photoFile()
        val bmp = if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        if (bmp != null) {
            ivAvatar.setImageBitmap(bmp)
            ivAvatar.visibility = View.VISIBLE
            tvAvatar.visibility = View.GONE
        } else {
            ivAvatar.setImageDrawable(null)
            ivAvatar.visibility = View.GONE
            tvAvatar.visibility = View.VISIBLE
        }
    }
}