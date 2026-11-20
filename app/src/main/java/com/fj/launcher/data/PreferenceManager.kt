package com.fj.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.fj.launcher.model.AppFolder
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.MatrixTheme
import com.fj.launcher.model.SortOrder
import com.fj.launcher.model.TextAlignment
import com.fj.launcher.model.VerticalAlignment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fj_launcher_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        // Home
        val KEY_SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val KEY_IS_24H_CLOCK = booleanPreferencesKey("is_24h_clock")
        val KEY_SHOW_DATE = booleanPreferencesKey("show_date")
        val KEY_SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val KEY_SHOW_PRAYER = booleanPreferencesKey("show_prayer")
        val KEY_SHOW_DATA_USAGE = booleanPreferencesKey("show_data_usage")
        val KEY_CLOCK_APP = stringPreferencesKey("clock_app")
        val KEY_PINNED_ALIGNMENT = stringPreferencesKey("pinned_alignment")
        val KEY_PINNED_VERTICAL_ALIGNMENT = stringPreferencesKey("pinned_vertical_alignment")
        val KEY_PINNED_FONT_SIZE = floatPreferencesKey("pinned_font_size")
        val KEY_PINNED_FONT_BOLD = booleanPreferencesKey("pinned_font_bold")
        val KEY_PINNED_COLOR = stringPreferencesKey("pinned_color")
        val KEY_MAX_HOME_APPS = intPreferencesKey("max_home_apps")

        // Matrix
        val KEY_MATRIX_ENABLED = booleanPreferencesKey("matrix_enabled")
        val KEY_MATRIX_THEME = stringPreferencesKey("matrix_theme")
        val KEY_MATRIX_MODE = stringPreferencesKey("matrix_mode")
        val KEY_MATRIX_SPEED = floatPreferencesKey("matrix_speed")
        val KEY_MATRIX_DENSITY = floatPreferencesKey("matrix_density")
        val KEY_MATRIX_SWAY = booleanPreferencesKey("matrix_sway")
        val KEY_MATRIX_WAVES = booleanPreferencesKey("matrix_waves")
        val KEY_MATRIX_FOG = booleanPreferencesKey("matrix_fog")
        val KEY_MATRIX_DIMMER = floatPreferencesKey("matrix_dimmer")
        val KEY_MATRIX_LOCKSCREEN_ENABLED = booleanPreferencesKey("matrix_lockscreen_enabled")

        // Drawer
        val KEY_DRAWER_SORT = stringPreferencesKey("drawer_sort")
        val KEY_DRAWER_FOLDER_FIRST = booleanPreferencesKey("drawer_folder_first")
        val KEY_AUTO_OPEN_KEYBOARD = booleanPreferencesKey("auto_open_keyboard")

        // Appearance & Gestures
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_NOTIFICATION_BAR = booleanPreferencesKey("notification_bar")
        val KEY_SWIPE_LEFT_APP = stringPreferencesKey("swipe_left_app")
        val KEY_SWIPE_RIGHT_APP = stringPreferencesKey("swipe_right_app")
        val KEY_DOUBLE_TAP_LOCK = booleanPreferencesKey("double_tap_lock")

        // App Metadata
        val KEY_PINNED_APPS = stringSetPreferencesKey("pinned_apps")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val KEY_APP_ALIASES = stringPreferencesKey("app_aliases_json")
        val KEY_FOLDERS_JSON = stringPreferencesKey("folders_json")
        val KEY_APP_FOLDERS_MAP = stringPreferencesKey("app_folders_map_json")

        // Location
        val KEY_CITY_NAME = stringPreferencesKey("city_name")
        val KEY_LATITUDE = doublePreferencesKey("latitude")
        val KEY_LONGITUDE = doublePreferencesKey("longitude")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.dataStore.data.map { prefs ->
        LauncherPreferences(
            showClock = prefs[KEY_SHOW_CLOCK] ?: true,
            is24HourClock = prefs[KEY_IS_24H_CLOCK] ?: true,
            showDate = prefs[KEY_SHOW_DATE] ?: true,
            showWeather = prefs[KEY_SHOW_WEATHER] ?: false,
            showPrayerTime = prefs[KEY_SHOW_PRAYER] ?: false,
            showDataUsage = prefs[KEY_SHOW_DATA_USAGE] ?: true,
            clockApp = prefs[KEY_CLOCK_APP] ?: "Default",
            pinnedAlignment = runCatching {
                TextAlignment.valueOf(prefs[KEY_PINNED_ALIGNMENT] ?: TextAlignment.CENTER.name)
            }.getOrDefault(TextAlignment.CENTER),
            pinnedVerticalAlignment = runCatching {
                VerticalAlignment.valueOf(prefs[KEY_PINNED_VERTICAL_ALIGNMENT] ?: VerticalAlignment.CENTER.name)
            }.getOrDefault(VerticalAlignment.CENTER),
            pinnedFontSizeSp = prefs[KEY_PINNED_FONT_SIZE] ?: 22f,
            pinnedFontWeightBold = prefs[KEY_PINNED_FONT_BOLD] ?: false,
            pinnedColorHex = prefs[KEY_PINNED_COLOR] ?: "#FFFFFF",
            maxHomeApps = prefs[KEY_MAX_HOME_APPS] ?: 7,
            matrixEnabled = prefs[KEY_MATRIX_ENABLED] ?: true,
            matrixTheme = runCatching {
                MatrixTheme.valueOf(prefs[KEY_MATRIX_THEME] ?: MatrixTheme.CLASSIC_GREEN.name)
            }.getOrDefault(MatrixTheme.CLASSIC_GREEN),
            matrixMode = prefs[KEY_MATRIX_MODE] ?: "matrix",
            matrixSpeed = prefs[KEY_MATRIX_SPEED] ?: 1.0f,
            matrixDensity = prefs[KEY_MATRIX_DENSITY] ?: 1.0f,
            matrix3DSway = prefs[KEY_MATRIX_SWAY] ?: true,
            matrixWaves = prefs[KEY_MATRIX_WAVES] ?: true,
            matrixFog = prefs[KEY_MATRIX_FOG] ?: true,
            matrixDimmerAlpha = prefs[KEY_MATRIX_DIMMER] ?: 0.50f,
            matrixLockscreenEnabled = prefs[KEY_MATRIX_LOCKSCREEN_ENABLED] ?: false,
            drawerSortOrder = runCatching {
                SortOrder.valueOf(prefs[KEY_DRAWER_SORT] ?: SortOrder.A_TO_Z.name)
            }.getOrDefault(SortOrder.A_TO_Z),
            drawerFolderFirst = prefs[KEY_DRAWER_FOLDER_FIRST] ?: true,
            autoOpenKeyboard = prefs[KEY_AUTO_OPEN_KEYBOARD] ?: false,
            themeMode = prefs[KEY_THEME_MODE] ?: "Dark",
            notificationBar = prefs[KEY_NOTIFICATION_BAR] ?: true,
            swipeLeftApp = prefs[KEY_SWIPE_LEFT_APP] ?: "None",
            swipeRightApp = prefs[KEY_SWIPE_RIGHT_APP] ?: "None",
            doubleTapToLock = prefs[KEY_DOUBLE_TAP_LOCK] ?: false,
            prayerCityName = prefs[KEY_CITY_NAME] ?: "Jakarta",
            latitude = prefs[KEY_LATITUDE] ?: -6.2088,
            longitude = prefs[KEY_LONGITUDE] ?: 106.8456
        )
    }

    suspend fun updatePreferences(transform: (LauncherPreferences) -> LauncherPreferences) {
        context.dataStore.edit { prefs ->
            val current = LauncherPreferences(
                showClock = prefs[KEY_SHOW_CLOCK] ?: true,
                is24HourClock = prefs[KEY_IS_24H_CLOCK] ?: true,
                showDate = prefs[KEY_SHOW_DATE] ?: true,
                showWeather = prefs[KEY_SHOW_WEATHER] ?: false,
                showPrayerTime = prefs[KEY_SHOW_PRAYER] ?: false,
                showDataUsage = prefs[KEY_SHOW_DATA_USAGE] ?: true,
                clockApp = prefs[KEY_CLOCK_APP] ?: "Default",
                pinnedAlignment = runCatching {
                    TextAlignment.valueOf(prefs[KEY_PINNED_ALIGNMENT] ?: TextAlignment.CENTER.name)
                }.getOrDefault(TextAlignment.CENTER),
                pinnedVerticalAlignment = runCatching {
                    VerticalAlignment.valueOf(prefs[KEY_PINNED_VERTICAL_ALIGNMENT] ?: VerticalAlignment.CENTER.name)
                }.getOrDefault(VerticalAlignment.CENTER),
                pinnedFontSizeSp = prefs[KEY_PINNED_FONT_SIZE] ?: 22f,
                pinnedFontWeightBold = prefs[KEY_PINNED_FONT_BOLD] ?: false,
                pinnedColorHex = prefs[KEY_PINNED_COLOR] ?: "#FFFFFF",
                maxHomeApps = prefs[KEY_MAX_HOME_APPS] ?: 7,
                matrixEnabled = prefs[KEY_MATRIX_ENABLED] ?: true,
                matrixTheme = runCatching {
                    MatrixTheme.valueOf(prefs[KEY_MATRIX_THEME] ?: MatrixTheme.CLASSIC_GREEN.name)
                }.getOrDefault(MatrixTheme.CLASSIC_GREEN),
                matrixMode = prefs[KEY_MATRIX_MODE] ?: "matrix",
                matrixSpeed = prefs[KEY_MATRIX_SPEED] ?: 1.0f,
                matrixDensity = prefs[KEY_MATRIX_DENSITY] ?: 1.0f,
                matrix3DSway = prefs[KEY_MATRIX_SWAY] ?: true,
                matrixWaves = prefs[KEY_MATRIX_WAVES] ?: true,
                matrixFog = prefs[KEY_MATRIX_FOG] ?: true,
                matrixDimmerAlpha = prefs[KEY_MATRIX_DIMMER] ?: 0.50f,
                matrixLockscreenEnabled = prefs[KEY_MATRIX_LOCKSCREEN_ENABLED] ?: false,
                drawerSortOrder = runCatching {
                    SortOrder.valueOf(prefs[KEY_DRAWER_SORT] ?: SortOrder.A_TO_Z.name)
                }.getOrDefault(SortOrder.A_TO_Z),
                drawerFolderFirst = prefs[KEY_DRAWER_FOLDER_FIRST] ?: true,
                autoOpenKeyboard = prefs[KEY_AUTO_OPEN_KEYBOARD] ?: false,
                themeMode = prefs[KEY_THEME_MODE] ?: "Dark",
                notificationBar = prefs[KEY_NOTIFICATION_BAR] ?: true,
                swipeLeftApp = prefs[KEY_SWIPE_LEFT_APP] ?: "None",
                swipeRightApp = prefs[KEY_SWIPE_RIGHT_APP] ?: "None",
                doubleTapToLock = prefs[KEY_DOUBLE_TAP_LOCK] ?: false,
                prayerCityName = prefs[KEY_CITY_NAME] ?: "Jakarta",
                latitude = prefs[KEY_LATITUDE] ?: -6.2088,
                longitude = prefs[KEY_LONGITUDE] ?: 106.8456
            )
            val updated = transform(current)

            prefs[KEY_SHOW_CLOCK] = updated.showClock
            prefs[KEY_IS_24H_CLOCK] = updated.is24HourClock
            prefs[KEY_SHOW_DATE] = updated.showDate
            prefs[KEY_SHOW_WEATHER] = updated.showWeather
            prefs[KEY_SHOW_PRAYER] = updated.showPrayerTime
            prefs[KEY_SHOW_DATA_USAGE] = updated.showDataUsage
            prefs[KEY_CLOCK_APP] = updated.clockApp
            prefs[KEY_PINNED_ALIGNMENT] = updated.pinnedAlignment.name
            prefs[KEY_PINNED_VERTICAL_ALIGNMENT] = updated.pinnedVerticalAlignment.name
            prefs[KEY_PINNED_FONT_SIZE] = updated.pinnedFontSizeSp
            prefs[KEY_PINNED_FONT_BOLD] = updated.pinnedFontWeightBold
            prefs[KEY_PINNED_COLOR] = updated.pinnedColorHex
            prefs[KEY_MAX_HOME_APPS] = updated.maxHomeApps
            prefs[KEY_MATRIX_ENABLED] = updated.matrixEnabled
            prefs[KEY_MATRIX_THEME] = updated.matrixTheme.name
            prefs[KEY_MATRIX_MODE] = updated.matrixMode
            prefs[KEY_MATRIX_SPEED] = updated.matrixSpeed
            prefs[KEY_MATRIX_DENSITY] = updated.matrixDensity
            prefs[KEY_MATRIX_SWAY] = updated.matrix3DSway
            prefs[KEY_MATRIX_WAVES] = updated.matrixWaves
            prefs[KEY_MATRIX_FOG] = updated.matrixFog
            prefs[KEY_MATRIX_DIMMER] = updated.matrixDimmerAlpha
            prefs[KEY_MATRIX_LOCKSCREEN_ENABLED] = updated.matrixLockscreenEnabled
            prefs[KEY_DRAWER_SORT] = updated.drawerSortOrder.name
            prefs[KEY_DRAWER_FOLDER_FIRST] = updated.drawerFolderFirst
            prefs[KEY_AUTO_OPEN_KEYBOARD] = updated.autoOpenKeyboard
            prefs[KEY_THEME_MODE] = updated.themeMode
            prefs[KEY_NOTIFICATION_BAR] = updated.notificationBar
            prefs[KEY_SWIPE_LEFT_APP] = updated.swipeLeftApp
            prefs[KEY_SWIPE_RIGHT_APP] = updated.swipeRightApp
            prefs[KEY_DOUBLE_TAP_LOCK] = updated.doubleTapToLock
            prefs[KEY_CITY_NAME] = updated.prayerCityName
            prefs[KEY_LATITUDE] = updated.latitude
            prefs[KEY_LONGITUDE] = updated.longitude
        }
    }

    val pinnedAppsFlow: Flow<Set<String>> = context.dataStore.data.map { it[KEY_PINNED_APPS] ?: emptySet() }
    val hiddenAppsFlow: Flow<Set<String>> = context.dataStore.data.map { it[KEY_HIDDEN_APPS] ?: emptySet() }

    val appAliasesFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_APP_ALIASES] ?: "{}"
        val map = mutableMapOf<String, String>()
        runCatching {
            val json = JSONObject(jsonStr)
            for (key in json.keys()) {
                map[key] = json.getString(key)
            }
        }
        map
    }

    val foldersFlow: Flow<List<AppFolder>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_FOLDERS_JSON] ?: "[]"
        val list = mutableListOf<AppFolder>()
        runCatching {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AppFolder(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        colorHex = obj.optString("colorHex", "#00FF41")
                    )
                )
            }
        }
        list
    }

    val appFoldersMapFlow: Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        val jsonStr = prefs[KEY_APP_FOLDERS_MAP] ?: "{}"
        val map = mutableMapOf<String, String>()
        runCatching {
            val json = JSONObject(jsonStr)
            for (key in json.keys()) {
                map[key] = json.getString(key)
            }
        }
        map
    }

    suspend fun togglePinApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val set = (prefs[KEY_PINNED_APPS] ?: emptySet()).toMutableSet()
            if (set.contains(packageName)) set.remove(packageName) else set.add(packageName)
            prefs[KEY_PINNED_APPS] = set
        }
    }

    suspend fun setAppHidden(packageName: String, hide: Boolean) {
        context.dataStore.edit { prefs ->
            val set = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            if (hide) set.add(packageName) else set.remove(packageName)
            prefs[KEY_HIDDEN_APPS] = set
        }
    }

    suspend fun renameApp(packageName: String, customLabel: String?) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_APP_ALIASES] ?: "{}"
            val json = JSONObject(jsonStr)
            if (customLabel.isNullOrBlank()) {
                json.remove(packageName)
            } else {
                json.put(packageName, customLabel)
            }
            prefs[KEY_APP_ALIASES] = json.toString()
        }
    }

    suspend fun saveFolder(folder: AppFolder) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_FOLDERS_JSON] ?: "[]"
            val arr = JSONArray(jsonStr)
            val updated = JSONArray()
            var replaced = false
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("id") == folder.id) {
                    val newObj = JSONObject().apply {
                        put("id", folder.id)
                        put("name", folder.name)
                        put("colorHex", folder.colorHex)
                    }
                    updated.put(newObj)
                    replaced = true
                } else {
                    updated.put(obj)
                }
            }
            if (!replaced) {
                updated.put(JSONObject().apply {
                    put("id", folder.id)
                    put("name", folder.name)
                    put("colorHex", folder.colorHex)
                })
            }
            prefs[KEY_FOLDERS_JSON] = updated.toString()
        }
    }

    suspend fun deleteFolder(folderId: String) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_FOLDERS_JSON] ?: "[]"
            val arr = JSONArray(jsonStr)
            val updated = JSONArray()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                if (obj.getString("id") != folderId) {
                    updated.put(obj)
                }
            }
            prefs[KEY_FOLDERS_JSON] = updated.toString()

            val mapStr = prefs[KEY_APP_FOLDERS_MAP] ?: "{}"
            val mapJson = JSONObject(mapStr)
            val keysToRemove = mutableListOf<String>()
            for (key in mapJson.keys()) {
                if (mapJson.getString(key) == folderId) keysToRemove.add(key)
            }
            keysToRemove.forEach { mapJson.remove(it) }
            prefs[KEY_APP_FOLDERS_MAP] = mapJson.toString()
        }
    }

    suspend fun assignAppToFolder(packageName: String, folderId: String?) {
        context.dataStore.edit { prefs ->
            val jsonStr = prefs[KEY_APP_FOLDERS_MAP] ?: "{}"
            val json = JSONObject(jsonStr)
            if (folderId.isNullOrBlank()) {
                json.remove(packageName)
            } else {
                json.put(packageName, folderId)
            }
            prefs[KEY_APP_FOLDERS_MAP] = json.toString()
        }
    }
}
