package id.ac.stmi.vertue

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class ActivitySetting : BaseActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setupHeader("Pengaturan Akun")
        applyEdgeInsets(
            findViewById(R.id.settingRoot),
            findViewById(R.id.headerRoot),
            findViewById(R.id.settingScroll)
        )

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)

        // Isi form hanya saat pertama dibuka (bukan saat layar diputar) agar ketikan tidak hilang
        if (savedInstanceState == null) {
            etName.setText(Session.name(this))
            etEmail.setText(Session.email(this))
            etPhone.setText(Session.phone(this))
            etAddress.setText(Session.address(this))
        }

        etPhone.imeOptions = EditorInfo.IME_ACTION_NEXT
        listOf(etName, etEmail, etPhone, etAddress).forEach { f ->
            f.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) { layoutOf(f)?.error = null }
            })
        }
        findViewById<View>(R.id.btnSave).setOnClickListener { save() }
    }

    private fun save() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().replace(" ", "").replace("-", "")
        val address = etAddress.text.toString().trim()

        when {
            name.length < 3 -> fail(etName, "Nama minimal 3 karakter")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> fail(etEmail, "Format email tidak valid, contoh: user@gmail.com")
            !isValidPhone(phone) -> fail(etPhone, "Nomor HP tidak valid, contoh: 081234567890")
            address.isEmpty() -> fail(etAddress, "Alamat wajib diisi")
            else -> {
                // Disimpan ke Session; Beranda, Profil, dan form Pesanan membaca ulang
                // data ini saat tampil, jadi perubahan langsung terlihat di seluruh aplikasi.
                Session.saveProfile(this, name, email, phone, address)
                toast("Perubahan disimpan")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun layoutOf(field: TextInputEditText): TextInputLayout? =
        generateSequence(field.parent) { it.parent }.filterIsInstance<TextInputLayout>().firstOrNull()

    private fun fail(field: TextInputEditText, msg: String) {
        layoutOf(field)?.error = msg
        field.requestFocus()
    }
}