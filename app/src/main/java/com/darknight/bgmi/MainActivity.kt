package com.darknight.bgmi

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import android.graphics.Color

class MainActivity : Activity() {
    private val dark = Color.rgb(8, 13, 24)
    private val cyan = Color.rgb(0, 213, 255)
    private lateinit var rootText: TextView
    private lateinit var fpsText: TextView
    private lateinit var report: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 32)
            setBackgroundColor(dark)
        }
        scroll.addView(page)
        page.addView(label("🦇 DarKnighT", 28f, cyan, true).apply { gravity = Gravity.CENTER })
        page.addView(label("BGMI 90 FPS ANALYZER", 17f, Color.WHITE, true))
        page.addView(label("Device diagnostics • safe optimization", 13f, 0xFF9FB0C8.toInt(), false))
        fpsText = label("Checking display capability…", 19f, Color.WHITE, true)
        panel(page, fpsText)
        rootText = label("Root access: checking…", 16f, Color.WHITE, true)
        panel(page, rootText)
        page.addView(Button(this).apply {
            text = "SCAN DEVICE"
            setOnClickListener { scan() }
        })
        page.addView(Button(this).apply {
            text = "OPEN DISPLAY SETTINGS"
            setOnClickListener {
                startActivity(android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS))
            }
        })
        report = label("", 14f, 0xFFD8E2F0.toInt(), false)
        panel(page, report)
        page.addView(label("SAFE BGMI SETTINGS", 17f, cyan, true))
        page.addView(label(
            "• Choose Smooth graphics and the highest FPS option BGMI itself offers.\n" +
            "• Close heavy background apps.\n" +
            "• Keep the tablet cool to reduce thermal throttling.\n" +
            "• Keep sufficient free storage and update BGMI officially.",
            14f, Color.WHITE, false
        ))
        page.addView(label("LIMITATION", 16f, 0xFFFFC66D.toInt(), true))
        page.addView(label(
            "A 90 Hz display does not guarantee 90 FPS in BGMI. This app does not modify game files, " +
            "bypass device whitelists, spoof device identity, or disable anti-cheat. Root is checked " +
            "with a read-only identity command only.",
            13f, 0xFFB9C6D8.toInt(), false
        ))
        setContentView(scroll)
        scan()
    }

    private fun scan() {
        val hz = try { windowManager.defaultDisplay.refreshRate } catch (_: Exception) { 0f }
        val maxHz = try {
            if (Build.VERSION.SDK_INT >= 23)
                windowManager.defaultDisplay.supportedModes.maxOfOrNull { it.refreshRate } ?: hz
            else hz
        } catch (_: Exception) { hz }

        val root = try {
            val p = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val out = p.inputStream.bufferedReader().readText()
            p.waitFor() == 0 && out.contains("uid=0")
        } catch (_: Exception) { false }

        rootText.text = if (root) "Root access: GRANTED" else "Root access: NOT AVAILABLE"
        val mem = android.app.ActivityManager.MemoryInfo()
        (getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager).getMemoryInfo(mem)
        val soc = try {
            Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", "ro.soc.model"))
                .inputStream.bufferedReader().readText().trim().ifBlank { Build.HARDWARE }
        } catch (_: Exception) { Build.HARDWARE }

        fpsText.text = "Maximum display mode: %.1f Hz".format(maxHz) +
            if (maxHz >= 89f) "\n90 FPS display refresh is possible; BGMI support is not confirmed."
            else "\nThis display does not report a 90 Hz mode."
        report.text = "DEVICE REPORT\n${Build.MANUFACTURER} ${Build.MODEL}\n" +
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
            "SoC: $soc\nCurrent refresh: %.1f Hz\n".format(hz) +
            "RAM: ${mem.totalMem / 1048576} MB total / ${mem.availMem / 1048576} MB available\n" +
            "BGMI FPS options must be checked inside the game."
    }

    private fun label(s: String, size: Float, color: Int, bold: Boolean) =
        TextView(this).apply {
            text = s; textSize = size; setTextColor(color)
            setPadding(4, 12, 4, 12)
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        }

    private fun panel(parent: LinearLayout, child: TextView) {
        parent.addView(LinearLayout(this).apply {
            setPadding(16, 12, 16, 12)
            setBackgroundColor(Color.rgb(18, 28, 45))
            addView(child)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 14 })
    }
}

