package com.fj.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MatrixGreen = Color(0xFF00FF41)
val MatrixDarkGreen = Color(0xFF003B00)
val CyberCyan = Color(0xFF00F0FF)
val AmberGold = Color(0xFFFFB000)
val BloodRed = Color(0xFFFF3333)
val DarkBackground = Color(0xFF0A0A0A)
val SurfaceDark = Color(0xFF141414)
val TextPrimary = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9E9E9E)

private val DarkColorScheme = darkColorScheme(
    primary = MatrixGreen,
    secondary = CyberCyan,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun FJLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
