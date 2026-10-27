package com.example.tvoscards

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.JavascriptInterface
import org.json.JSONObject

/** Methods callable from the web UI as window.Native.*  (they run on a WebView background thread). */
class Bridge(private val svc: KeyService) {
    private val ctx: Context get() = svc.applicationContext
    private val ui = Handler(Looper.getMainLooper())

    @JavascriptInterface fun stats(): String = SysInfo.stats(ctx)
    @JavascriptInterface fun apps(): String = SysInfo.running(ctx).toString()
    @JavascriptInterface fun clearAll(): Int = SysInfo.killAll(ctx)
    @JavascriptInterface fun kill(pkg: String) { SysInfo.kill(ctx, pkg) }
    @JavascriptInterface fun net(): String = SysInfo.net(ctx)
    @JavascriptInterface fun bt(): Boolean = SysInfo.bt()

    @JavascriptInterface fun hide() { svc.setMode("hidden") }
    @JavascriptInterface fun flash(ms: Int) { svc.flash(ms) }

    @JavascriptInterface fun getKey(): String {
        val c = Prefs.keyCode(ctx)
        return JSONObject()
            .put("code", c)
            .put("name", Prefs.keyName(c))
            .put("longMs", Prefs.longMs(ctx))
            .put("consume", Prefs.consume(ctx))
            .toString()
    }
    @JavascriptInterface fun learnKey() { svc.startLearn() }
    @JavascriptInterface fun setLongMs(ms: Int) { Prefs.setLongMs(ctx, ms) }
    @JavascriptInterface fun setConsume(b: Boolean) { Prefs.setConsume(ctx, b) }

    @JavascriptInterface fun openSettings(kind: String) {
        ui.post {
            try {
                val action = when (kind) {
                    "wifi" -> Settings.ACTION_WIFI_SETTINGS
                    "bt" -> Settings.ACTION_BLUETOOTH_SETTINGS
                    else -> Settings.ACTION_SETTINGS
                }
                svc.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) { }
            svc.setMode("hidden")
        }
    }

    @JavascriptInterface fun assistant() {
        ui.post {
            svc.setMode("hidden")
            try {
                svc.startActivity(Intent(Intent.ACTION_VOICE_COMMAND).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) {
                try {
                    svc.startActivity(Intent(Intent.ACTION_ASSIST).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e2: Exception) { }
            }
        }
    }

    @JavascriptInterface fun pickSound() {
        ui.post {
            svc.setMode("hidden")
            try {
                svc.startActivity(Intent(svc, SoundPickActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) { }
        }
    }
    @JavascriptInterface fun soundName(): String = Prefs.soundName(ctx)
    @JavascriptInterface fun playCustom() {
        ui.post {
            try {
                val u = Prefs.soundUri(ctx)
                if (u != null) {
                    val mp = MediaPlayer()
                    mp.setDataSource(ctx, Uri.parse(u))
                    mp.setOnCompletionListener { it.release() }
                    mp.prepare()
                    mp.start()
                }
            } catch (e: Exception) { }
        }
    }
}
