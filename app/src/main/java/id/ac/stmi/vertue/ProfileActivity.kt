package id.ac.stmi.vertue

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView

class ProfileActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupHeader("Profil")
        setupBottomNav(R.id.nav_profile)
        val name = Session.name(this)
        findViewById<TextView>(R.id.tvAvatar).text = name.take(1).uppercase()
        findViewById<TextView>(R.id.tvPName).text = name
        findViewById<TextView>(R.id.tvPPhone).text = Session.phone(this)
        findViewById<TextView>(R.id.tvPAddr).text = Session.address(this)
        findViewById<Switch>(R.id.swNotif).apply {
            isChecked = Session.notifOn(this@ProfileActivity)
            setOnCheckedChangeListener { _, c -> Session.setNotif(this@ProfileActivity, c) }
        }
        findViewById<android.view.View>(R.id.btnHelp).setOnClickListener { showHelp() }
        findViewById<android.view.View>(R.id.btnLogout).setOnClickListener { confirmLogout() }
        findViewById<android.view.View>(R.id.btnDemo).setOnClickListener {
            val o = Repo.active()
            if (o == null) { toast("Tidak ada pesanan berjalan"); return@setOnClickListener }
            if (o.step < 8) o.step++
            if (o.step == 8) o.done = true
            toast("#${o.id} → ${Repo.steps[o.step - 1]}")
        }
    }
}
