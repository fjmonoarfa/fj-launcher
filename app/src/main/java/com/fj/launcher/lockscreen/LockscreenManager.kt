package com.fj.launcher.lockscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.fj.launcher.data.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockscreenManager(private val context: Context) {

    private val preferenceManager = PreferenceManager(context)
    private var screenOffReceiver: BroadcastReceiver? = null
    private var isRegistered = false

    fun start() {
        if (isRegistered) return

        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val prefs = preferenceManager.preferencesFlow.first()
                        if (prefs.matrixLockscreenEnabled) {
                            launchLockscreen(context)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.registerReceiver(screenOffReceiver, filter)
        isRegistered = true
        Log.d("LockscreenManager", "LockscreenManager started")
    }

    fun stop() {
        if (!isRegistered) return
        screenOffReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        screenOffReceiver = null
        isRegistered = false
        Log.d("LockscreenManager", "LockscreenManager stopped")
    }

    companion object {
        fun launchLockscreen(context: Context) {
            val intent = Intent(context, LockscreenMatrixActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                )
            }
            context.startActivity(intent)
        }
    }
}
