package com.fj.launcher.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.data.LocationHelper
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.MatrixTheme
import com.fj.launcher.model.SortOrder
import com.fj.launcher.model.TextAlignment
import kotlinx.coroutines.launch
import java.util.Locale

enum class GestureTarget {
    SWIPE_LEFT, SWIPE_RIGHT
}

@Composable
fun SettingsScreen(
    preferences: LauncherPreferences,
    hiddenApps: List<AppItem>,
    allApps: List<AppItem> = emptyList(),
    onUpdatePreferences: (LauncherPreferences) -> Unit,
    onUnhideApp: (AppItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showHiddenAppsDialog by remember { mutableStateOf(false) }
    var showAlignmentDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectingGestureTarget by remember { mutableStateOf<GestureTarget?>(null) }
    var isSyncingGps by remember { mutableStateOf(false) }

    // Runtime Permission Launcher for GPS Location
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isSyncingGps = true
            coroutineScope.launch {
                val res = LocationHelper.getCurrentLocation(context)
                if (res != null) {
                    onUpdatePreferences(
                        preferences.copy(
                            latitude = res.latitude,
                            longitude = res.longitude,
                            prayerCityName = res.locationName
                        )
                    )
                }
                isSyncingGps = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "FJ Launcher Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }

            // CARD 1: Default Launcher Status
            SettingsCard {
                Text(
                    text = "Default Launcher",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set as default",
                        color = Color.White,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Set",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            openDefaultAppsSettings(context)
                        }
                    )
                }
            }

            // CARD 2: Home Screen
            SettingsCard {
                SectionTitle(title = "Home Screen")

                SettingsValueRow(
                    title = "Time",
                    value = if (preferences.showClock) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(showClock = !preferences.showClock))
                    }
                )

                SettingsValueRow(
                    title = "24-hour time",
                    value = if (preferences.is24HourClock) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(is24HourClock = !preferences.is24HourClock))
                    }
                )

                SettingsValueRow(
                    title = "Date",
                    value = if (preferences.showDate) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(showDate = !preferences.showDate))
                    }
                )

                SettingsValueRow(
                    title = "App alignment",
                    value = when (preferences.pinnedAlignment) {
                        TextAlignment.LEFT -> "Left"
                        TextAlignment.CENTER -> "Center"
                        TextAlignment.RIGHT -> "Right"
                    },
                    onClick = { showAlignmentDialog = true }
                )

                SettingsValueRow(
                    title = "Show prayer times",
                    value = if (preferences.showPrayerTime) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(showPrayerTime = !preferences.showPrayerTime))
                    }
                )

                SettingsValueRow(
                    title = "Weather",
                    value = if (preferences.showWeather) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(showWeather = !preferences.showWeather))
                    }
                )
            }

            // CARD 3: Appearance
            SettingsCard {
                SectionTitle(title = "Appearance")

                SettingsValueRow(
                    title = "Auto show keyboard",
                    value = if (preferences.autoOpenKeyboard) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(autoOpenKeyboard = !preferences.autoOpenKeyboard))
                    }
                )

                SettingsValueRow(
                    title = "Notification bar",
                    value = if (preferences.notificationBar) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(notificationBar = !preferences.notificationBar))
                    }
                )

                SettingsValueRow(
                    title = "Theme mode",
                    value = preferences.themeMode,
                    onClick = { }
                )

                SettingsValueRow(
                    title = "Bold font",
                    value = if (preferences.pinnedFontWeightBold) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(pinnedFontWeightBold = !preferences.pinnedFontWeightBold))
                    }
                )

                SettingsValueRow(
                    title = "Text size",
                    value = String.format(Locale.US, "%.1f", preferences.pinnedFontSizeSp / 22f),
                    onClick = {
                        val nextSize = if (preferences.pinnedFontSizeSp >= 28f) 18f else preferences.pinnedFontSizeSp + 2f
                        onUpdatePreferences(preferences.copy(pinnedFontSizeSp = nextSize))
                    }
                )
            }

            // CARD 4: GLMatrix (xscreensaver Jamie Zawinski)
            SettingsCard {
                SectionTitle(title = "GLMatrix (xscreensaver)")

                SettingsValueRow(
                    title = "Matrix rain background",
                    value = if (preferences.matrixEnabled) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrixEnabled = !preferences.matrixEnabled))
                    }
                )

                SettingsValueRow(
                    title = "Matrix theme",
                    value = when (preferences.matrixTheme) {
                        MatrixTheme.CLASSIC_GREEN -> "Classic Green"
                        MatrixTheme.CYBERPUNK_CYAN -> "Cyan"
                        MatrixTheme.GOLDEN_AMBER -> "Amber"
                        MatrixTheme.BLOOD_RED -> "Blood Red"
                        MatrixTheme.HACKER_WHITE -> "White"
                    },
                    onClick = { showThemeDialog = true }
                )

                // Slider untuk Matrix speed
                SettingsSliderRow(
                    title = "Matrix speed",
                    valueText = "${String.format(Locale.US, "%.1f", preferences.matrixSpeed)}x",
                    value = preferences.matrixSpeed,
                    valueRange = 0.2f..3.0f,
                    onValueChange = {
                        onUpdatePreferences(preferences.copy(matrixSpeed = it))
                    }
                )

                SettingsValueRow(
                    title = "3D Camera sway",
                    value = if (preferences.matrix3DSway) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrix3DSway = !preferences.matrix3DSway))
                    }
                )

                // Slider untuk Background dimmer
                SettingsSliderRow(
                    title = "Background dimmer",
                    valueText = "${(preferences.matrixDimmerAlpha * 100).toInt()}%",
                    value = preferences.matrixDimmerAlpha,
                    valueRange = 0.0f..0.95f,
                    onValueChange = {
                        onUpdatePreferences(preferences.copy(matrixDimmerAlpha = it))
                    }
                )
            }

            // CARD 5: Gestures
            SettingsCard {
                SectionTitle(title = "Gestures")

                val swipeRightName = remember(preferences.swipeRightApp, allApps) {
                    if (preferences.swipeRightApp.isBlank() || preferences.swipeRightApp.equals("None", ignoreCase = true)) {
                        "None"
                    } else {
                        allApps.find { it.packageName == preferences.swipeRightApp }?.displayLabel ?: preferences.swipeRightApp
                    }
                }

                val swipeLeftName = remember(preferences.swipeLeftApp, allApps) {
                    if (preferences.swipeLeftApp.isBlank() || preferences.swipeLeftApp.equals("None", ignoreCase = true)) {
                        "None"
                    } else {
                        allApps.find { it.packageName == preferences.swipeLeftApp }?.displayLabel ?: preferences.swipeLeftApp
                    }
                }

                SettingsValueRow(
                    title = "Geser dari kiri (Swipe Right)",
                    value = swipeRightName,
                    onClick = { selectingGestureTarget = GestureTarget.SWIPE_RIGHT }
                )

                SettingsValueRow(
                    title = "Geser dari kanan (Swipe Left)",
                    value = swipeLeftName,
                    onClick = { selectingGestureTarget = GestureTarget.SWIPE_LEFT }
                )

                SettingsValueRow(
                    title = "Double tap to lock",
                    value = if (preferences.doubleTapToLock) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(doubleTapToLock = !preferences.doubleTapToLock))
                    }
                )
            }

            // CARD 6: Apps & Folders
            SettingsCard {
                SectionTitle(title = "Drawer & Folders")

                SettingsValueRow(
                    title = "Folder first",
                    value = if (preferences.drawerFolderFirst) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(drawerFolderFirst = !preferences.drawerFolderFirst))
                    }
                )

                SettingsValueRow(
                    title = "Sort order",
                    value = when (preferences.drawerSortOrder) {
                        SortOrder.A_TO_Z -> "A-Z"
                        SortOrder.Z_TO_A -> "Z-A"
                    },
                    onClick = {
                        val next = if (preferences.drawerSortOrder == SortOrder.A_TO_Z) SortOrder.Z_TO_A else SortOrder.A_TO_Z
                        onUpdatePreferences(preferences.copy(drawerSortOrder = next))
                    }
                )

                SettingsValueRow(
                    title = "Hidden apps",
                    value = "${hiddenApps.size} apps",
                    onClick = { showHiddenAppsDialog = true }
                )
            }

            // CARD 7: Waktu Sholat & Cuaca (GPS based)
            SettingsCard {
                SectionTitle(title = "Waktu Sholat & Cuaca")

                val currentSchedule = remember(preferences.latitude, preferences.longitude) {
                    com.fj.launcher.data.PrayerTimeCalculator.calculate(preferences.latitude, preferences.longitude)
                }

                SettingsValueRow(
                    title = "Lokasi GPS",
                    value = if (isSyncingGps) "Mencari GPS..." else preferences.prayerCityName,
                    onClick = {
                        val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasFine || hasCoarse) {
                            isSyncingGps = true
                            coroutineScope.launch {
                                val res = LocationHelper.getCurrentLocation(context)
                                if (res != null) {
                                    onUpdatePreferences(
                                        preferences.copy(
                                            latitude = res.latitude,
                                            longitude = res.longitude,
                                            prayerCityName = res.locationName
                                        )
                                    )
                                }
                                isSyncingGps = false
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )

                Text(
                    text = "Koordinat: ${String.format(Locale.US, "%.4f, %.4f", preferences.latitude, preferences.longitude)} (Ketuk untuk update GPS)",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                SettingsValueRow(
                    title = "Metode Perhitungan",
                    value = "Kemenag RI",
                    onClick = { }
                )

                HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 4.dp))

                // Live schedule preview for today
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Jadwal Hari Ini (${preferences.prayerCityName}):",
                        color = Color(0xFF888888),
                        fontSize = 13.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subuh: ${currentSchedule.fajr}", color = Color(0xFF00FF41), fontSize = 13.sp)
                        Text("Dzuhur: ${currentSchedule.dhuhr}", color = Color.White, fontSize = 13.sp)
                        Text("Ashar: ${currentSchedule.asr}", color = Color.White, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Terbit: ${currentSchedule.sunrise}", color = Color(0xFF888888), fontSize = 13.sp)
                        Text("Maghrib: ${currentSchedule.maghrib}", color = Color.White, fontSize = 13.sp)
                        Text("Isya: ${currentSchedule.isha}", color = Color.White, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Alignment Dialog
    if (showAlignmentDialog) {
        AlertDialog(
            onDismissRequest = { showAlignmentDialog = false },
            title = { Text("App Alignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TextAlignment.LEFT to "Left",
                        TextAlignment.CENTER to "Center",
                        TextAlignment.RIGHT to "Right"
                    ).forEach { (align, label) ->
                        TextButton(
                            onClick = {
                                onUpdatePreferences(preferences.copy(pinnedAlignment = align))
                                showAlignmentDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, color = if (preferences.pinnedAlignment == align) Color(0xFF00FF41) else Color.White)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Matrix Theme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MatrixTheme.values().forEach { theme ->
                        TextButton(
                            onClick = {
                                onUpdatePreferences(preferences.copy(matrixTheme = theme))
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(theme.displayName, color = if (preferences.matrixTheme == theme) Color(0xFF00FF41) else Color.White)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Hidden Apps Dialog
    if (showHiddenAppsDialog) {
        AlertDialog(
            onDismissRequest = { showHiddenAppsDialog = false },
            title = { Text("Hidden Apps") },
            text = {
                if (hiddenApps.isEmpty()) {
                    Text("No hidden apps.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        hiddenApps.forEach { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app.displayLabel, color = Color.White, modifier = Modifier.weight(1f))
                                TextButton(onClick = { onUnhideApp(app) }) {
                                    Text("Unhide", color = Color(0xFF00FF41))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHiddenAppsDialog = false }) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // App Picker Dialog for Gestures
    selectingGestureTarget?.let { target ->
        val dialogTitle = if (target == GestureTarget.SWIPE_RIGHT) {
            "Pilih Aplikasi (Geser dari Kiri)"
        } else {
            "Pilih Aplikasi (Geser dari Kanan)"
        }
        val currentSelected = if (target == GestureTarget.SWIPE_RIGHT) {
            preferences.swipeRightApp
        } else {
            preferences.swipeLeftApp
        }

        AppPickerDialog(
            title = dialogTitle,
            currentSelected = currentSelected,
            apps = allApps,
            onDismiss = { selectingGestureTarget = null },
            onSelect = { selectedPackage ->
                if (target == GestureTarget.SWIPE_RIGHT) {
                    onUpdatePreferences(preferences.copy(swipeRightApp = selectedPackage))
                } else {
                    onUpdatePreferences(preferences.copy(swipeLeftApp = selectedPackage))
                }
                selectingGestureTarget = null
            }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, Color(0xFF282828)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SettingsValueRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingsSliderRow(
    title: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = valueText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AppPickerDialog(
    title: String,
    currentSelected: String,
    apps: List<AppItem>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps.sortedBy { it.displayLabel.lowercase() }
        } else {
            val q = searchQuery.trim().lowercase()
            apps.filter {
                it.displayLabel.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }.sortedBy { it.displayLabel.lowercase() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari aplikasi...", fontSize = 14.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF00FF41),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Option "None"
                    item {
                        TextButton(
                            onClick = { onSelect("None") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "None (Tidak Ada / Nonaktifkan)",
                                    color = if (currentSelected.isBlank() || currentSelected.equals("None", ignoreCase = true)) {
                                        Color(0xFF00FF41)
                                    } else {
                                        Color.Gray
                                    },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFF262626), modifier = Modifier.padding(vertical = 4.dp))
                    }

                    items(filteredApps, key = { it.packageName }) { app ->
                        val isSelected = currentSelected == app.packageName
                        TextButton(
                            onClick = { onSelect(app.packageName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = app.displayLabel,
                                    color = if (isSelected) Color(0xFF00FF41) else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Text(
                                        text = "Dipilih",
                                        color = Color(0xFF00FF41),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

private fun openDefaultAppsSettings(context: Context) {
    runCatching {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        runCatching {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
