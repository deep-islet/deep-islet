package com.deepislet.pet

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val d = resources.displayMetrics.density
        val pad = (26 * d).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(Color.parseColor("#0E1620"))
        }

        val title = TextView(this).apply {
            text = "深屿"
            textSize = 26f
            setTextColor(Color.parseColor("#D8E8F6"))
            gravity = Gravity.CENTER
        }

        val hint = TextView(this).apply {
            text = "第一次要先去系统里给「显示在其他应用上层」权限\n回来再点一次，他就会浮在桌面上了"
            textSize = 13f
            setTextColor(Color.parseColor("#7C93A8"))
            gravity = Gravity.CENTER
            setPadding(0, pad / 2, 0, pad)
        }

        val btnOpen = Button(this).apply {
            text = if (OverlayService.running) "他还在这儿" else "让他出来"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                } else {
                    startService(Intent(this@MainActivity, OverlayService::class.java))
                    moveTaskToBack(true)
                }
            }
        }

        val btnClose = Button(this).apply {
            text = "收回去"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
            }
        }

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.topMargin = (10 * d).toInt()

        root.addView(title)
        root.addView(hint)
        root.addView(btnOpen, lp)
        root.addView(btnClose, lp)

        setContentView(root)
    }
}