package id.ac.stmi.vertue

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

const val WORKSHOP_PHONE = "6281234567890"
const val BANK_NAME = "BCA"
const val ACCOUNT_NO = "8820 1234 5678"
const val ACCOUNT_HOLDER = "EDI"
const val PAY_DEADLINE_HOURS = 24L

private val ID_LOCALE = Locale("id", "ID")

data class Service(val name: String, val price: Long)
data class Driver(val name: String, val phone: String, val plate: String)

enum class PaymentStatus { UNPAID, VERIFYING, CONFIRMED, REJECTED }

enum class OrderStatus(val label: String) {
    WAITING_DP("Menunggu pembayaran DP"),
    VERIFYING_DP("Menunggu verifikasi DP"),
    DP_REJECTED("Bukti DP ditolak"),
    IN_PROGRESS("Dalam pengerjaan"),
    READY("Siap diambil"),
    VERIFYING_FINAL("Menunggu verifikasi pelunasan"),
    FINAL_REJECTED("Bukti pelunasan ditolak"),
    DELIVERING("Diantar kembali"),
    COMPLETED("Selesai"),
    CANCELLED("Dibatalkan")
}

data class Order(
    val id: String,
    val title: String,
    val total: Long,
    val created: String,
    var dpStatus: PaymentStatus = PaymentStatus.UNPAID,
    var finalStatus: PaymentStatus = PaymentStatus.UNPAID,
    var step: Int = 1,
    var cancelled: Boolean = false,
    var delivered: Boolean = false,
    var estimate: String = "menyusul",
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = "",
    val info: List<String> = emptyList(),
    val carModel: String = "",
    val plate: String = "",
    val isPickup: Boolean = false,
    val pickupAddress: String = "",
    var driver: Driver? = null,
    var dpProof: String? = null,
    var finalProof: String? = null
) {
    val dp: Long get() = total / 2
    val dpPaid: Boolean get() = dpStatus == PaymentStatus.CONFIRMED
    val paidFull: Boolean get() = finalStatus == PaymentStatus.CONFIRMED
    val done: Boolean get() = dpPaid && step >= 8
    val remaining: Long get() = when { paidFull -> 0L; dpPaid -> total - dp; else -> total }
    val payAmount: Long get() = if (!dpPaid) dp else remaining

    val status: OrderStatus
        get() = when {
            cancelled -> OrderStatus.CANCELLED
            dpStatus == PaymentStatus.UNPAID -> OrderStatus.WAITING_DP
            dpStatus == PaymentStatus.VERIFYING -> OrderStatus.VERIFYING_DP
            dpStatus == PaymentStatus.REJECTED -> OrderStatus.DP_REJECTED
            paidFull -> if (isPickup && !delivered) OrderStatus.DELIVERING else OrderStatus.COMPLETED
            !done -> OrderStatus.IN_PROGRESS
            finalStatus == PaymentStatus.VERIFYING -> OrderStatus.VERIFYING_FINAL
            finalStatus == PaymentStatus.REJECTED -> OrderStatus.FINAL_REJECTED
            else -> OrderStatus.READY
        }

    val inProgress: Boolean get() = status == OrderStatus.IN_PROGRESS
    val canPay: Boolean get() = status in listOf(OrderStatus.WAITING_DP, OrderStatus.DP_REJECTED, OrderStatus.READY, OrderStatus.FINAL_REJECTED)
    val canCancel: Boolean get() = status == OrderStatus.WAITING_DP || status == OrderStatus.DP_REJECTED
    val progress: Int get() = when { !dpPaid -> 0; done -> 100; else -> step * 100 / 8 }
    val statusLabel: String get() = if (status == OrderStatus.IN_PROGRESS) Repo.shortSteps[step - 1] else status.label

    val dpDeadline: String
        get() = SimpleDateFormat("d MMM yyyy, HH.mm", ID_LOCALE).format(Date(createdAt + PAY_DEADLINE_HOURS * 3_600_000L))

    fun invoiceNo(final: Boolean): String = (if (final) "INV-LN-" else "INV-DP-") + id.removePrefix("ORD-")
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

    val drivers = listOf(
        Driver("Budi Santoso", "6281234567891", "B 9988 XYZ"),
        Driver("Agus Pratama", "6281234567892", "B 1122 ABC"),
        Driver("Dedi Kurniawan", "6281234567893", "B 3344 DEF"),
        Driver("Rizky Ramadhan", "6281234567894", "B 5566 GHI"),
        Driver("Slamet Riyadi", "6281234567895", "B 7788 JKL")
    )

    val orders = mutableListOf(
        Order(
            id = "ORD-4021", title = "Lapis Jok Kulit Full (Mobil Sedan)", total = 7_700_000, created = "10 Okt 2026",
            dpStatus = PaymentStatus.CONFIRMED, step = 5, estimate = "20 Okt 2026",
            note = "Kulit Wollsdorf · warna hitam · Plat B 1234 XYZ",
            info = listOf(
                "10 Okt 2026, 09.15", "12 Okt 2026, 10.40", "13 Okt 2026, 14.00", "14 Okt 2026, 11.30",
                "Dimulai 15 Okt 2026 · Teknisi: Eric", "Estimasi 18 Okt 2026", "Estimasi 19 Okt 2026", "Estimasi 20 Okt 2026"
            ),
            plate = "B 1234 XYZ", isPickup = true, pickupAddress = "Pondok Indah, Jakarta Selatan", driver = drivers[0]
        ),
        Order(id = "ORD-4024", title = "Setir Lapis Kulit", total = 950_000, created = "14 Okt 2026"),
        Order(
            id = "ORD-3998", title = "Karpet Mobil Custom (set)", total = 1_850_000, created = "20 Sep 2026",
            dpStatus = PaymentStatus.CONFIRMED, finalStatus = PaymentStatus.CONFIRMED, step = 8, estimate = "02 Okt 2026"
        )
    )

    private var counter = 4025
    fun nextId() = "ORD-${counter++}"
    fun find(id: String?) = orders.firstOrNull { it.id == id }

    fun active() = orders.firstOrNull {
        it.status in listOf(OrderStatus.IN_PROGRESS, OrderStatus.READY, OrderStatus.VERIFYING_FINAL, OrderStatus.FINAL_REJECTED, OrderStatus.DELIVERING)
    }

    fun today(): String = SimpleDateFormat("d MMM yyyy", ID_LOCALE).format(Date())

    fun expireOverdue() {
        val now = System.currentTimeMillis()
        orders.filter { it.dpStatus == PaymentStatus.UNPAID && !it.cancelled && now > it.createdAt + PAY_DEADLINE_HOURS * 3_600_000L }.forEach { it.cancelled = true }
    }

    fun confirmPayment(o: Order, final: Boolean, ok: Boolean) {
        if (final) {
            o.finalStatus = if (ok) PaymentStatus.CONFIRMED else PaymentStatus.REJECTED
        } else {
            o.dpStatus = if (ok) PaymentStatus.CONFIRMED else PaymentStatus.REJECTED
            if (ok) {
                o.step = 1
                if (o.isPickup && o.driver == null) o.driver = drivers.random()
                o.estimate = daysFromNow(14)
            }
        }
    }

    fun advance(o: Order) {
        if (o.dpPaid && o.step < 8) o.step++
    }

    private fun daysFromNow(n: Int): String =
        SimpleDateFormat("d MMM yyyy", ID_LOCALE).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, n) }.time)
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