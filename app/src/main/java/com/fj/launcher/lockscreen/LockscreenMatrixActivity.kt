package com.fj.launcher.lockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fj.launcher.data.PreferenceManager
import com.fj.launcher.glmatrix.GLMatrixView
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.MatrixTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockscreenMatrixActivity : ComponentActivity() {

    private var userPresentReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show when locked & turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Fullscreen edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Listen for user unlock (e.g. fingerprint unlocked directly by hardware sensor)
        userPresentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    dismissLockscreen()
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(userPresentReceiver, filter)

        val preferenceManager = PreferenceManager(this)

        setContent {
            val preferences by preferenceManager.preferencesFlow.collectAsState(initial = LauncherPreferences())
            LockscreenContent(
                preferences = preferences,
                onDismiss = { dismissLockscreen() }
            )
        }
    }

    private fun dismissLockscreen() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km?.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    finishAndRemoveTask()
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, android.R.anim.fade_out)
                }
                override fun onDismissError() {
                    finishAndRemoveTask()
                }
            }) ?: run {
                finishAndRemoveTask()
                @Suppress("DEPRECATION")
                overridePendingTransition(0, android.R.anim.fade_out)
            }
        } else {
            finishAndRemoveTask()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        userPresentReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Back press also dismisses/unlocks
        dismissLockscreen()
    }
}

@Composable
private fun LockscreenContent(
    preferences: LauncherPreferences,
    onDismiss: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var batteryPct by remember { mutableIntStateOf(100) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Update time & date every second
    LaunchedEffect(preferences.is24HourClock) {
        val timePattern = if (preferences.is24HourClock) "HH:mm" else "hh:mm a"
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("id", "ID"))
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)

            // Read battery
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            bm?.let {
                batteryPct = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            }

            delay(1000)
        }
    }

    // Theme color accent for text
    val accentColor = when (preferences.matrixTheme) {
        MatrixTheme.CLASSIC_GREEN -> Color(0xFF00FF41)
        MatrixTheme.CYBERPUNK_CYAN -> Color(0xFF00E5FF)
        MatrixTheme.GOLDEN_AMBER -> Color(0xFFFFB300)
        MatrixTheme.BLOOD_RED -> Color(0xFFFF1744)
        MatrixTheme.HACKER_WHITE -> Color.White
    }

    // Swipe up animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15f) {
                        onDismiss()
                    }
                }
            }
    ) {
        // GLMatrix live wallpaper background
        AndroidView(
            factory = { ctx ->
                GLMatrixView(ctx).also { gv ->
                    gv.renderer.state.theme = preferences.matrixTheme
                    gv.renderer.state.mode = preferences.matrixMode
                    gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                    gv.renderer.state.density = preferences.matrixDensity
                    gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                    gv.renderer.state.wavesEnabled = preferences.matrixWaves
                    gv.renderer.state.fogEnabled = preferences.matrixFog
                    gv.renderer.state.dimmerAlpha = preferences.matrixDimmerAlpha.coerceAtMost(0.40f)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Clock & Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentDate,
                fontSize = 17.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "⚡ $batteryPct%",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom "Geser ke atas untuk membuka" Hint
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Swipe up",
                tint = accentColor,
                modifier = Modifier
                    .size(28.dp)
                    .offset(y = arrowOffset.dp)
            )
            Text(
                text = "Geser ke atas untuk membuka",
                color = Color(0xCCFFFFFF),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }
}
