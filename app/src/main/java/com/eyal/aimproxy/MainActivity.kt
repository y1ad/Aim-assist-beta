package com.eyal.aimproxy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.eyal.aimproxy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val host = "fi11.bot-hosting.cloud"
    private val port = "26283"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        refreshFriends()

        binding.btnCopyHost.setOnClickListener { copy(host) }
        binding.btnCopyPort.setOnClickListener { copy(port) }
        binding.btnAddFriend.setOnClickListener {
            val n = binding.inputFriend.text?.toString().orEmpty()
            FriendStore.add(this, n)
            binding.inputFriend.setText("")
            refreshFriends()
            Toast.makeText(this, "Añadido. En juego: .friend add $n", Toast.LENGTH_SHORT).show()
        }
        binding.btnClearFriends.setOnClickListener {
            FriendStore.clear(this)
            refreshFriends()
        }
        binding.btnOverlay.setOnClickListener { openOverlay() }
    }

    private fun refreshFriends() {
        val l = FriendStore.list(this)
        binding.txtFriends.text = if (l.isEmpty()) "(sin amigos)" else l.joinToString("\n")
    }

    private fun copy(v: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ap", v))
        Toast.makeText(this, "Copiado: $v", Toast.LENGTH_SHORT).show()
    }

    private fun openOverlay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Activa permiso de superposición y pulsa de nuevo", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Menú flotante activo", Toast.LENGTH_SHORT).show()
    }
}
