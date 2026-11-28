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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    onRenameFolder: (AppFolder, String) -> Unit = { folder, newName -> onCreateFolder(folder.copy(name = newName)) },
    onAssignFolder: (AppItem, String?) -> Unit = { _, _ -> },
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var expandedAppPackage by remember { mutableStateOf<String?>(null) }
    var appToRename by remember { mutableStateOf<AppItem?>(null) }
    var appToAssignFolder by remember { mutableStateOf<AppItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedFolderForView by remember { mutableStateOf<AppFolder?>(null) }
    var folderForOptions by remember { mutableStateOf<AppFolder?>(null) }
    var folderToRename by remember { mutableStateOf<AppFolder?>(null) }
    var folderToDeleteConfirm by remember { mutableStateOf<AppFolder?>(null) }

    // Visible apps (exclude hidden)
    val visibleApps = remember(apps) {
        apps.filter { !it.isHidden }
    }

    // Precompute folder app counts for O(1) item layout
    val folderCounts = remember(visibleApps) {
        visibleApps.groupingBy { it.folderId }.eachCount()
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

    val listState = rememberLazyListState()

    val bgAlpha = (1f - preferences.drawerTransparency).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top Minimalist Indicator / Handle (Tap for search, Long-press to create folder)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSearchActive) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = { isSearchActive = true },
                                    onLongClick = { showCreateFolderDialog = true }
                                )
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(3.dp)
                                    .background(Color.White, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = { Text("Cari aplikasi...", color = Color.Gray, fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF00FF41), modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                isSearchActive = false
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Tutup", tint = Color.Gray, modifier = Modifier.size(18.dp))
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

            // App List with optimized view recycling
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 36.dp)
            ) {
                // FOLDERS FIRST (Placed at the very top of app list, always B/W monochrome)
                if (searchQuery.isBlank() && folders.isNotEmpty()) {
                    items(
                        items = folders,
                        key = { "folder_${it.id}" },
                        contentType = { "folder_item" }
                    ) { folder ->
                        val count = folderCounts[folder.id] ?: 0
                        DrawerFolderItem(
                            folder = folder,
                            count = count,
                            onClick = { selectedFolderForView = folder },
                            onLongClick = { folderForOptions = folder }
                        )
                    }

                    item(key = "divider_folders", contentType = "divider") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // APPS LIST (Optimized with DrawerAppItem to prevent over-composition during scroll)
                items(
                    items = standaloneApps,
                    key = { it.packageName },
                    contentType = { "app_item" }
                ) { app ->
                    val isExpanded = expandedAppPackage == app.packageName
                    DrawerAppItem(
                        app = app,
                        isExpanded = isExpanded,
                        onItemClick = {
                            if (isExpanded) {
                                expandedAppPackage = null
                            } else {
                                onAppClick(app)
                            }
                        },
                        onItemLongClick = {
                            expandedAppPackage = if (isExpanded) null else app.packageName
                        },
                        onUninstall = {
                            expandedAppPackage = null
                            onUninstall(app)
                        },
                        onRename = {
                            expandedAppPackage = null
                            appToRename = app
                        },
                        onFolder = {
                            expandedAppPackage = null
                            appToAssignFolder = app
                        },
                        onTogglePin = {
                            onTogglePin(app)
                        },
                        onHide = {
                            expandedAppPackage = null
                            onHideApp(app)
                        },
                        onInfo = {
                            expandedAppPackage = null
                            onOpenInfo(app)
                        },
                        onClose = {
                            expandedAppPackage = null
                        }
                    )
                }
            }
        }
    }

    // Rename Dialog
    appToRename?.let { app ->
        var newName by remember { mutableStateOf(app.displayLabel) }
        AlertDialog(
            onDismissRequest = { appToRename = null },
            containerColor = Color(0xFF161616),
            title = { Text("Rename Aplikasi", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Baru") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF444444)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameApp(app, newName.trim())
                        appToRename = null
                    }
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToRename = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Folder View Modal
    selectedFolderForView?.let { folder ->
        val appsInFolder = remember(apps, folder.id) {
            apps.filter { it.folderId == folder.id && !it.isHidden }
        }
        FolderContentSheet(
            folder = folder,
            appsInFolder = appsInFolder,
            folders = folders,
            drawerTransparency = preferences.drawerTransparency,
            onDismiss = { selectedFolderForView = null },
            onAppClick = {
                selectedFolderForView = null
                onAppClick(it)
            },
            onTogglePin = onTogglePin,
            onRenameApp = onRenameApp,
            onHideApp = {
                selectedFolderForView = null
                onHideApp(it)
            },
            onOpenInfo = {
                selectedFolderForView = null
                onOpenInfo(it)
            },
            onUninstall = {
                selectedFolderForView = null
                onUninstall(it)
            },
            onAssignFolder = onAssignFolder,
            onCreateFolder = onCreateFolder,
            onRenameFolder = onRenameFolder,
            onDeleteFolder = {
                onDeleteFolder(it)
                selectedFolderForView = null
            }
        )
    }

    // Folder Options Dialog for Drawer Folder Item
    folderForOptions?.let { folder ->
        FolderOptionsDialog(
            folder = folder,
            onDismiss = { folderForOptions = null },
            onRename = {
                folderForOptions = null
                folderToRename = folder
            },
            onDelete = {
                folderForOptions = null
                folderToDeleteConfirm = folder
            }
        )
    }

    folderToRename?.let { folder ->
        RenameFolderDialog(
            folder = folder,
            onDismiss = { folderToRename = null },
            onConfirm = { newName ->
                onRenameFolder(folder, newName)
                folderToRename = null
            }
        )
    }

    folderToDeleteConfirm?.let { folder ->
        DeleteFolderConfirmationDialog(
            folder = folder,
            onDismiss = { folderToDeleteConfirm = null },
            onConfirm = {
                onDeleteFolder(folder)
                folderToDeleteConfirm = null
            }
        )
    }

    // Create Folder Dialog (triggered by long-pressing top handle or "+ Buat Folder Baru")
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { newFolder ->
                onCreateFolder(newFolder)
                showCreateFolderDialog = false
            }
        )
    }

    // Assign / Move / Remove App from Folder Dialog
    appToAssignFolder?.let { targetApp ->
        AssignFolderDialog(
            app = targetApp,
            folders = folders,
            onDismiss = { appToAssignFolder = null },
            onAssign = { targetFolder ->
                onAssignFolder(targetApp, targetFolder?.id)
                appToAssignFolder = null
            },
            onCreateNewFolder = {
                showCreateFolderDialog = true
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DrawerAppItem(
    app: AppItem,
    isExpanded: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onUninstall: () -> Unit,
    onRename: () -> Unit,
    onFolder: () -> Unit,
    onTogglePin: () -> Unit,
    onHide: () -> Unit,
    onInfo: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // App Item Name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onItemClick,
                    onLongClick = onItemLongClick
                )
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = app.displayLabel,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Inline Action Menu (Composed only when item is expanded)
        if (isExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = Color(0xFF141414),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InlineActionItem(icon = Icons.Default.Delete, label = "Uninstall", onClick = onUninstall)
                    InlineActionItem(icon = Icons.Default.Edit, label = "Rename", onClick = onRename)
                    InlineActionItem(icon = Icons.Default.Folder, label = "Folder", onClick = onFolder)
                    InlineActionItem(
                        icon = Icons.Default.PushPin,
                        label = if (app.isPinned) "Unpin" else "Pin",
                        tint = if (app.isPinned) Color(0xFF00FF41) else Color.White,
                        onClick = onTogglePin
                    )
                    InlineActionItem(icon = Icons.Default.VisibilityOff, label = "Hide", onClick = onHide)
                    InlineActionItem(icon = Icons.Default.Info, label = "Info", onClick = onInfo)
                    InlineActionItem(icon = Icons.Default.Close, label = "Close", onClick = onClose)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DrawerFolderItem(
    folder: AppFolder,
    count: Int,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 12.dp),
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "$count",
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
internal fun RowScope.InlineActionItem(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = Color(0xFFCCCCCC),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}
