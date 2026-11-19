package com.fj.launcher.ui.home

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.data.DataUsageHelper
import com.fj.launcher.data.LocationHelper
import com.fj.launcher.data.PrayerTimeCalculator
import com.fj.launcher.model.LauncherPreferences
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeaderWidget(
    preferences: LauncherPreferences,
    onClockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val timeFormat = remember(preferences.is24HourClock) {
        if (preferences.is24HourClock) {
            SimpleDateFormat("H:mm", Locale.getDefault())
        } else {
            SimpleDateFormat("h:mm", Locale.getDefault())
        }
    }

    val dateFormat = remember {
        SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    }

    val prayerSchedule = remember(currentTime, preferences.latitude, preferences.longitude) {
        PrayerTimeCalculator.calculate(preferences.latitude, preferences.longitude)
    }

    var weatherText by remember { mutableStateOf("Cerah Berawan") }
    LaunchedEffect(preferences.showWeather, preferences.latitude, preferences.longitude) {
        if (preferences.showWeather) {
            val w = LocationHelper.fetchWeather(preferences.latitude, preferences.longitude)
            if (w != null) weatherText = w
        }
    }

    var dataUsageText by remember { mutableStateOf("") }
    LaunchedEffect(preferences.showDataUsage) {
        if (preferences.showDataUsage) {
            while (true) {
                dataUsageText = DataUsageHelper.getDataUsageSummary(context)
                delay(15000L)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Clock (Matches Image 1)
        if (preferences.showClock) {
            Text(
                text = timeFormat.format(currentTime),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    onClockClick()
                }
            )
        }

        // Date (Matches Image 1: "Wed, 28 Oct")
        if (preferences.showDate) {
            Text(
                text = dateFormat.format(currentTime),
                color = Color(0xFFDDDDDD),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        openCalendarApp(context)
                    }
            )
        }

        // Optional Prayer Time or Weather below date
        if (preferences.showPrayerTime) {
            val countdown = if (prayerSchedule.minutesUntilNext < 60) {
                "${prayerSchedule.minutesUntilNext}m"
            } else {
                "${prayerSchedule.minutesUntilNext / 60}j ${prayerSchedule.minutesUntilNext % 60}m"
            }
            Text(
                text = "${prayerSchedule.nextPrayerName} ${prayerSchedule.nextPrayerTime} (-$countdown)",
                color = Color(0xFF00FF41),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (preferences.showWeather) {
            Text(
                text = weatherText,
                color = Color(0xFFA0A0A0),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (preferences.showDataUsage && dataUsageText.isNotBlank()) {
            Text(
                text = dataUsageText,
                color = Color(0xFFA0A0A0),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable {
                        DataUsageHelper.openDataSettings(context)
                    }
            )
        }
    }
}

private fun openClockApp(context: Context) {
    runCatching {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

private fun openCalendarApp(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
