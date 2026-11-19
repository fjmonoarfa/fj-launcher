package com.fj.launcher.model

data class LauncherPreferences(
    // Home Screen Settings
    val showClock: Boolean = true,
    val is24HourClock: Boolean = true,
    val showDate: Boolean = true,
    val showWeather: Boolean = false,
    val showPrayerTime: Boolean = false,
    val showDataUsage: Boolean = true,
    val clockApp: String = "Default",
    val pinnedAlignment: TextAlignment = TextAlignment.CENTER,
    val pinnedVerticalAlignment: VerticalAlignment = VerticalAlignment.CENTER,
    val pinnedFontSizeSp: Float = 22f,
    val pinnedFontWeightBold: Boolean = false,
    val pinnedColorHex: String = "#FFFFFF",
    val maxHomeApps: Int = 7,
    
    // Matrix Background Settings (GLMatrix xscreensaver)
    val matrixEnabled: Boolean = true,
    val matrixTheme: MatrixTheme = MatrixTheme.CLASSIC_GREEN,
    val matrixMode: String = "matrix",
    val matrixSpeed: Float = 1.0f,
    val matrixDensity: Float = 1.0f,
    val matrix3DSway: Boolean = true,
    val matrixWaves: Boolean = true,
    val matrixFog: Boolean = true,
    val matrixDimmerAlpha: Float = 0.50f,
    
    // Apps Drawer Settings
    val drawerSortOrder: SortOrder = SortOrder.A_TO_Z,
    val drawerFolderFirst: Boolean = true,
    val autoOpenKeyboard: Boolean = false,
    
    // Appearance & Gestures
    val themeMode: String = "Dark",
    val notificationBar: Boolean = true,
    val swipeLeftApp: String = "None",
    val swipeRightApp: String = "None",
    val doubleTapToLock: Boolean = false,
    
    // Prayer Time Settings
    val prayerCityName: String = "Jakarta",
    val latitude: Double = -6.2088,
    val longitude: Double = 106.8456,
    val calculationMethod: String = "Kemenag RI"
)
