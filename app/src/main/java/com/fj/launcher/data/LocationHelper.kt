package com.fj.launcher.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

data class GpsLocationResult(
    val latitude: Double,
    val longitude: Double,
    val locationName: String
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GpsLocationResult? = withContext(Dispatchers.IO) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return@withContext null
        }

        // 1. Check last known location from providers
        var bestLocation: Location? = null
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)

        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                val loc = runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
                if (loc != null) {
                    if (bestLocation == null || loc.time > bestLocation.time) {
                        bestLocation = loc
                    }
                }
            }
        }

        // If last known location is fresh (< 30 minutes), use it
        val now = System.currentTimeMillis()
        if (bestLocation != null && (now - bestLocation.time) < 30 * 60 * 1000) {
            val name = resolveLocationName(context, bestLocation.latitude, bestLocation.longitude)
            return@withContext GpsLocationResult(bestLocation.latitude, bestLocation.longitude, name)
        }

        // 2. Request single update
        suspendCancellableCoroutine { continuation ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    val name = resolveLocationName(context, location.latitude, location.longitude)
                    if (continuation.isActive) {
                        continuation.resume(GpsLocationResult(location.latitude, location.longitude, name))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            var requested = false
            for (provider in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
                if (locationManager.isProviderEnabled(provider)) {
                    runCatching {
                        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        requested = true
                    }
                    if (requested) break
                }
            }

            if (!requested) {
                if (bestLocation != null) {
                    val name = resolveLocationName(context, bestLocation.latitude, bestLocation.longitude)
                    continuation.resume(GpsLocationResult(bestLocation.latitude, bestLocation.longitude, name))
                } else {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                runCatching { locationManager.removeUpdates(listener) }
            }
        }
    }

    fun resolveLocationName(context: Context, latitude: Double, longitude: Double): String {
        return runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                addr.subAdminArea ?: addr.locality ?: addr.adminArea ?: "Lokasi GPS"
            } else {
                String.format(Locale.US, "GPS: %.2f, %.2f", latitude, longitude)
            }
        }.getOrDefault(String.format(Locale.US, "GPS: %.2f, %.2f", latitude, longitude))
    }

    suspend fun fetchWeather(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current_weather=true"
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JSONObject(response)
                val current = json.getJSONObject("current_weather")
                val temp = current.getDouble("temperature").toInt()
                val code = current.getInt("weathercode")

                val desc = when (code) {
                    0 -> "Cerah"
                    1, 2 -> "Cerah Berawan"
                    3 -> "Berawan"
                    45, 48 -> "Berkabut"
                    51, 53, 55 -> "Gerimis"
                    61, 63, 65 -> "Hujan"
                    80, 81, 82 -> "Hujan Deras"
                    95, 96, 99 -> "Badai Petir"
                    else -> "Cerah Berawan"
                }
                "$temp°C $desc"
            } else {
                null
            }
        }.getOrNull()
    }
}

