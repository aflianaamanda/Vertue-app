package id.ac.stmi.vertue

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        Repo.expireOverdue()
    }

    protected fun setupHeader(title: String) {
        findViewById<TextView>(R.id.headerTitle).text = title
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    protected fun setupBottomNav(selected: Int) {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNav)
        nav.selectedItemId = selected
        nav.setOnItemSelectedListener { item ->
            if (item.itemId != selected) {
                val target = when (item.itemId) {
                    R.id.nav_home -> MainActivity::class.java
                    R.id.nav_orders -> OrdersActivity::class.java
                    R.id.nav_tracking -> TrackingActivity::class.java
                    else -> ProfileActivity::class.java
                }
                startActivity(
                    Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NO_ANIMATION)
                )
                if (this !is MainActivity) finish()
            }
            true
        }
    }

    protected fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    protected fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    protected fun showHelp() {
        AlertDialog.Builder(this)
            .setTitle("Hubungi workshop")
            .setItems(arrayOf("Telepon", "WhatsApp")) { _, i ->
                val intent = if (i == 0) Intent(Intent.ACTION_DIAL, Uri.parse("tel:+$WORKSHOP_PHONE"))
                else Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$WORKSHOP_PHONE?text=Halo%20Vertue"))
                try { startActivity(intent) } catch (e: ActivityNotFoundException) { toast("Aplikasi tidak tersedia") }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    protected fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Yakin ingin keluar dari akun?")
            .setPositiveButton("Keluar") { _, _ ->
                Session.logout(this)
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    protected fun confirmCancel(o: Order, onDone: () -> Unit = {}) {
        if (!o.canCancel) {
            toast("Pesanan yang DP-nya sudah dibayar dibatalkan lewat workshop")
            showHelp()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Batalkan pesanan?")
            .setMessage("Pesanan #${o.id} akan dibatalkan.")
            .setPositiveButton("Batalkan") { _, _ ->
                o.cancelled = true
                toast("Pesanan dibatalkan")
                onDone()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    protected fun go(cls: Class<*>, id: String? = null) {
        val i = Intent(this, cls)
        if (id != null) i.putExtra("id", id)
        startActivity(i)
    }
}