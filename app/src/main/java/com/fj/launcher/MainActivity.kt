package com.fj.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.fj.launcher.data.AppRepository
import com.fj.launcher.data.PreferenceManager
import com.fj.launcher.glmatrix.GLMatrixView
import com.fj.launcher.model.AppFolder
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.ui.drawer.AppContextMenu
import com.fj.launcher.ui.drawer.AppDrawerScreen
import com.fj.launcher.ui.home.HomeScreen
import com.fj.launcher.ui.settings.SettingsScreen
import com.fj.launcher.ui.theme.FJLauncherTheme
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class LauncherScreen {
    HOME, DRAWER, SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var appRepository: AppRepository
    private lateinit var preferenceManager: PreferenceManager
    private var glMatrixView: GLMatrixView? = null

    // Screen state at activity level so onNewIntent can return to HOME
    private var currentScreen by mutableStateOf(LauncherScreen.HOME)

    // Receiver for physical/system Home key events
    private val homeKeyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason == "homekey") {
                    if (currentScreen == LauncherScreen.HOME) {
                        // Di home screen disable tombol home
                        return
                    }
                    currentScreen = LauncherScreen.HOME
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge full screen for GLMatrix under status & navigation bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        // Lock screen di-handle secara normal/native oleh Android OS

        // Sinkronisasi otomatis lokasi GPS untuk Waktu Sholat & Cuaca
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val gps = com.fj.launcher.data.LocationHelper.getCurrentLocation(this@MainActivity)
            if (gps != null) {
                preferenceManager.updatePreferences { prefs ->
                    prefs.copy(
                        latitude = gps.latitude,
                        longitude = gps.longitude,
                        prayerCityName = gps.locationName
                    )
                }
            }
        }

        appRepository = AppRepository(this, lifecycleScope)
        preferenceManager = PreferenceManager(this)

        setContent {
            FJLauncherTheme {
                val preferences by preferenceManager.preferencesFlow.collectAsState(initial = LauncherPreferences())
                val folders by preferenceManager.foldersFlow.collectAsState(initial = emptyList())

                val apps by remember {
                    combine(
                        appRepository.getInstalledAppsFlow(),
                        preferenceManager.pinnedAppsFlow,
                        preferenceManager.hiddenAppsFlow,
                        preferenceManager.appAliasesFlow,
                        preferenceManager.appFoldersMapFlow
                    ) { rawList, pinnedSet, hiddenSet, aliasesMap, folderMap ->
                        rawList.map { item ->
                            item.copy(
                                isPinned = pinnedSet.contains(item.packageName),
                                isHidden = hiddenSet.contains(item.packageName),
                                customLabel = aliasesMap[item.packageName],
                                folderId = folderMap[item.packageName]
                            )
                        }
                    }
                }.collectAsState(initial = emptyList())

                val pinnedApps = remember(apps, preferences.maxHomeApps) {
                    apps.filter { it.isPinned }.take(preferences.maxHomeApps)
                }

                val hiddenApps = remember(apps) {
                    apps.filter { it.isHidden }
                }

                var selectedAppForMenu by remember { mutableStateOf<AppItem?>(null) }

                LaunchedEffect(preferences, currentScreen) {
                    glMatrixView?.let { gv ->
                        gv.renderer.state.theme = preferences.matrixTheme
                        gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                        gv.renderer.state.density = preferences.matrixDensity
                        gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                        gv.renderer.state.dimmerAlpha = preferences.matrixDimmerAlpha

                        if (currentScreen == LauncherScreen.HOME && preferences.matrixEnabled) {
                            gv.resumeRendering()
                        } else {
                            gv.pauseRendering()
                        }
                    }
                }

                // Back navigation handling:
                // - If in DRAWER or SETTINGS: return to HOME
                // - If in HOME: DO NOTHING (tombol back tidak berfungsi di home screen)
                DisposableEffect(currentScreen) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (currentScreen != LauncherScreen.HOME) {
                                currentScreen = LauncherScreen.HOME
                            } else {
                                // Do nothing! Launcher stays on home screen, does not exit.
                            }
                        }
                    }
                    onBackPressedDispatcher.addCallback(callback)
                    onDispose { callback.remove() }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Background: Pure Black or GLMatrix xscreensaver (Full Screen Edge-to-Edge)
                    if (preferences.matrixEnabled) {
                        AndroidView(
                            factory = { ctx ->
                                GLMatrixView(ctx).also { gv ->
                                    glMatrixView = gv
                                    gv.renderer.state.theme = preferences.matrixTheme
                                    gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                                    gv.renderer.state.density = preferences.matrixDensity
                                    gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                                    gv.renderer.state.dimmerAlpha = preferences.matrixDimmerAlpha
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black)
                        )
                    }

                    // Foreground Screen Layer
                    when (currentScreen) {
                        LauncherScreen.HOME -> {
                            HomeScreen(
                                preferences = preferences,
                                pinnedApps = pinnedApps,
                                onAppClick = { app -> appRepository.launchApp(app) },
                                onAppLongClick = { app -> selectedAppForMenu = app },
                                onOpenDrawer = { currentScreen = LauncherScreen.DRAWER },
                                onOpenSettings = { currentScreen = LauncherScreen.SETTINGS },
                                onSwipeLeft = {
                                    // Swipe dari kanan (ke kiri)
                                    if (preferences.swipeLeftApp.isNotBlank() && !preferences.swipeLeftApp.equals("None", ignoreCase = true)) {
                                        appRepository.launchAppByPackageName(preferences.swipeLeftApp)
                                    }
                                },
                                onSwipeRight = {
                                    // Swipe dari kiri (ke kanan)
                                    if (preferences.swipeRightApp.isNotBlank() && !preferences.swipeRightApp.equals("None", ignoreCase = true)) {
                                        appRepository.launchAppByPackageName(preferences.swipeRightApp)
                                    }
                                }
                            )
                        }
                        LauncherScreen.DRAWER -> {
                            AppDrawerScreen(
                                apps = apps,
                                folders = folders,
                                preferences = preferences,
                                onAppClick = { app ->
                                    appRepository.launchApp(app)
                                    currentScreen = LauncherScreen.HOME
                                },
                                onTogglePin = { app ->
                                    lifecycleScope.launch { preferenceManager.togglePinApp(app.packageName) }
                                },
                                onRenameApp = { app, newLabel ->
                                    lifecycleScope.launch { preferenceManager.renameApp(app.packageName, newLabel) }
                                },
                                onHideApp = { app ->
                                    lifecycleScope.launch { preferenceManager.setAppHidden(app.packageName, true) }
                                },
                                onOpenInfo = { app ->
                                    appRepository.openAppInfo(app)
                                },
                                onUninstall = { app ->
                                    appRepository.uninstallApp(app)
                                },
                                onCreateFolder = { folder ->
                                    lifecycleScope.launch { preferenceManager.saveFolder(folder) }
                                },
                                onDeleteFolder = { folder ->
                                    lifecycleScope.launch { preferenceManager.deleteFolder(folder.id) }
                                },
                                onAssignFolder = { app, folderId ->
                                    lifecycleScope.launch { preferenceManager.assignAppToFolder(app.packageName, folderId) }
                                },
                                onCloseDrawer = { currentScreen = LauncherScreen.HOME }
                            )
                        }
                        LauncherScreen.SETTINGS -> {
                            SettingsScreen(
                                preferences = preferences,
                                hiddenApps = hiddenApps,
                                allApps = apps,
                                onUpdatePreferences = { updated ->
                                    lifecycleScope.launch {
                                        preferenceManager.updatePreferences { updated }
                                    }
                                },
                                onUnhideApp = { app ->
                                    lifecycleScope.launch {
                                        preferenceManager.setAppHidden(app.packageName, false)
                                    }
                                },
                                onBack = { currentScreen = LauncherScreen.HOME }
                            )
                        }
                    }

                    // Context Menu Modal BottomSheet (For long-press in Home Screen)
                    selectedAppForMenu?.let { app ->
                        AppContextMenu(
                            app = app,
                            folders = folders,
                            onDismiss = { selectedAppForMenu = null },
                            onRename = { targetApp, newLabel ->
                                lifecycleScope.launch {
                                    preferenceManager.renameApp(targetApp.packageName, newLabel)
                                }
                            },
                            onTogglePin = { targetApp ->
                                lifecycleScope.launch {
                                    preferenceManager.togglePinApp(targetApp.packageName)
                                }
                            },
                            onToggleHide = { targetApp ->
                                lifecycleScope.launch {
                                    preferenceManager.setAppHidden(targetApp.packageName, !targetApp.isHidden)
                                }
                            },
                            onAssignFolder = { targetApp, folderId ->
                                lifecycleScope.launch {
                                    preferenceManager.assignAppToFolder(targetApp.packageName, folderId)
                                }
                            },
                            onOpenInfo = { targetApp ->
                                appRepository.openAppInfo(targetApp)
                            },
                            onUninstall = { targetApp ->
                                appRepository.uninstallApp(targetApp)
                            }
                        )
                    }
                }
            }
        }
    }

    // Disable Home Button di Home Screen, tetap izinkan kembali dari Drawer/Settings
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_HOME) {
            if (currentScreen == LauncherScreen.HOME) {
                return true // Consume and do nothing!
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME && currentScreen == LauncherScreen.HOME) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME && currentScreen == LauncherScreen.HOME) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (currentScreen == LauncherScreen.HOME) {
            // Sudah di home screen: abaikan sepenuhnya (disable tombol home di home screen)
            return
        }
        currentScreen = LauncherScreen.HOME
    }

    override fun onResume() {
        super.onResume()
        glMatrixView?.resumeRendering()
        runCatching {
            val filter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(homeKeyReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(homeKeyReceiver, filter)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        glMatrixView?.pauseRendering()
        runCatching {
            unregisterReceiver(homeKeyReceiver)
        }
    }
}
