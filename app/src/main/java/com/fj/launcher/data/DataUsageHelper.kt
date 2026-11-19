package com.fj.launcher.data

import android.app.AppOpsManager
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar
import java.util.Locale

object DataUsageHelper {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getDataUsageSummary(context: Context): String {
        // Try NetworkStatsManager if permission is granted
        if (hasUsageStatsPermission(context)) {
            val stats = getNetworkStatsUsage(context)
            if (stats != null) {
                return stats
            }
        }

        // Fallback to TrafficStats (device boot usage)
        return getTrafficStatsFallback()
    }

    @Suppress("DEPRECATION")
    private fun getNetworkStatsUsage(context: Context): String? {
        val nsm = context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager ?: return null
        val now = System.currentTimeMillis()

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startToday = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startMonth = cal.timeInMillis

        return try {
            // Mobile data usage (today & this month)
            val bucketToday = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startToday, now)
            val todayBytes = (bucketToday?.rxBytes ?: 0L) + (bucketToday?.txBytes ?: 0L)

            val bucketMonth = nsm.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, startMonth, now)
            val monthBytes = (bucketMonth?.rxBytes ?: 0L) + (bucketMonth?.txBytes ?: 0L)

            if (todayBytes > 0 || monthBytes > 0) {
                if (monthBytes > todayBytes && monthBytes > 0) {
                    "Data: ${formatBytes(todayBytes)} • Bln: ${formatBytes(monthBytes)}"
                } else {
                    "Data: ${formatBytes(todayBytes)}"
                }
            } else {
                // If mobile data is 0, check Wi-Fi as fallback for Wi-Fi only devices
                val wifiTodayBucket = nsm.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, null, startToday, now)
                val wifiToday = (wifiTodayBucket?.rxBytes ?: 0L) + (wifiTodayBucket?.txBytes ?: 0L)
                if (wifiToday > 0) {
                    "WiFi: ${formatBytes(wifiToday)}"
                } else {
                    "Data: 0 MB"
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun getTrafficStatsFallback(): String {
        val mobileRx = TrafficStats.getMobileRxBytes()
        val mobileTx = TrafficStats.getMobileTxBytes()
        val mobileTotal = if (mobileRx != TrafficStats.UNSUPPORTED.toLong() && mobileTx != TrafficStats.UNSUPPORTED.toLong()) {
            mobileRx.coerceAtLeast(0L) + mobileTx.coerceAtLeast(0L)
        } else {
            0L
        }

        if (mobileTotal > 0) {
            return "Data: ${formatBytes(mobileTotal)}"
        }

        val totalRx = TrafficStats.getTotalRxBytes()
        val totalTx = TrafficStats.getTotalTxBytes()
        val total = if (totalRx != TrafficStats.UNSUPPORTED.toLong() && totalTx != TrafficStats.UNSUPPORTED.toLong()) {
            totalRx.coerceAtLeast(0L) + totalTx.coerceAtLeast(0L)
        } else {
            0L
        }

        return if (total > 0) {
            "Data: ${formatBytes(total)}"
        } else {
            "Data: 0 MB"
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 10.0 -> String.format(Locale.US, "%.1f GB", gb)
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 100.0 -> String.format(Locale.US, "%.0f MB", mb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun openDataSettings(context: Context) {
        val intents = if (!hasUsageStatsPermission(context)) {
            listOf(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                Intent(Settings.ACTION_DATA_USAGE_SETTINGS),
                Intent(Settings.ACTION_WIRELESS_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
        } else {
            listOf(
                Intent(Settings.ACTION_DATA_USAGE_SETTINGS),
                Intent(Settings.ACTION_WIRELESS_SETTINGS),
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                Intent(Settings.ACTION_SETTINGS)
            )
        }

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                break
            } catch (_: Exception) {}
        }
    }
}
