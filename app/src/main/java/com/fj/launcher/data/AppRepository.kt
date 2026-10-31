package com.fj.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.fj.launcher.model.AppItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class AppRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val packageManager: PackageManager = context.packageManager

    fun getInstalledAppsFlow(): Flow<List<AppItem>> = callbackFlow {
        fun reload() {
            scope.launch(Dispatchers.IO) {
                val list = queryAllInstalledApps()
                trySend(list)
            }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                reload()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        context.registerReceiver(receiver, filter)
        reload()

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    fun queryAllInstalledApps(): List<AppItem> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        val myPackage = context.packageName

        return resolveInfos
            .filter { it.activityInfo.packageName != myPackage }
            .map { info ->
                val label = runCatching {
                    info.loadLabel(packageManager).toString()
                }.getOrDefault(info.activityInfo.name)

                AppItem(
                    packageName = info.activityInfo.packageName,
                    activityName = info.activityInfo.name,
                    label = label
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun launchApp(app: AppItem) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "Cannot launch ${app.displayLabel}", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchAppByPackageName(packageName: String) {
        if (packageName.isBlank() || packageName.equals("None", ignoreCase = true)) return
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "Cannot launch app ($packageName)", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(app: AppItem) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(app: AppItem) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
