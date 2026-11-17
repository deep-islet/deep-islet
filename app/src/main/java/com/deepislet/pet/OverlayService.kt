package com.deepislet.pet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.FrameLayout
import kotlin.math.abs

class OverlayService : Service() {

    companion object {
        @Volatile
        var running = false
            private set

        private const val CH_ID = "islet_overlay"
        private const val NOTI_ID = 1001
    }

    private lateinit var wm: WindowManager
    private var root: FrameLayout? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        running = true
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundInternal()
        addOverlay()
    }

    private fun startForegroundInternal() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(CH_ID, "桌宠悬浮窗", NotificationManager.IMPORTANCE_LOW)
            )
            Notification.Builder(this, CH_ID)
        } else {
            Notification.Builder(this)
        }
        val noti = builder
            .setContentTitle("深屿")
            .setContentText("在屏幕上待着")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
        startForeground(NOTI_ID, noti)
    }

    private fun addOverlay() {
        val d = resources.displayMetrics.density
        val size = (230 * d).toInt()

        val container = FrameLayout(this)
        container.setBackgroundColor(Color.TRANSPARENT)

        val web = WebView(this)
        web.setBackgroundColor(Color.TRANSPARENT)
        web.settings.javaScriptEnabled = true
        web.settings.allowFileAccess = true
        web.settings.allowFileAccessFromFileURLs = true
        web.settings.allowUniversalAccessFromFileURLs = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        web.loadUrl("file:///android_asset/pet/index.html?w=1")
        container.addView(
            web,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        params = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (24 * d).toInt()
            y = (resources.displayMetrics.heightPixels * 0.55f).toInt()
        }

        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var dragging = false

        web.setOnTouchListener { v, ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = ev.rawX
                    downY = ev.rawY
                    startX = params.x
                    startY = params.y
                    dragging = false
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = ev.rawX - downX
                    val dy = ev.rawY - downY
                    if (!dragging && (abs(dx) > 14 || abs(dy) > 14)) dragging = true
                    if (dragging) {
                        params.x = startX + dx.toInt()
                        params.y = startY + dy.toInt()
                        try {
                            wm.updateViewLayout(container, params)
                        } catch (e: Exception) {
                            // 窗口已被移除，忽略
                        }
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        }

        root = container
        try {
            wm.addView(container, params)
        } catch (e: Exception) {
            root = null
        }
    }

    override fun onDestroy() {
        running = false
        root?.let {
            try {
                wm.removeView(it)
            } catch (e: Exception) {
                // 已移除
            }
        }
        root = null
        super.onDestroy()
    }
}