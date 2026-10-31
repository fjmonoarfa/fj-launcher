package com.fj.launcher.model

import android.content.ComponentName
import android.graphics.drawable.Drawable

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

enum class SortOrder {
    A_TO_Z, Z_TO_A
}

enum class MatrixTheme(val displayName: String, val primaryColorHex: String) {
    CLASSIC_GREEN("Classic Matrix Green", "#00FF41"),
    CYBERPUNK_CYAN("Cyberpunk Cyan", "#00F0FF"),
    GOLDEN_AMBER("Golden Amber", "#FFB000"),
    BLOOD_RED("Blood Red", "#FF3333"),
    HACKER_WHITE("Hacker White", "#F0F0F0")
}

data class AppItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val customLabel: String? = null,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val folderId: String? = null
) {
    val displayLabel: String
        get() = if (!customLabel.isNullOrBlank()) customLabel else label

    val componentName: ComponentName
        get() = ComponentName(packageName, activityName)
}

data class AppFolder(
    val id: String,
    val name: String,
    val colorHex: String = "#00FF41"
)
