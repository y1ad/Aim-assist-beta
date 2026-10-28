package com.eyal.aimproxy

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private var wm: WindowManager? = null
    private var view: android.view.View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val v = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null)
        view = v

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 40
        params.y = 200

        val friends = FriendStore.list(this)
        v.findViewById<TextView>(R.id.overlayFriends).text =
            if (friends.isEmpty()) "(vacío — añade en la app)" else friends.joinToString(", ")

        fun clipCmd(cmd: String) {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("cmd", cmd))
            Toast.makeText(this, "Copiado: $cmd — pégalo en el chat", Toast.LENGTH_SHORT).show()
        }

        v.findViewById<Button>(R.id.btnSoft).setOnClickListener { clipCmd(".aim soft") }
        v.findViewById<Button>(R.id.btnSnap).setOnClickListener { clipCmd(".aim snap") }
        v.findViewById<Button>(R.id.btnOff).setOnClickListener { clipCmd(".aim off") }
        v.findViewById<Button>(R.id.btnClose).setOnClickListener {
            stopSelf()
        }

        var dragX = 0
        var dragY = 0
        v.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragX = e.rawX.toInt(); dragY = e.rawY.toInt()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val nx = e.rawX.toInt(); val ny = e.rawY.toInt()
                    params.x += nx - dragX
                    params.y += ny - dragY
                    dragX = nx; dragY = ny
                    wm?.updateViewLayout(v, params)
                    true
                }
                else -> false
            }
        }

        wm?.addView(v, params)
    }

    override fun onDestroy() {
        view?.let { wm?.removeView(it) }
        view = null
        super.onDestroy()
    }
}
