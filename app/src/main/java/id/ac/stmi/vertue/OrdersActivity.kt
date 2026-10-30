package id.ac.stmi.vertue

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class OrdersActivity : BaseActivity() {
    private lateinit var listView: ListView
    private val adapter = OrderAdapter()
    private var filter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)
        setupHeader("Pesanan Saya")
        setupBottomNav(R.id.nav_orders)
        listView = findViewById(R.id.listOrders)
        listView.adapter = adapter
        listView.emptyView = findViewById(R.id.tvEmpty)
        registerForContextMenu(listView)
        listView.setOnItemClickListener { _, _, pos, _ ->
            val o = adapter.getItem(pos)
            if (o.canPay) go(PaymentActivity::class.java, o.id) else go(TrackingActivity::class.java, o.id)
        }
        findViewById<RadioGroup>(R.id.filterGroup).setOnCheckedChangeListener { _, id ->
            filter = when (id) { R.id.chipRun -> 1; R.id.chipDone -> 2; else -> 0 }
            refresh()
        }
    }

    override fun onResume() {
        super.onResume();
        refresh()
    }

    private fun refresh() {
        adapter.items = when (filter) {
            1 -> Repo.orders.filter { it.status != OrderStatus.COMPLETED && it.status != OrderStatus.CANCELLED }
            2 -> Repo.orders.filter { it.status == OrderStatus.COMPLETED || it.status == OrderStatus.CANCELLED }
            else -> Repo.orders.toList()
        }
        adapter.notifyDataSetChanged()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, info: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, info)
        val pos = (info as AdapterView.AdapterContextMenuInfo).position
        val o = adapter.getItem(pos)
        menu.setHeaderTitle("#${o.id}")
        menuInflater.inflate(R.menu.menu_context_order, menu)
        menu.findItem(R.id.ctx_cancel).isVisible = o.canCancel
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val pos = (item.menuInfo as AdapterView.AdapterContextMenuInfo).position
        val o = adapter.getItem(pos)
        when (item.itemId) {
            R.id.ctx_copy -> {
                (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                    .setPrimaryClip(ClipData.newPlainText("order", o.id))
                toast("Nomor pesanan disalin")
            }
            R.id.ctx_share -> startActivity(Intent.createChooser(
                Intent(Intent.ACTION_SEND).setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "Pesanan Vertue #${o.id}\n${o.title}\nStatus: ${o.statusLabel}\nTotal: ${o.total.rp()}"),
                "Bagikan ringkasan"))
            R.id.ctx_cancel -> confirmCancel(o) { refresh() }
        }
        return true
    }

    inner class OrderAdapter : BaseAdapter() {
        var items: List<Order> = emptyList()
        override fun getCount() = items.size
        override fun getItem(position: Int): Order = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: LayoutInflater.from(this@OrdersActivity).inflate(R.layout.item_order, parent, false)
            val o = getItem(position)
            v.findViewById<TextView>(R.id.tvId).text = "#${o.id}"
            val chip = v.findViewById<TextView>(R.id.tvStatus)
            chip.text = o.statusLabel
            when (o.status) {
                OrderStatus.CANCELLED, OrderStatus.DP_REJECTED, OrderStatus.FINAL_REJECTED -> { chip.setBackgroundResource(R.drawable.bg_pill_red); chip.setTextColor(getColor(R.color.red)) }
                OrderStatus.COMPLETED -> { chip.setBackgroundResource(R.drawable.bg_pill_green); chip.setTextColor(getColor(R.color.green)) }
                OrderStatus.WAITING_DP, OrderStatus.VERIFYING_DP -> { chip.setBackgroundResource(R.drawable.bg_pill_red); chip.setTextColor(getColor(R.color.red)) }
                else -> { chip.setBackgroundResource(R.drawable.bg_pill_gold); chip.setTextColor(getColor(R.color.gold)) }
            }
            v.findViewById<TextView>(R.id.tvTitle).text = o.title
            v.findViewById<TextView>(R.id.tvSub).text = when (o.status) {
                OrderStatus.COMPLETED -> "Selesai ${o.estimate}"
                OrderStatus.WAITING_DP, OrderStatus.VERIFYING_DP -> "Dipesan ${o.created}"
                else -> "Estimasi selesai ${o.estimate}"
            }
            val pb = v.findViewById<ProgressBar>(R.id.pb)
            val step = v.findViewById<TextView>(R.id.tvStep)
            pb.visibility = if (o.inProgress) View.VISIBLE else View.GONE
            step.visibility = pb.visibility
            pb.progress = o.progress
            step.text = "Tahap ${o.step} dari 8"

            val pay = v.findViewById<Button>(R.id.btnPay)
            pay.visibility = if (o.canPay) View.VISIBLE else View.GONE
            pay.text = if (!o.dpPaid) "Bayar DP ${o.dp.rp()}" else "Bayar Pelunasan ${o.remaining.rp()}"
            pay.setOnClickListener { go(PaymentActivity::class.java, o.id) }

            val inv = v.findViewById<TextView>(R.id.tvInvoice)
            inv.visibility = if (o.status == OrderStatus.COMPLETED) View.VISIBLE else View.GONE
            inv.setOnClickListener { go(InvoiceActivity::class.java, o.id) }

            v.findViewById<ImageButton>(R.id.btnMore).setOnClickListener { anchor ->
                PopupMenu(this@OrdersActivity, anchor).apply {
                    menuInflater.inflate(R.menu.menu_popup_order, menu)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.pop_track -> go(TrackingActivity::class.java, o.id)
                            R.id.pop_invoice -> go(InvoiceActivity::class.java, o.id)
                            else -> showHelp()
                        }
                        true
                    }
                }.show()
            }
            return v
        }
    }
}