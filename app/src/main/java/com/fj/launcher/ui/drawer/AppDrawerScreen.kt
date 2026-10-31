package com.fj.launcher.ui.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.model.AppFolder
import com.fj.launcher.model.AppItem
import com.fj.launcher.model.LauncherPreferences
import com.fj.launcher.model.SortOrder

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerScreen(
    apps: List<AppItem>,
    folders: List<AppFolder>,
    preferences: LauncherPreferences,
    onAppClick: (AppItem) -> Unit,
    onTogglePin: (AppItem) -> Unit,
    onRenameApp: (AppItem, String) -> Unit,
    onHideApp: (AppItem) -> Unit,
    onOpenInfo: (AppItem) -> Unit,
    onUninstall: (AppItem) -> Unit,
    onCreateFolder: (AppFolder) -> Unit,
    onDeleteFolder: (AppFolder) -> Unit,
    onAssignFolder: (AppItem, String?) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var expandedAppPackage by remember { mutableStateOf<String?>(null) }
    var appToRename by remember { mutableStateOf<AppItem?>(null) }
    var appForMoveFolder by remember { mutableStateOf<AppItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedFolderForView by remember { mutableStateOf<AppFolder?>(null) }
    var folderForOptions by remember { mutableStateOf<AppFolder?>(null) }

    // Visible apps (exclude hidden)
    val visibleApps = remember(apps) {
        apps.filter { !it.isHidden }
    }

    // Filter by search
    val searchedApps = remember(visibleApps, searchQuery) {
        if (searchQuery.isBlank()) {
            visibleApps
        } else {
            val q = searchQuery.trim().lowercase()
            visibleApps.filter {
                it.displayLabel.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            }
        }
    }

    // Sort apps
    val sortedApps = remember(searchedApps, preferences.drawerSortOrder) {
        when (preferences.drawerSortOrder) {
            SortOrder.A_TO_Z -> searchedApps.sortedBy { it.displayLabel.lowercase() }
            SortOrder.Z_TO_A -> searchedApps.sortedByDescending { it.displayLabel.lowercase() }
        }
    }

    // Standalone apps outside folders
    val standaloneApps = remember(sortedApps, searchQuery) {
        if (searchQuery.isNotBlank()) {
            sortedApps
        } else {
            sortedApps.filter { it.folderId == null }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xE6050505)) // Subtle translucent dark overlay
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Minimalist Indicator / Handle (Kembali ke garis minimalist "—")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSearchActive) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(3.dp)
                            .background(Color(0xFFCCCCCC), RoundedCornerShape(2.dp))
                            .combinedClickable(
                                onClick = { isSearchActive = true },
                                onLongClick = { showCreateFolderDialog = true }
                            )
                    )
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = { Text("Cari aplikasi...", color = Color.Gray, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF00FF41),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Tutup",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF41),
                            unfocusedBorderColor = Color(0xFF333333),
                            focusedContainerColor = Color(0xFF141414),
                            unfocusedContainerColor = Color(0xFF141414),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // FOLDERS FIRST (warna sama dengan font/teks: Color.White)
                if (searchQuery.isBlank() && preferences.drawerFolderFirst && folders.isNotEmpty()) {
                    items(folders, key = { "folder_${it.id}" }) { folder ->
                        val count = visibleApps.count { it.folderId == folder.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { selectedFolderForView = folder },
                                    onLongClick = { folderForOptions = folder }
                                )
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = folder.name,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = folder.name,
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$count",
                                color = Color(0xFF777777),
                                fontSize = 14.sp
                            )
                        }
                    }

                    item(key = "divider_folders") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // APPS LIST
                items(standaloneApps, key = { it.packageName }) { app ->
                    val isExpanded = expandedAppPackage == app.packageName

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // App Item Name
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isExpanded) {
                                            expandedAppPackage = null
                                        } else {
                                            onAppClick(app)
                                        }
                                    },
                                    onLongClick = {
                                        expandedAppPackage = if (isExpanded) null else app.packageName
                                    }
                                )
                                .padding(vertical = 15.dp)
                        ) {
                            Text(
                                text = app.displayLabel,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.2.sp
                            )
                        }

                        // INLINE APP OPTIONS (Matches minimalist design, includes Folder)
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Uninstall
                                InlineActionItem(
                                    icon = Icons.Default.Delete,
                                    label = "Uninstall",
                                    onClick = {
                                        expandedAppPackage = null
                                        onUninstall(app)
                                    }
                                )

                                // 2. Rename
                                InlineActionItem(
                                    icon = Icons.Default.Edit,
                                    label = "Rename",
                                    onClick = {
                                        expandedAppPackage = null
                                        appToRename = app
                                    }
                                )

                                // 3. Pin / Unpin
                                InlineActionItem(
                                    icon = Icons.Default.PushPin,
                                    label = if (app.isPinned) "Unpin" else "Pin",
                                    tint = if (app.isPinned) Color(0xFF00FF41) else Color.White,
                                    onClick = {
                                        onTogglePin(app)
                                    }
                                )

                                // 4. Move to Folder / Folder (Warna putih konsisten dengan teks)
                                InlineActionItem(
                                    icon = Icons.Default.Folder,
                                    label = "Folder",
                                    tint = Color.White,
                                    onClick = {
                                        expandedAppPackage = null
                                        appForMoveFolder = app
                                    }
                                )

                                // 5. Hide
                                InlineActionItem(
                                    icon = Icons.Default.VisibilityOff,
                                    label = "Hide",
                                    onClick = {
                                        expandedAppPackage = null
                                        onHideApp(app)
                                    }
                                )

                                // 6. Info
                                InlineActionItem(
                                    icon = Icons.Default.Info,
                                    label = "Info",
                                    onClick = {
                                        expandedAppPackage = null
                                        onOpenInfo(app)
                                    }
                                )

                                // 7. Close
                                InlineActionItem(
                                    icon = Icons.Default.Close,
                                    label = "Tutup",
                                    onClick = {
                                        expandedAppPackage = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { folder ->
                onCreateFolder(folder)
                showCreateFolderDialog = false
            }
        )
    }

    // Move to Folder Dialog
    appForMoveFolder?.let { targetApp ->
        MoveToFolderDialog(
            app = targetApp,
            folders = folders,
            onDismiss = { appForMoveFolder = null },
            onAssignFolder = { app, folderId ->
                onAssignFolder(app, folderId)
            },
            onCreateFolder = onCreateFolder
        )
    }

    // Rename Dialog
    appToRename?.let { app ->
        var newName by remember { mutableStateOf(app.displayLabel) }
        AlertDialog(
            onDismissRequest = { appToRename = null },
            title = { Text("Rename Aplikasi") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Baru") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameApp(app, newName.trim())
                        appToRename = null
                    }
                ) {
                    Text("Simpan", color = Color(0xFF00FF41))
                }
            },
            dismissButton = {
                TextButton(onClick = { appToRename = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Folder View Modal
    selectedFolderForView?.let { currentFolder ->
        val activeFolder = folders.find { it.id == currentFolder.id } ?: currentFolder
        val appsInFolder = remember(apps, activeFolder.id) {
            apps.filter { it.folderId == activeFolder.id }
        }
        FolderContentSheet(
            folder = activeFolder,
            appsInFolder = appsInFolder,
            folders = folders,
            onDismiss = { selectedFolderForView = null },
            onAppClick = {
                selectedFolderForView = null
                onAppClick(it)
            },
            onTogglePin = onTogglePin,
            onRenameApp = onRenameApp,
            onHideApp = onHideApp,
            onOpenInfo = onOpenInfo,
            onUninstall = onUninstall,
            onRemoveFromFolder = { targetApp ->
                onAssignFolder(targetApp, null)
            },
            onAssignFolder = onAssignFolder,
            onCreateFolder = onCreateFolder,
            onDeleteFolder = {
                onDeleteFolder(it)
                selectedFolderForView = null
            },
            onRenameFolder = { targetFolder, newName ->
                onCreateFolder(targetFolder.copy(name = newName))
            }
        )
    }

    // Folder Options Dialog (from Drawer Long Click)
    folderForOptions?.let { folder ->
        FolderOptionsDialog(
            folder = folder,
            onDismiss = { folderForOptions = null },
            onRename = { newName ->
                onCreateFolder(folder.copy(name = newName))
                folderForOptions = null
            },
            onDelete = {
                onDeleteFolder(folder)
                folderForOptions = null
            }
        )
    }
}

@Composable
internal fun InlineActionItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
