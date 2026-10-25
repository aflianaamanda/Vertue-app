package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.CheckBox
import android.widget.EditText

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Session.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java)); finish(); return
        }
        setContentView(R.layout.activity_login)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPw = findViewById<EditText>(R.id.etPassword)
        findViewById<CheckBox>(R.id.cbShow).setOnCheckedChangeListener { _, checked ->
            etPw.transformationMethod =
                if (checked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            etPw.setSelection(etPw.text.length)
        }
        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val pw = etPw.text.toString()
            if (!Regex("^(\\+62|62|0)8[0-9]{8,12}$").matches(phone)) {
                etPhone.error = "Nomor tidak valid, contoh: 081234567890"; etPhone.requestFocus(); return@setOnClickListener
            }
            if (pw.length < 6) {
                etPw.error = "Kata sandi minimal 6 karakter"; etPw.requestFocus(); return@setOnClickListener
            }
            Session.login(this, phone)   // demo: belum ada backend, semua kredensial valid diterima
            startActivity(Intent(this, MainActivity::class.java)); finish()
        }
        findViewById<android.view.View>(R.id.tvForgot).setOnClickListener { showHelp() }
        findViewById<android.view.View>(R.id.tvRegister).setOnClickListener { toast("Pendaftaran dilakukan lewat workshop (demo)") }
    }
}
