package com.nova.app.ui.history
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.nova.app.R
import com.nova.app.core.NovaApplication
import com.nova.app.databinding.ActivityHistoryBinding
import com.nova.app.db.CommandHistoryEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = "Command History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val adapter = HistoryAdapter()
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter
        lifecycleScope.launch {
            val h = NovaApplication.instance.database.commandHistoryDao().getRecent()
            if (h.isEmpty()) { binding.tvEmpty.visibility = View.VISIBLE } else adapter.setItems(h)
        }
        binding.btnClearHistory.setOnClickListener {
            lifecycleScope.launch { NovaApplication.instance.database.commandHistoryDao().deleteAll() }
        }
    }
    override fun onSupportNavigateUp(): Boolean { onBackPressedDispatcher.onBackPressed(); return true }
}

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {
    private val items = mutableListOf<CommandHistoryEntity>()
    private val fmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    fun setItems(list: List<CommandHistoryEntity>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: CommandHistoryEntity) {
            v.findViewById<TextView>(R.id.tvCommand).text = item.commandText
            v.findViewById<TextView>(R.id.tvResponse).text = item.response
            v.findViewById<TextView>(R.id.tvTime).text = fmt.format(Date(item.timestamp))
        }
        private val v = itemView
    }
}