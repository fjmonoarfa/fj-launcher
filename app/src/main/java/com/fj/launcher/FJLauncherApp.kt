package com.fj.launcher

import android.app.Application
import com.fj.launcher.data.PreferenceManager
import com.fj.launcher.lockscreen.LockscreenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FJLauncherApp : Application() {

    lateinit var lockscreenManager: LockscreenManager
        private set

    override fun onCreate() {
        super.onCreate()
        lockscreenManager = LockscreenManager(this)

        val preferenceManager = PreferenceManager(this)
        CoroutineScope(Dispatchers.Main).launch {
            preferenceManager.preferencesFlow.collect { prefs ->
                if (prefs.matrixLockscreenEnabled) {
                    lockscreenManager.start()
                } else {
                    lockscreenManager.stop()
                }
            }
        }
    }
}
