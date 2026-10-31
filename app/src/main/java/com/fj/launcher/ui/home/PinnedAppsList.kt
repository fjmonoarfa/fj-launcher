package com.fj.launcher.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.TextAlignment

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinnedAppsList(
    pinnedApps: List<AppItem>,
    preferences: LauncherPreferences,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = remember(preferences.pinnedColorHex) {
        runCatching {
            Color(android.graphics.Color.parseColor(preferences.pinnedColorHex))
        }.getOrDefault(Color.White)
    }

    val textAlign = when (preferences.pinnedAlignment) {
        TextAlignment.LEFT -> TextAlign.Start
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.End
    }

    val horizontalAlign = when (preferences.pinnedAlignment) {
        TextAlignment.LEFT -> Alignment.Start
        TextAlignment.CENTER -> Alignment.CenterHorizontally
        TextAlignment.RIGHT -> Alignment.End
    }

    if (pinnedApps.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Geser ke atas untuk membuka aplikasi,\ntahan lama untuk pin ke Home",
                color = Color(0x88FFFFFF),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = horizontalAlign,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pinnedApps, key = { it.packageName }) { app ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) }
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = when (preferences.pinnedAlignment) {
                        TextAlignment.LEFT -> Alignment.CenterStart
                        TextAlignment.CENTER -> Alignment.Center
                        TextAlignment.RIGHT -> Alignment.CenterEnd
                    }
                ) {
                    Text(
                        text = app.displayLabel,
                        color = textColor,
                        fontSize = preferences.pinnedFontSizeSp.sp,
                        fontWeight = if (preferences.pinnedFontWeightBold) FontWeight.Bold else FontWeight.Normal,
                        textAlign = textAlign,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}
