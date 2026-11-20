package com.fj.launcher.lockscreen

import android.app.AlarmManager
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.fj.launcher.data.PreferenceManager
import com.fj.launcher.glmatrix.GLMatrixView
import com.fj.launcher.model.LauncherPreferences
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LockscreenMatrixActivity : ComponentActivity() {

    companion object {
        var isLockscreenActive: Boolean = false
    }

    private var userPresentReceiver: BroadcastReceiver? = null
    private var glMatrixView: GLMatrixView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isLockscreenActive = true

        // Show when locked, but DO NOT wake/turn on screen automatically
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        // Do NOT add FLAG_KEEP_SCREEN_ON so the screen can timeout naturally

        // Allow drawing behind system bars while keeping system status bar visible
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

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
                onDismiss = { dismissLockscreen() },
                onGlViewCreated = { glMatrixView = it }
            )
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

    private fun dismissLockscreen() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && km?.isKeyguardLocked == true) {
            km.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    finishAndRemoveTask()
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, android.R.anim.fade_out)
                }
                override fun onDismissError() {
                    finishAndRemoveTask()
                }
            })
        } else {
            finishAndRemoveTask()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isLockscreenActive = false
        glMatrixView?.onActivityPause()
        glMatrixView = null
        userPresentReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        userPresentReceiver = null
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
    onDismiss: () -> Unit,
    onGlViewCreated: (GLMatrixView) -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var nextAlarmTime by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Update time, date, and next alarm
    LaunchedEffect(preferences.is24HourClock) {
        val timePattern = if (preferences.is24HourClock) "H:mm" else "h:mm"
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        val alarmFormat = SimpleDateFormat(timePattern, Locale.getDefault())

        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now)

            // Read next alarm
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val nextAlarm = am?.nextAlarmClock
            nextAlarmTime = if (nextAlarm != null && nextAlarm.triggerTime > System.currentTimeMillis()) {
                alarmFormat.format(Date(nextAlarm.triggerTime))
            } else {
                null
            }

            delay(1000)
        }
    }

    // Warm pastel peach / cream clock color matching Android 14 / Realme lockscreen
    val clockColor = Color(0xFFFFD5A5)

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
        // GLMatrix live background
        AndroidView(
            factory = { ctx ->
                GLMatrixView(ctx).also { gv ->
                    onGlViewCreated(gv)
                    gv.renderer.state.theme = preferences.matrixTheme
                    gv.renderer.state.mode = preferences.matrixMode
                    gv.renderer.state.speedMultiplier = preferences.matrixSpeed
                    gv.renderer.state.density = preferences.matrixDensity
                    gv.renderer.state.cameraSwayEnabled = preferences.matrix3DSway
                    gv.renderer.state.wavesEnabled = preferences.matrixWaves
                    gv.renderer.state.fogEnabled = preferences.matrixFog
                    gv.renderer.state.dimmerAlpha = preferences.matrixDimmerAlpha.coerceAtMost(0.35f)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Lock & Clock & Date Section (matching uploaded lockscreen layout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 18.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Big Clock Numbers (Warm pastel peach/cream)
            Text(
                text = currentTime,
                fontSize = 86.sp,
                fontWeight = FontWeight.Normal,
                color = clockColor,
                letterSpacing = (-1).sp,
                lineHeight = 90.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Date & Alarm row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = currentDate,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal
                )

                if (nextAlarmTime != null) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Alarm",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = nextAlarmTime!!,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // Bottom "Swipe up to open" & Shortcut Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp, start = 28.dp, end = 28.dp)
        ) {
            Text(
                text = "Swipe up to open",
                color = Color(0xEEFFFFFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Center)
            )

            // Circular Quick Shortcut (Grid/Apps) Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF333336),
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.CenterEnd)
                    .clickable { onDismiss() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Apps",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
