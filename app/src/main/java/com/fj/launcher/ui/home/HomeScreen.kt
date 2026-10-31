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

@Composable
fun HomeScreen(
    preferences: LauncherPreferences,
    pinnedApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
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
            // Swipe gestures:
            // Vertical: UP -> Drawer, DOWN -> Notifications
            // Horizontal: LEFT (Geser ke kiri / dari kanan), RIGHT (Geser ke kanan / dari kiri)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        val absX = kotlin.math.abs(totalDragX)
                        val absY = kotlin.math.abs(totalDragY)
                        val threshold = 75f

                        if (absX > absY && absX > threshold) {
                            if (totalDragX > 0f) {
                                // Geser dari kiri ke kanan -> Swipe Right
                                onSwipeRight()
                            } else {
                                // Geser dari kanan ke kiri -> Swipe Left
                                onSwipeLeft()
                            }
                        } else if (absY > absX && absY > threshold) {
                            if (totalDragY < -threshold) {
                                onOpenDrawer()
                            } else if (totalDragY > threshold) {
                                expandNotifications(context)
                            }
                        }
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(0.7f))

            // Centered Pinned Apps List
            PinnedAppsList(
                pinnedApps = pinnedApps,
                preferences = preferences,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1.3f))
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
