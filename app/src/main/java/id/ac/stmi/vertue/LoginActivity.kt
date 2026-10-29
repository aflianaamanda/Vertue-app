package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Session.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java)); finish(); return
        }

        setContentView(R.layout.activity_login)

        val etPhone = findViewById<EditText>(R.id.etEmail)
        val etPw = findViewById<EditText>(R.id.etPassword)

        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
            val email = etPhone.text.toString().trim()
            val pw = etPw.text.toString()

            // Pengecekan format Email
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etPhone.error = "Format email tidak valid, contoh: user@gmail.com"
                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (pw.length < 6) {
                etPw.error = "Kata sandi minimal 6 karakter"
                etPw.requestFocus()
                return@setOnClickListener
            }

            // Demo: belum ada backend, semua kredensial valid diterima
            Session.login(this, email)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<android.view.View>(R.id.tvForgot).setOnClickListener { showHelp() }
        findViewById<android.view.View>(R.id.tvRegister).setOnClickListener {
            toast("Pendaftaran dilakukan lewat workshop (demo)")
        }
    }
}