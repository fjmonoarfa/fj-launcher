package com.fj.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.AlarmClock
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.fj.launcher.data.AppRepository
import com.fj.launcher.data.LocationHelper
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

    private var currentScreen by mutableStateOf(LauncherScreen.HOME)
    private var selectedAppForMenu by mutableStateOf<AppItem?>(null)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            refreshGpsLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appRepository = AppRepository(this, lifecycleScope)
        preferenceManager = PreferenceManager(this)

        requestLocationUpdates()

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

                LaunchedEffect(preferences, currentScreen) {
                    glMatrixView?.let { gv ->
                        gv.renderer.state.theme = preferences.matrixTheme
                        gv.renderer.state.mode = preferences.matrixMode
                        gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                        gv.renderer.state.density = preferences.matrixDensity
                        gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                        gv.renderer.state.wavesEnabled = preferences.matrixWaves
                        gv.renderer.state.fogEnabled = preferences.matrixFog
                        gv.renderer.state.dimmerAlpha = preferences.matrixDimmerAlpha

                        if (currentScreen == LauncherScreen.HOME && preferences.matrixEnabled) {
                            gv.resumeRendering()
                        } else {
                            gv.pauseRendering()
                        }
                    }
                }

                DisposableEffect(currentScreen, selectedAppForMenu) {
                    val callback = object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            if (selectedAppForMenu != null) {
                                selectedAppForMenu = null
                            } else if (currentScreen != LauncherScreen.HOME) {
                                currentScreen = LauncherScreen.HOME
                            } else {
                                // Disable back button on HOME screen (do nothing)
                            }
                        }
                    }
                    onBackPressedDispatcher.addCallback(callback)
                    onDispose { callback.remove() }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Background: Pure Black or GLMatrix xscreensaver
                    if (preferences.matrixEnabled) {
                        AndroidView(
                            factory = { ctx ->
                                GLMatrixView(ctx).also { gv ->
                                    glMatrixView = gv
                                    gv.renderer.state.theme = preferences.matrixTheme
                                    gv.renderer.state.mode = preferences.matrixMode
                                    gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                                    gv.renderer.state.density = preferences.matrixDensity
                                    gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                                    gv.renderer.state.wavesEnabled = preferences.matrixWaves
                                    gv.renderer.state.fogEnabled = preferences.matrixFog
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

                    // Foreground Screen Layer with smooth 180ms crossfade
                    Crossfade(
                        targetState = currentScreen,
                        animationSpec = tween(180),
                        label = "screen_crossfade"
                    ) { targetScreen ->
                        when (targetScreen) {
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    preferences = preferences,
                                    pinnedApps = pinnedApps,
                                    onAppClick = { app -> appRepository.launchApp(app) },
                                    onAppLongClick = { app -> selectedAppForMenu = app },
                                    onClockClick = {
                                        if (preferences.clockApp.isBlank() || preferences.clockApp == "Default") {
                                            openClockApp()
                                        } else if (preferences.clockApp != "None") {
                                            launchPackage(preferences.clockApp)
                                        }
                                    },
                                    onSwipeLeft = {
                                        if (preferences.swipeLeftApp != "None" && preferences.swipeLeftApp.isNotBlank()) {
                                            launchPackage(preferences.swipeLeftApp)
                                        }
                                    },
                                    onSwipeRight = {
                                        if (preferences.swipeRightApp != "None" && preferences.swipeRightApp.isNotBlank()) {
                                            launchPackage(preferences.swipeRightApp)
                                        }
                                    },
                                    onOpenDrawer = { currentScreen = LauncherScreen.DRAWER },
                                    onOpenSettings = { currentScreen = LauncherScreen.SETTINGS }
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
                                    onRenameFolder = { folder, newName ->
                                        lifecycleScope.launch { preferenceManager.saveFolder(folder.copy(name = newName)) }
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
                                    allApps = apps,
                                    hiddenApps = hiddenApps,
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

    override fun onResume() {
        super.onResume()
        glMatrixView?.onActivityResume()
    }

    override fun onPause() {
        super.onPause()
        glMatrixView?.onActivityPause()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            if (currentScreen != LauncherScreen.HOME) {
                currentScreen = LauncherScreen.HOME
            }
            if (selectedAppForMenu != null) {
                selectedAppForMenu = null
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (currentScreen == LauncherScreen.HOME && selectedAppForMenu == null) {
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_HOME) {
                // Completely disable back and home button on HOME screen
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (currentScreen == LauncherScreen.HOME && selectedAppForMenu == null) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
                // Completely disable back and home button on HOME screen
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun launchPackage(packageName: String) {
        if (packageName.isBlank() || packageName == "None") return
        runCatching {
            val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent != null) {
                startActivity(intent)
            }
        }
    }

    private fun openClockApp() {
        runCatching {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun requestLocationUpdates() {
        val hasFine = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            refreshGpsLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun refreshGpsLocation() {
        lifecycleScope.launch {
            val loc = LocationHelper.getCurrentLocation(this@MainActivity)
            if (loc != null) {
                preferenceManager.updatePreferences { current ->
                    current.copy(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        prayerCityName = loc.locationName
                    )
                }
            }
        }
    }
}
