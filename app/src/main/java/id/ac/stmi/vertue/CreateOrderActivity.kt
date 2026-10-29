package id.ac.stmi.vertue

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import java.util.Calendar

class CreateOrderActivity : BaseActivity() {
    private lateinit var rg: RadioGroup
    private var pickedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_order)
        setupHeader("Buat Pesanan")

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddr = findViewById<EditText>(R.id.etAddress)
        val etCar = findViewById<EditText>(R.id.etCar)
        val etPlate = findViewById<EditText>(R.id.etPlate)
        val etNote = findViewById<EditText>(R.id.etNote)
        val etDate = findViewById<EditText>(R.id.etDate)
        val sp = findViewById<Spinner>(R.id.spTime)

        etName.setText(Session.name(this))
        etPhone.setText(Session.phone(this))
        etAddr.setText(Session.address(this))
        sp.adapter = ArrayAdapter.createFromResource(this, R.array.jam_antar, android.R.layout.simple_spinner_dropdown_item)

        rg = findViewById(R.id.rgService)
        var firstId = View.NO_ID
        Repo.services.forEachIndexed { i, s ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = "${s.name}\n${s.price.rp()}"
                tag = s
                setButtonDrawable(null)
                setBackgroundResource(R.drawable.bg_option)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                setTextColor(getColor(R.color.ink))
                textSize = 13f
            }
            val lp = RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = dp(8)
            rg.addView(rb, lp)
            if (i == 0) firstId = rb.id
        }
        rg.setOnCheckedChangeListener { _, _ -> updateSummary() }
        rg.check(firstId)

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dlg = DatePickerDialog(this, { _, y, m, d ->
                val cal = Calendar.getInstance().apply { set(y, m, d) }
                pickedDate = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale("id", "ID")).format(cal.time)
                etDate.setText(pickedDate)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dlg.datePicker.minDate = System.currentTimeMillis() - 1000
            dlg.show()
        }

        findViewById<View>(R.id.btnNext).setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val addr = etAddr.text.toString().trim()
            val car = etCar.text.toString().trim()
            val plate = etPlate.text.toString().trim().uppercase()
            val s = selected()

            when {
                name.length < 3 -> { etName.error = "Isi nama lengkap"; etName.requestFocus() }
                !Regex("^(\\+62|62|0)8[0-9]{8,12}$").matches(phone) -> { etPhone.error = "Nomor telepon tidak valid"; etPhone.requestFocus() }
                addr.isEmpty() -> { etAddr.error = "Alamat wajib diisi"; etAddr.requestFocus() }
                car.length < 3 -> { etCar.error = "Isi merek & tipe mobil"; etCar.requestFocus() }
                !Regex("^[A-Z]{1,2}\\s?\\d{1,4}\\s?[A-Z]{0,3}$").matches(plate) -> { etPlate.error = "Plat tidak valid, contoh: B 1234 XYZ"; etPlate.requestFocus() }
                s == null -> toast("Pilih layanan terlebih dahulu")
                pickedDate.isEmpty() -> toast("Pilih tanggal antar mobil")
                !findViewById<CheckBox>(R.id.cbTerms).isChecked -> toast("Setujui syarat & ketentuan dulu")
                else -> {
                    val pickup = findViewById<CheckBox>(R.id.cbPickup).isChecked
                    val note = buildString {
                        append(if (pickup) "Jemput" else "Antar").append(" $pickedDate, ").append(sp.selectedItem)
                        val extra = etNote.text.toString().trim()
                        if (extra.isNotEmpty()) append(" · ").append(extra)
                    }
                    val o = Order(
                        id = Repo.nextId(),
                        title = s.name,
                        total = s.price,
                        created = Repo.today(),
                        note = note,
                        isPickup = pickup,
                        pickupAddress = if (pickup) addr else "",
                        carModel = car,
                        plate = plate
                    )
                    Repo.orders.add(0, o)
                    go(PaymentActivity::class.java, o.id)
                    finish()
                }
            }
        }
    }

    private fun selected(): Service? =
        rg.findViewById<RadioButton>(rg.checkedRadioButtonId)?.tag as? Service

    private fun updateSummary() {
        val s = selected() ?: return
        findViewById<TextView>(R.id.tvTotal).text = s.price.rp()
        findViewById<TextView>(R.id.tvDp).text = (s.price / 2).rp()
    }
}