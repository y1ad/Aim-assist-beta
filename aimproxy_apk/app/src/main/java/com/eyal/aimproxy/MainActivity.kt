package com.eyal.aimproxy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eyal.aimproxy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: FriendAdapter
    private val host = "fi11.bot-hosting.cloud"
    private val port = "26283"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.txtHost.text = host
        binding.txtPort.text = port

        adapter = FriendAdapter { name ->
            val list = FriendStore.list(this).filter { !it.equals(name, true) }
            FriendStore.save(this, list)
            refreshFriends()
            Toast.makeText(this, "Removed $name — in game: .friend remove $name", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerFriends.layoutManager = LinearLayoutManager(this)
        binding.recyclerFriends.adapter = adapter
        refreshFriends()

        binding.navHome.setOnClickListener { showPage(0) }
        binding.navList.setOnClickListener { showPage(1) }
        binding.navMisc.setOnClickListener { showPage(2) }

        binding.btnCopyHost.setOnClickListener { copy(host) }
        binding.btnCopyPort.setOnClickListener { copy(port) }
        binding.btnOverlay.setOnClickListener { openOverlay() }

        binding.btnAddFriend.setOnClickListener {
            val n = binding.inputFriend.text?.toString().orEmpty().trim()
            if (n.isEmpty()) return@setOnClickListener
            val cur = FriendStore.list(this)
            if (cur.size >= 100) {
                Toast.makeText(this, "Max 100", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FriendStore.add(this, n)
            binding.inputFriend.setText("")
            refreshFriends()
            copy(".friend add $n")
            Toast.makeText(this, "Added. Command copied for chat.", Toast.LENGTH_SHORT).show()
        }
        binding.btnClearFriends.setOnClickListener {
            FriendStore.clear(this)
            refreshFriends()
        }

        // Misc toggles → copy chat commands for proxy
        binding.swSoft.setOnCheckedChangeListener { _, on ->
            if (on) {
                binding.swSnap.isChecked = false
                binding.swAimOn.isChecked = true
                copy(".aim soft")
            }
        }
        binding.swSnap.setOnCheckedChangeListener { _, on ->
            if (on) {
                binding.swSoft.isChecked = false
                binding.swAimOn.isChecked = true
                copy(".aim snap")
            }
        }
        binding.swAimOn.setOnCheckedChangeListener { _, on ->
            if (on) {
                if (!binding.swSoft.isChecked && !binding.swSnap.isChecked) {
                    binding.swSoft.isChecked = true
                } else {
                    copy(".aim on")
                }
            } else {
                binding.swSoft.isChecked = false
                binding.swSnap.isChecked = false
                copy(".aim off")
            }
        }
        binding.btnMiscOverlay.setOnClickListener { openOverlay() }
        binding.btnStatus.setOnClickListener { copy(".aim status") }

        showPage(0)
    }

    private fun showPage(i: Int) {
        binding.pageHome.visibility = if (i == 0) View.VISIBLE else View.GONE
        binding.pageList.visibility = if (i == 1) View.VISIBLE else View.GONE
        binding.pageMisc.visibility = if (i == 2) View.VISIBLE else View.GONE
        val sel = ContextCompat.getColor(this, R.color.sidebar_sel)
        val clear = 0x00000000
        val on = ContextCompat.getColor(this, R.color.text)
        val off = ContextCompat.getColor(this, R.color.muted)
        binding.navHome.setBackgroundColor(if (i == 0) sel else clear)
        binding.navList.setBackgroundColor(if (i == 1) sel else clear)
        binding.navMisc.setBackgroundColor(if (i == 2) sel else clear)
        binding.navHome.setTextColor(if (i == 0) on else off)
        binding.navList.setTextColor(if (i == 1) on else off)
        binding.navMisc.setTextColor(if (i == 2) on else off)
    }

    private fun refreshFriends() {
        val list = FriendStore.list(this)
        adapter.submit(list)
        binding.txtCount.text = "Added Users (${list.size}/100)"
    }

    private fun copy(v: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ap", v))
        Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
    }

    private fun openOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Enable overlay permission", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Floating menu on", Toast.LENGTH_SHORT).show()
    }
}

class FriendAdapter(private val onRemove: (String) -> Unit) :
    RecyclerView.Adapter<FriendAdapter.VH>() {
    private val data = mutableListOf<String>()

    fun submit(list: List<String>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.txtName)
        val remove: TextView = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = data[position]
        holder.name.text = n
        holder.remove.setOnClickListener { onRemove(n) }
    }

    override fun getItemCount() = data.size
}
