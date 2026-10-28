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
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private var wm: WindowManager? = null
    private var bubbleView: View? = null
    private var menuView: View? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var menuParams: WindowManager.LayoutParams? = null
    private var menuOpen = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (bubbleView == null) showBubble()
        if (intent?.getBooleanExtra(EXTRA_OPEN_MENU, false) == true) {
            openMenu()
        }
        return START_STICKY
    }

    private fun typeOverlay(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
    }

    private fun showBubble() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val v = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)
        bubbleView = v
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            typeOverlay(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 24
        params.y = 220
        bubbleParams = params

        var dragX = 0
        var dragY = 0
        var moved = false
        v.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragX = e.rawX.toInt()
                    dragY = e.rawY.toInt()
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val nx = e.rawX.toInt()
                    val ny = e.rawY.toInt()
                    if (kotlin.math.abs(nx - dragX) > 6 || kotlin.math.abs(ny - dragY) > 6) {
                        moved = true
                        params.x += nx - dragX
                        params.y += ny - dragY
                        dragX = nx
                        dragY = ny
                        wm?.updateViewLayout(v, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        if (menuOpen) closeMenu() else openMenu()
                    }
                    true
                }
                else -> false
            }
        }
        wm?.addView(v, params)
    }

    private fun openMenu() {
        if (menuOpen) return
        val v = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null)
        menuView = v
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            typeOverlay(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        val bp = bubbleParams
        params.x = (bp?.x ?: 24) + 60
        params.y = bp?.y ?: 220
        menuParams = params

        val friends = FriendStore.list(this)
        v.findViewById<TextView>(R.id.overlayFriends).text =
            if (friends.isEmpty()) "(empty — add in app)" else friends.joinToString(", ")

        fun clip(cmd: String) {
            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("cmd", cmd))
            Toast.makeText(this, "Copied: $cmd", Toast.LENGTH_SHORT).show()
        }

        v.findViewById<TextView>(R.id.btnSoft).setOnClickListener { clip(".aim soft") }
        v.findViewById<TextView>(R.id.btnSnap).setOnClickListener { clip(".aim snap") }
        v.findViewById<TextView>(R.id.btnOff).setOnClickListener { clip(".aim off") }
        v.findViewById<TextView>(R.id.btnMin).setOnClickListener { closeMenu() }
        v.findViewById<TextView>(R.id.btnClose).setOnClickListener {
            closeMenu()
            stopSelf()
        }

        wm?.addView(v, params)
        menuOpen = true
    }

    private fun closeMenu() {
        menuView?.let { runCatching { wm?.removeView(it) } }
        menuView = null
        menuOpen = false
    }

    override fun onDestroy() {
        closeMenu()
        bubbleView?.let { runCatching { wm?.removeView(it) } }
        bubbleView = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_OPEN_MENU = "open_menu"
    }
}
