package id.ac.stmi.vertue

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class ServicesActivity : BaseActivity() {

    private lateinit var list: LinearLayout
    private var selected = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)
        setupHeader("Semua Layanan")
        applyEdgeInsets(
            findViewById(R.id.servicesRoot),
            findViewById(R.id.headerRoot),
            findViewById(R.id.servicesScroll)
        )

        list = findViewById(R.id.catalogList)

        val group = findViewById<RadioGroup>(R.id.catGroup)
        Catalog.categories.forEach { cat ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = cat
                tag = cat
                isClickable = true
                isFocusable = true
                setButtonDrawable(null)
                setBackgroundResource(R.drawable.bg_chip)
                setTextColor(getColorStateList(R.color.chip_text))
                textSize = 13f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(18), dp(9), dp(18), dp(9))
            }
            val lp = RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            lp.marginEnd = dp(8)
            group.addView(rb, lp)
            if (cat == selected) rb.isChecked = true
        }
        group.setOnCheckedChangeListener { g, id ->
            selected = g.findViewById<View>(id)?.tag as? String ?: "Semua"
            render()
        }

        findViewById<View>(R.id.btnOrderNow).setOnClickListener { go(CreateOrderActivity::class.java) }
        findViewById<View>(R.id.btnContact).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$VERTUE_WA?text=Halo%20Vertue"))
            try { startActivity(intent) } catch (e: ActivityNotFoundException) { toast("Aplikasi tidak tersedia") }
        }

        render()
    }

    private fun render() {
        list.removeAllViews()
        val inflater = LayoutInflater.from(this)
        Catalog.categories.filter { it != "Semua" && (selected == "Semua" || it == selected) }.forEach { cat ->
            val items = Catalog.items.filter { it.category == cat }
            if (items.isEmpty()) return@forEach

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_card)
                setPadding(dp(20), dp(20), dp(20), dp(20))
            }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(16)

            card.addView(TextView(this).apply {
                text = cat
                setTextColor(getColor(R.color.ink))
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
            })
            items.forEachIndexed { i, item ->
                val row = inflater.inflate(R.layout.item_catalog, card, false)
                row.findViewById<View>(R.id.divider).visibility = if (i == 0) View.GONE else View.VISIBLE
                row.findViewById<TextView>(R.id.tvCatName).text = item.name
                row.findViewById<TextView>(R.id.tvCatPrice).text = item.price
                row.findViewById<TextView>(R.id.tvCatDesc).text = item.desc
                card.addView(row)
            }
            list.addView(card, lp)
        }
    }
}