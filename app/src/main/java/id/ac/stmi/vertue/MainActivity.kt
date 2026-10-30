package id.ac.stmi.vertue

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : BaseActivity() {
    private var ready = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Session.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setupBottomNav(R.id.nav_home)
        findViewById<View>(R.id.btnCreate).setOnClickListener { go(CreateOrderActivity::class.java) }
        findViewById<View>(R.id.tvAllServices).setOnClickListener { go(CreateOrderActivity::class.java) }
        findViewById<View>(R.id.tvSeeProgress).setOnClickListener { go(TrackingActivity::class.java, Repo.active()?.id) }
        findViewById<View>(R.id.btnInvoice).setOnClickListener {
            val o = Repo.active() ?: Repo.orders.firstOrNull()
            if (o == null) toast("Belum ada tagihan") else go(InvoiceActivity::class.java, o.id)
        }
        ready = true
    }

    override fun onResume() {
        super.onResume()
        if (ready) bind()
    }

    private fun bind() {
        findViewById<TextView>(R.id.tvName).text = Session.name(this)
        val o = Repo.active()
        findViewById<View>(R.id.cardActive).visibility = if (o != null) View.VISIBLE else View.GONE
        findViewById<View>(R.id.cardEmpty).visibility = if (o == null) View.VISIBLE else View.GONE
        if (o != null) {
            findViewById<TextView>(R.id.tvActiveTitle).text = o.title
            findViewById<TextView>(R.id.tvActiveSub).text = "Tahap: ${o.statusLabel} · Estimasi ${o.estimate}"
            findViewById<ProgressBar>(R.id.pbActive).progress = o.progress
        }
        val list = findViewById<LinearLayout>(R.id.serviceList)
        list.removeAllViews()
        Repo.services.forEach { s ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_service_row, list, false)
            row.findViewById<TextView>(R.id.tvSvcName).text = s.name
            row.findViewById<TextView>(R.id.tvSvcPrice).text = s.price.rp()
            list.addView(row)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_notif -> { go(NotificationsActivity::class.java); true }
        R.id.action_profile -> { go(ProfileActivity::class.java); true }
        R.id.action_help -> { showHelp(); true }
        R.id.action_logout -> { confirmLogout(); true }
        else -> super.onOptionsItemSelected(item)
    }
}