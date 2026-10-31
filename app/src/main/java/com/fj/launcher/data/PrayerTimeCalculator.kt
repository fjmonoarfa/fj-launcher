package com.fj.launcher.data

import java.util.Calendar
import java.util.Locale
import kotlin.math.*

data class PrayerSchedule(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val minutesUntilNext: Long
)

/**
 * High-accuracy astronomical prayer times calculator based on Kemenag RI standards:
 * - Subuh: Sun depression angle 20.0°
 * - Terbit (Sunrise): Sun depression angle 0.833°
 * - Dzuhur: Solar transit (Zawal) + ihtiyat
 * - Ashar: Shafi'i shadow factor = 1.0 (arccot(1 + tan|lat - decl|))
 * - Maghrib: Sun depression angle 0.833° (Sunset) + ihtiyat
 * - Isya: Sun depression angle 18.0°
 * - Ihtiyat (safety buffer): +2 to +2.5 minutes standard for Indonesia
 */
object PrayerTimeCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        calendar: Calendar = Calendar.getInstance()
    ): PrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Accurate timezone offset accounting for daylight/raw offset
        val timezoneOffsetHours = calendar.timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        val julianDate = julianDate(year, month, day) - (longitude / (15.0 * 24.0))

        // Solar coordinates (Jean Meeus algorithm)
        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l))))) / 15.0

        // Equation of time (hours) & solar declination (degrees)
        val eqT = q / 15.0 - ra
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))

        // Solar noon (transit)
        val noon = fixHour(12.0 + timezoneOffsetHours - (longitude / 15.0) - eqT)

        // Kemenag standard depression angles
        val sunAltSunrise = -0.833
        val fajrAngle = -20.0
        val ishaAngle = -18.0

        // Asr altitude above horizon: arccot(1 + tan(|lat - decl|)) = atan(1 / (1 + tan(|lat - decl|)))
        val asrDiff = abs(latitude - decl)
        val asrAlt = Math.toDegrees(atan(1.0 / (1.0 + tan(Math.toRadians(asrDiff)))))

        val haSunrise = sunHourAngle(sunAltSunrise, latitude, decl)
        val haFajr = sunHourAngle(fajrAngle, latitude, decl)
        val haIsha = sunHourAngle(ishaAngle, latitude, decl)
        val haAsr = sunHourAngle(asrAlt, latitude, decl)

        // Kemenag RI standard ihtiyat (safety buffer ~2.5 mins)
        val ihtiyat = 2.5 / 60.0

        val fajrHour = noon - haFajr + (2.0 / 60.0)
        val sunriseHour = noon - haSunrise - (1.0 / 60.0)
        val dhuhrHour = noon + ihtiyat
        val asrHour = noon + haAsr + ihtiyat
        val maghribHour = noon + haSunrise + ihtiyat
        val ishaHour = noon + haIsha + ihtiyat

        val fFajr = formatTime(fajrHour)
        val fSunrise = formatTime(sunriseHour)
        val fDhuhr = formatTime(dhuhrHour)
        val fAsr = formatTime(asrHour)
        val fMaghrib = formatTime(maghribHour)
        val fIsha = formatTime(ishaHour)

        // Current time in fractional hours
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) +
                (calendar.get(Calendar.MINUTE) / 60.0) +
                (calendar.get(Calendar.SECOND) / 3600.0)

        val prayers = listOf(
            "Subuh" to fajrHour,
            "Terbit" to sunriseHour,
            "Dzuhur" to dhuhrHour,
            "Ashar" to asrHour,
            "Maghrib" to maghribHour,
            "Isya" to ishaHour
        )

        var nextName = "Subuh"
        var nextHour = fajrHour + 24.0

        for ((name, hour) in prayers) {
            if (hour > currentHour) {
                nextName = name
                nextHour = hour
                break
            }
        }

        val minutesUntil = ((nextHour - currentHour) * 60.0).toLong().coerceAtLeast(0)
        val nextTimeFormatted = when (nextName) {
            "Subuh" -> fFajr
            "Terbit" -> fSunrise
            "Dzuhur" -> fDhuhr
            "Ashar" -> fAsr
            "Maghrib" -> fMaghrib
            else -> fIsha
        }

        return PrayerSchedule(
            fajr = fFajr,
            sunrise = fSunrise,
            dhuhr = fDhuhr,
            asr = fAsr,
            maghrib = fMaghrib,
            isha = fIsha,
            nextPrayerName = nextName,
            nextPrayerTime = nextTimeFormatted,
            minutesUntilNext = minutesUntil
        )
    }

    private fun sunHourAngle(alpha: Double, latitude: Double, declination: Double): Double {
        val sinAlpha = sin(Math.toRadians(alpha))
        val sinLat = sin(Math.toRadians(latitude))
        val sinDecl = sin(Math.toRadians(declination))
        val cosLat = cos(Math.toRadians(latitude))
        val cosDecl = cos(Math.toRadians(declination))

        val cosHA = (sinAlpha - sinLat * sinDecl) / (cosLat * cosDecl)
        val clamped = cosHA.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clamped)) / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun formatTime(hourFraction: Double): String {
        val fixed = fixHour(hourFraction)
        val hours = floor(fixed).toInt()
        val minutes = floor((fixed - hours) * 60.0 + 0.5).toInt()
        val adjHours = if (minutes >= 60) (hours + 1) % 24 else hours
        val adjMins = minutes % 60
        return String.format(Locale.US, "%02d:%02d", adjHours, adjMins)
    }
}
