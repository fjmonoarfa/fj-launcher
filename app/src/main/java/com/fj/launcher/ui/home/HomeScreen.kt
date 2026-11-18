package com.fj.launcher.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.VerticalAlignment
import kotlin.math.abs

@Composable
fun HomeScreen(
    preferences: LauncherPreferences,
    pinnedApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onClockClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            // Gestures: Long press on background opens settings
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onOpenSettings()
                    }
                )
            }
            // Swipe gestures (Vertical & Horizontal)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        val absX = abs(totalDragX)
                        val absY = abs(totalDragY)
                        if (absX > absY && absX > 60f) {
                            if (totalDragX > 0f) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        } else if (absY > 60f) {
                            if (totalDragY < -60f) {
                                onOpenDrawer()
                            } else if (totalDragY > 60f) {
                                expandNotifications(context)
                            }
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { _, dragAmount ->
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Widget (Clock & Date - Centered as in Image 1)
            HeaderWidget(
                preferences = preferences,
                onClockClick = onClockClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Pinned Apps with configurable vertical alignment: Top, Center, or Bottom
            when (preferences.pinnedVerticalAlignment) {
                VerticalAlignment.TOP -> {
                    Spacer(modifier = Modifier.height(28.dp))
                    PinnedAppsList(
                        pinnedApps = pinnedApps,
                        preferences = preferences,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                VerticalAlignment.CENTER -> {
                    Spacer(modifier = Modifier.weight(1f))
                    PinnedAppsList(
                        pinnedApps = pinnedApps,
                        preferences = preferences,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
                VerticalAlignment.BOTTOM -> {
                    Spacer(modifier = Modifier.weight(1f))
                    PinnedAppsList(
                        pinnedApps = pinnedApps,
                        preferences = preferences,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

@SuppressLint("WrongConstant")
private fun expandNotifications(context: Context) {
    runCatching {
        val statusBarService = context.getSystemService("statusbar")
        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val expandMethod = statusBarManager.getMethod("expandNotificationsPanel")
        expandMethod.invoke(statusBarService)
    }
}
