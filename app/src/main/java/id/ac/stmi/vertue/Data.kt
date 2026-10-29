package id.ac.stmi.vertue

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val WORKSHOP_PHONE = "6281234567890"
const val ACCOUNT_NO = "[NO. REKENING]"

data class Service(val name: String, val price: Long)

data class Order(
    val id: String,
    val title: String,
    val total: Long,
    var dpPaid: Boolean,
    var step: Int,
    var done: Boolean,
    var paidFull: Boolean,
    val estimate: String,
    val created: String,
    val note: String = "",
    val info: List<String> = emptyList(),
    val driverName: String = Repo.getRandomDriver().name,
    val driverPhone: String = Repo.getRandomDriver().phone,
    val licensePlate: String = Repo.getRandomDriver().plate,
    val isPickup: Boolean = false
) {
    val dp: Long get() = total / 2
    val remaining: Long get() = when { paidFull -> 0L; dpPaid -> total - dp; else -> total }
    val progress: Int get() = if (done) 100 else step * 100 / 8
    val inProgress: Boolean get() = dpPaid && !done
    val payAmount: Long get() = if (!dpPaid) dp else remaining
    val statusLabel: String
        get() = when {
            !dpPaid -> "Menunggu pembayaran"
            done -> "Selesai"
            else -> Repo.shortSteps[step - 1]
        }
}

object Repo {
    val steps = listOf(
        "Pesanan dikonfirmasi", "Mobil diterima & inspeksi awal", "Pembongkaran jok", "Pembuatan pola",
        "Penjahitan", "Pemasangan", "Finishing & QC", "Siap diambil"
    )
    val shortSteps = listOf(
        "Dikonfirmasi", "Inspeksi", "Pembongkaran", "Pola", "Penjahitan", "Pemasangan", "Finishing", "Siap diambil"
    )
    val services = listOf(
        Service("Lapis Jok Kulit Full (Sedan)", 7_700_000),
        Service("Lapis Jok Kulit Full (SUV)", 9_450_000),
        Service("Setir Lapis Kulit", 950_000)
    )

    data class Driver(val name: String, val phone: String, val plate: String)
    val drivers = listOf(
        Driver("Budi Santoso", "6281234567891", "B 9988 XYZ"),
        Driver("Agus Pratama", "6281234567892", "B 1122 ABC"),
        Driver("Dedi Kurniawan", "6281234567893", "B 3344 DEF"),
        Driver("Rizky Ramadhan", "6281234567894", "B 5566 GHI"),
        Driver("Slamet Riyadi", "6281234567895", "B 7788 JKL")
    )
    fun getRandomDriver(): Driver = drivers.random()

    val orders = mutableListOf(
        Order(
            "INV-4021", "Lapis Jok Kulit Full (Mobil Sedan)", 7_700_000, true, 5, false, false,
            "20 Okt 2026", "10 Okt 2026", "Kulit Wollsdorf · warna hitam · Plat B 1234 XYZ",
            listOf(
                "10 Okt 2026, 09.15", "12 Okt 2026, 10.40", "13 Okt 2026, 14.00", "14 Okt 2026, 11.30",
                "Dimulai 15 Okt 2026 · Teknisi: Eric", "Estimasi 18 Okt 2026", "Estimasi 19 Okt 2026", "Estimasi 20 Okt 2026"
            ),
            "Budi Santoso", "6281234567891", "B 9988 XYZ", true
        ),
        Order("INV-4024", "Setir Lapis Kulit", 950_000, false, 1, false, false, "menyusul", "14 Okt 2026"),
        Order("INV-3998", "Karpet Mobil Custom (set)", 1_850_000, true, 8, true, true, "02 Okt 2026", "20 Sep 2026")
    )
    private var counter = 4025
    fun nextId() = "INV-${counter++}"
    fun find(id: String?) = orders.firstOrNull { it.id == id }
    fun active() = orders.firstOrNull { it.inProgress }
    fun today(): String = SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date())
}

object Session {
    private fun p(c: Context) = c.getSharedPreferences("vertue", Context.MODE_PRIVATE)
    fun isLoggedIn(c: Context) = p(c).getBoolean("in", false)
    fun login(c: Context, phone: String) = p(c).edit().putBoolean("in", true).putString("phone", phone).apply()
    fun logout(c: Context) = p(c).edit().putBoolean("in", false).apply()
    fun phone(c: Context): String = p(c).getString("phone", "") ?: ""
    fun name(c: Context): String = p(c).getString("name", "Arief Muhammad") ?: "Arief Muhammad"
    fun address(c: Context): String = p(c).getString("address", "Pondok Indah, Jakarta Selatan") ?: ""
    fun notifOn(c: Context) = p(c).getBoolean("notif", true)
    fun setNotif(c: Context, v: Boolean) = p(c).edit().putBoolean("notif", v).apply()
}

fun Long.rp(): String = "Rp" + "%,d".format(Locale("id", "ID"), this)

private fun spell(n: Long): String {
    val s = arrayOf("", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas")
    return when {
        n < 12 -> s[n.toInt()]
        n < 20 -> spell(n - 10) + " belas"
        n < 100 -> spell(n / 10) + " puluh " + spell(n % 10)
        n < 200 -> "seratus " + spell(n - 100)
        n < 1000 -> spell(n / 100) + " ratus " + spell(n % 100)
        n < 2000 -> "seribu " + spell(n - 1000)
        n < 1_000_000 -> spell(n / 1000) + " ribu " + spell(n % 1000)
        n < 1_000_000_000 -> spell(n / 1_000_000) + " juta " + spell(n % 1_000_000)
        else -> n.toString()
    }.trim().replace(Regex("\\s+"), " ")
}

fun terbilangRupiah(n: Long): String {
    if (n == 0L) return "Nol Rupiah"
    return spell(n).split(" ").joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } } + " Rupiah"
}