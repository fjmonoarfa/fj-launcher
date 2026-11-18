package com.fj.launcher.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.MatrixTheme
import com.fj.launcher.model.SortOrder
import com.fj.launcher.model.TextAlignment
import com.fj.launcher.model.VerticalAlignment
import java.util.Locale

@Composable
fun SettingsScreen(
    preferences: LauncherPreferences,
    allApps: List<AppItem> = emptyList(),
    hiddenApps: List<AppItem>,
    onUpdatePreferences: (LauncherPreferences) -> Unit,
    onUnhideApp: (AppItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showHiddenAppsDialog by remember { mutableStateOf(false) }
    var showAlignmentDialog by remember { mutableStateOf(false) }
    var showVerticalAlignmentDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var appPickerTarget by remember { mutableStateOf<String?>(null) }

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
            // CARD 1: FJ Launcher Header Card (Matches Image 4)
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FJ Launcher",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                SettingsActionRow(
                    title = "Change default launcher",
                    onClick = { openDefaultAppsSettings(context) }
                )
                SettingsActionRow(
                    title = "About and FAQs",
                    onClick = { /* Info */ }
                )
                SettingsActionRow(
                    title = "More features...",
                    onClick = { /* More features */ }
                )
            }

            // CARD 2: Home screen (Matches Image 4)
            SettingsCard {
                SectionTitle(title = "Home screen")

                SettingsValueRow(
                    title = "Apps on home screen",
                    value = "${preferences.maxHomeApps}",
                    onClick = {
                        val next = if (preferences.maxHomeApps >= 10) 4 else preferences.maxHomeApps + 1
                        onUpdatePreferences(preferences.copy(maxHomeApps = next))
                    }
                )

                SettingsValueRow(
                    title = "Show date time",
                    value = if (preferences.showClock && preferences.showDate) "On" else "Off",
                    onClick = {
                        val turnOn = !(preferences.showClock && preferences.showDate)
                        onUpdatePreferences(preferences.copy(showClock = turnOn, showDate = turnOn))
                    }
                )

                SettingsValueRow(
                    title = "Horizontal align",
                    value = when (preferences.pinnedAlignment) {
                        TextAlignment.LEFT -> "Left"
                        TextAlignment.CENTER -> "Center"
                        TextAlignment.RIGHT -> "Right"
                    },
                    onClick = { showAlignmentDialog = true }
                )

                SettingsValueRow(
                    title = "Vertical align",
                    value = when (preferences.pinnedVerticalAlignment) {
                        VerticalAlignment.TOP -> "Top"
                        VerticalAlignment.CENTER -> "Center"
                        VerticalAlignment.BOTTOM -> "Bottom"
                    },
                    onClick = { showVerticalAlignmentDialog = true }
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

            // CARD 3: Appearance (Matches Image 4 & 5)
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
                    title = "Bold font",
                    value = if (preferences.pinnedFontWeightBold) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(pinnedFontWeightBold = !preferences.pinnedFontWeightBold))
                    }
                )

                SettingsValueRow(
                    title = "Text size",
                    value = String.format("%.1f", preferences.pinnedFontSizeSp / 22f),
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
                    title = "Matrix background",
                    value = if (preferences.matrixEnabled) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrixEnabled = !preferences.matrixEnabled))
                    }
                )

                SettingsValueRow(
                    title = "Character mode",
                    value = when (preferences.matrixMode.lowercase()) {
                        "binary" -> "Binary"
                        "hexadecimal" -> "Hex"
                        "decimal" -> "Decimal"
                        "dna" -> "DNA"
                        else -> "Matrix"
                    },
                    onClick = { showModeDialog = true }
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

                var currentSpeed by remember(preferences.matrixSpeed) { mutableFloatStateOf(preferences.matrixSpeed) }
                SettingsSliderRow(
                    title = "Matrix speed",
                    valueLabel = "${String.format(Locale.US, "%.1f", currentSpeed)}x",
                    value = currentSpeed,
                    valueRange = 0.2f..3.0f,
                    steps = 27,
                    onValueChange = { currentSpeed = it },
                    onValueChangeFinished = {
                        val rounded = Math.round(currentSpeed * 10f) / 10f
                        onUpdatePreferences(preferences.copy(matrixSpeed = rounded))
                    }
                )

                var currentDensity by remember(preferences.matrixDensity) { mutableFloatStateOf(preferences.matrixDensity) }
                SettingsSliderRow(
                    title = "Matrix density",
                    valueLabel = "${String.format(Locale.US, "%.1f", currentDensity)}x",
                    value = currentDensity,
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    onValueChange = { currentDensity = it },
                    onValueChangeFinished = {
                        val rounded = Math.round(currentDensity * 10f) / 10f
                        onUpdatePreferences(preferences.copy(matrixDensity = rounded))
                    }
                )

                SettingsValueRow(
                    title = "Brightness waves",
                    value = if (preferences.matrixWaves) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrixWaves = !preferences.matrixWaves))
                    }
                )

                SettingsValueRow(
                    title = "Depth fog",
                    value = if (preferences.matrixFog) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrixFog = !preferences.matrixFog))
                    }
                )

                SettingsValueRow(
                    title = "3D Camera sway",
                    value = if (preferences.matrix3DSway) "On" else "Off",
                    onClick = {
                        onUpdatePreferences(preferences.copy(matrix3DSway = !preferences.matrix3DSway))
                    }
                )

                var currentDimmer by remember(preferences.matrixDimmerAlpha) { mutableFloatStateOf(preferences.matrixDimmerAlpha) }
                SettingsSliderRow(
                    title = "Background dimmer",
                    valueLabel = "${(currentDimmer * 100).toInt()}%",
                    value = currentDimmer,
                    valueRange = 0.0f..0.90f,
                    steps = 17,
                    onValueChange = { currentDimmer = it },
                    onValueChangeFinished = {
                        val rounded = Math.round(currentDimmer * 100f) / 100f
                        onUpdatePreferences(preferences.copy(matrixDimmerAlpha = rounded))
                    }
                )
            }

            // CARD 5: Gestures
            SettingsCard {
                SectionTitle(title = "Gestures")

                SettingsValueRow(
                    title = "Touch clock app",
                    value = getAppDisplayLabel(preferences.clockApp, allApps, defaultLabel = "Default Clock"),
                    onClick = { appPickerTarget = "clock" }
                )

                SettingsValueRow(
                    title = "Swipe left app",
                    value = getAppDisplayLabel(preferences.swipeLeftApp, allApps),
                    onClick = { appPickerTarget = "swipe_left" }
                )

                SettingsValueRow(
                    title = "Swipe right app",
                    value = getAppDisplayLabel(preferences.swipeRightApp, allApps),
                    onClick = { appPickerTarget = "swipe_right" }
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

            // CARD 7: Waktu Sholat Kemenag RI
            SettingsCard {
                SectionTitle(title = "Waktu Sholat")

                val currentSchedule = remember(preferences.latitude, preferences.longitude) {
                    com.fj.launcher.data.PrayerTimeCalculator.calculate(preferences.latitude, preferences.longitude)
                }

                SettingsValueRow(
                    title = "Lokasi",
                    value = if (preferences.prayerCityName.isNotBlank()) preferences.prayerCityName else "GPS (Otomatis)",
                    onClick = { }
                )

                SettingsValueRow(
                    title = "Metode",
                    value = "Kemenag RI",
                    onClick = { }
                )

                HorizontalDivider(color = Color(0xFF222222), modifier = Modifier.padding(vertical = 4.dp))

                // Live schedule preview for today with balanced 3 columns
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Jadwal Hari Ini (${preferences.prayerCityName}):",
                        color = Color(0xFF888888),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Subuh: ${currentSchedule.fajr}", color = Color(0xFF00FF41), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Terbit: ${currentSchedule.sunrise}", color = Color(0xFF888888), fontSize = 12.sp, maxLines = 1)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dzuhur: ${currentSchedule.dhuhr}", color = Color.White, fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ashar: ${currentSchedule.asr}", color = Color.White, fontSize = 12.sp, maxLines = 1)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Maghrib: ${currentSchedule.maghrib}", color = Color.White, fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Isya: ${currentSchedule.isha}", color = Color.White, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Alignment Dialog
    if (showAlignmentDialog) {
        AlertDialog(
            onDismissRequest = { showAlignmentDialog = false },
            title = { Text("App Alignment (Horizontal)") },
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

    // Vertical Alignment Dialog
    if (showVerticalAlignmentDialog) {
        AlertDialog(
            onDismissRequest = { showVerticalAlignmentDialog = false },
            title = { Text("App Alignment (Vertical)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        VerticalAlignment.TOP to "Top (Vertical)",
                        VerticalAlignment.CENTER to "Center (Vertical)",
                        VerticalAlignment.BOTTOM to "Bottom (Vertical)"
                    ).forEach { (align, label) ->
                        TextButton(
                            onClick = {
                                onUpdatePreferences(preferences.copy(pinnedVerticalAlignment = align))
                                showVerticalAlignmentDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = label,
                                color = if (preferences.pinnedVerticalAlignment == align) Color(0xFF00FF41) else Color.White
                            )
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

    // Character Mode Dialog (glmatrix original)
    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            title = { Text("Character Mode") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "matrix" to "Matrix (Katakana & Digits)",
                        "binary" to "Binary (0 & 1)",
                        "hexadecimal" to "Hexadecimal (0-9, A-F)",
                        "decimal" to "Decimal (0-9)",
                        "dna" to "DNA (A, C, G, T)"
                    ).forEach { (mode, label) ->
                        TextButton(
                            onClick = {
                                onUpdatePreferences(preferences.copy(matrixMode = mode))
                                showModeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(label, color = if (preferences.matrixMode.equals(mode, ignoreCase = true)) Color(0xFF00FF41) else Color.White)
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

    // App Picker Dialog (for Clock touch, Swipe Left, Swipe Right)
    if (appPickerTarget != null) {
        val target = appPickerTarget!!
        val targetTitle = when (target) {
            "clock" -> "Pilih Aplikasi untuk Jam"
            "swipe_left" -> "Pilih Aplikasi Swipe Kiri"
            "swipe_right" -> "Pilih Aplikasi Swipe Kanan"
            else -> "Pilih Aplikasi"
        }
        val currentPackage = when (target) {
            "clock" -> preferences.clockApp
            "swipe_left" -> preferences.swipeLeftApp
            "swipe_right" -> preferences.swipeRightApp
            else -> ""
        }

        var searchQuery by remember { mutableStateOf("") }
        val filteredApps = remember(allApps, searchQuery) {
            if (searchQuery.isBlank()) {
                allApps.sortedBy { it.displayLabel.lowercase() }
            } else {
                allApps.filter { it.displayLabel.contains(searchQuery, ignoreCase = true) }
                    .sortedBy { it.displayLabel.lowercase() }
            }
        }

        AlertDialog(
            onDismissRequest = { appPickerTarget = null },
            title = { Text(targetTitle) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari aplikasi...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FF41),
                            unfocusedBorderColor = Color(0xFF444444)
                        )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (target == "clock") {
                            TextButton(
                                onClick = {
                                    onUpdatePreferences(preferences.copy(clockApp = "Default"))
                                    appPickerTarget = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Default Alarm / Clock",
                                        color = if (currentPackage == "Default" || currentPackage.isBlank()) Color(0xFF00FF41) else Color.White
                                    )
                                    if (currentPackage == "Default" || currentPackage.isBlank()) {
                                        Text("✓", color = Color(0xFF00FF41))
                                    }
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                when (target) {
                                    "clock" -> onUpdatePreferences(preferences.copy(clockApp = "None"))
                                    "swipe_left" -> onUpdatePreferences(preferences.copy(swipeLeftApp = "None"))
                                    "swipe_right" -> onUpdatePreferences(preferences.copy(swipeRightApp = "None"))
                                }
                                appPickerTarget = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "None (Nonaktifkan)",
                                    color = if (currentPackage == "None") Color(0xFF00FF41) else Color(0xFFAAAAAA)
                                )
                                if (currentPackage == "None") {
                                    Text("✓", color = Color(0xFF00FF41))
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF333333))

                        filteredApps.forEach { app ->
                            val isSelected = currentPackage == app.packageName
                            TextButton(
                                onClick = {
                                    when (target) {
                                        "clock" -> onUpdatePreferences(preferences.copy(clockApp = app.packageName))
                                        "swipe_left" -> onUpdatePreferences(preferences.copy(swipeLeftApp = app.packageName))
                                        "swipe_right" -> onUpdatePreferences(preferences.copy(swipeRightApp = app.packageName))
                                    }
                                    appPickerTarget = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = app.displayLabel,
                                        color = if (isSelected) Color(0xFF00FF41) else Color.White,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Text("✓", color = Color(0xFF00FF41))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { appPickerTarget = null }) {
                    Text("Batal", color = Color.White)
                }
            }
        )
    }
}

private fun getAppDisplayLabel(
    packageName: String,
    allApps: List<AppItem>,
    defaultLabel: String = "None"
): String {
    if (packageName.isBlank() || packageName == "None") return "None"
    if (packageName == "Default") return defaultLabel
    return allApps.find { it.packageName == packageName }?.displayLabel ?: packageName
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
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            color = Color(0xFFCCCCCC),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = valueLabel,
                color = Color(0xFFCCCCCC),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        )
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
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
