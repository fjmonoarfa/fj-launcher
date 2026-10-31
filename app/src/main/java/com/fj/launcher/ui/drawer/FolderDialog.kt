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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fj.launcher.model.AppFolder
import com.fj.launcher.model.AppItem
import java.util.UUID

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (AppFolder) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Folder Baru", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Nama Folder") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderName.isNotBlank()) {
                        val newFolder = AppFolder(
                            id = UUID.randomUUID().toString(),
                            name = folderName.trim(),
                            colorHex = "#FFFFFF"
                        )
                        onConfirm(newFolder)
                    }
                }
            ) {
                Text("Buat", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@Composable
fun MoveToFolderDialog(
    app: AppItem,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onAssignFolder: (AppItem, String?) -> Unit,
    onCreateFolder: (AppFolder) -> Unit
) {
    var showInlineCreateFolder by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Pindahkan ke Folder", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(app.displayLabel, fontSize = 13.sp, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // If the app is currently in a folder: Option to remove it
                if (app.folderId != null) {
                    TextButton(
                        onClick = {
                            onAssignFolder(app, null)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.FolderOff, contentDescription = null, tint = Color(0xFFFF5252))
                            Text("Keluarkan dari folder ini", fontWeight = FontWeight.Medium)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF262626))
                }

                // Existing folders
                if (folders.isNotEmpty()) {
                    Text(
                        text = "Pilih folder tujuan:",
                        fontSize = 12.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    folders.forEach { folder ->
                        val isSelected = app.folderId == folder.id
                        TextButton(
                            onClick = {
                                onAssignFolder(app, folder.id)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = folder.name,
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Saat ini dipilih",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0xFF262626))
                } else if (app.folderId == null) {
                    Text(
                        text = "Belum ada folder yang dibuat.",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }

                // Create new folder option
                if (!showInlineCreateFolder) {
                    TextButton(
                        onClick = { showInlineCreateFolder = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = Color.White)
                            Text("+ Buat Folder Baru", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text("Nama Folder Baru") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showInlineCreateFolder = false }) {
                                Text("Batal", color = Color.Gray)
                            }
                            TextButton(
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        val newFolder = AppFolder(
                                            id = UUID.randomUUID().toString(),
                                            name = newFolderName.trim(),
                                            colorHex = "#FFFFFF"
                                        )
                                        onCreateFolder(newFolder)
                                        onAssignFolder(app, newFolder.id)
                                        onDismiss()
                                    }
                                }
                            ) {
                                Text("Buat & Pindahkan", color = Color.White)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.Gray)
            }
        }
    )
}

@Composable
fun FolderOptionsDialog(
    folder: AppFolder,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showRenameInput by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf(folder.name) }

    when {
        showDeleteConfirm -> {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Hapus Folder?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Text(
                        "Folder '${folder.name}' akan dihapus. Aplikasi di dalamnya tidak akan terhapus, hanya dikeluarkan ke daftar drawer utama.",
                        color = Color(0xFFCCCCCC)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text("Hapus", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }
        showRenameInput -> {
            AlertDialog(
                onDismissRequest = { showRenameInput = false },
                title = { Text("Rename Folder", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Nama Folder") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                onRename(newFolderName.trim())
                                showRenameInput = false
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Simpan", color = Color(0xFF00FF41))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameInput = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }
        else -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White)
                        Text(folder.name, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = { showRenameInput = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                                Text("Ganti Nama (Rename)", color = Color.White)
                            }
                        }

                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5252))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                                Text("Hapus Folder", color = Color(0xFFFF5252))
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Tutup", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderContentSheet(
    folder: AppFolder,
    appsInFolder: List<AppItem>,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onTogglePin: (AppItem) -> Unit,
    onRenameApp: (AppItem, String) -> Unit,
    onHideApp: (AppItem) -> Unit,
    onOpenInfo: (AppItem) -> Unit,
    onUninstall: (AppItem) -> Unit,
    onRemoveFromFolder: (AppItem) -> Unit,
    onAssignFolder: (AppItem, String?) -> Unit,
    onCreateFolder: (AppFolder) -> Unit,
    onDeleteFolder: (AppFolder) -> Unit,
    onRenameFolder: (AppFolder, String) -> Unit
) {
    var expandedAppPackage by remember { mutableStateOf<String?>(null) }
    var appToRename by remember { mutableStateOf<AppItem?>(null) }
    var appForMoveFolder by remember { mutableStateOf<AppItem?>(null) }
    var showFolderOptionsDialog by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xF5080808),
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(3.dp)
                    .background(Color(0xFFCCCCCC), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Folder Header: Clean text with folder icon and count, NO permanent delete icon!
            // Folder options (Rename/Hapus) available via long click
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { /* Keep open */ },
                        onLongClick = { showFolderOptionsDialog = true }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = folder.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${appsInFolder.size}",
                    color = Color(0xFF777777),
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (appsInFolder.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Folder ini kosong.\nBuka drawer, tahan lama aplikasi, lalu pilih 'Folder' untuk memindahkannya ke sini.",
                        color = Color(0xFF777777),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 36.dp)
                ) {
                    items(appsInFolder, key = { it.packageName }) { app ->
                        val isExpanded = expandedAppPackage == app.packageName

                        Column(modifier = Modifier.fillMaxWidth()) {
                            // App Item Name: Identical to regular drawer menu (22.sp, white, padding 15.dp, no persistent icons)
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

                            // INLINE APP OPTIONS (Revealed on long click)
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

                                    // 4. Keluarkan dari Folder
                                    InlineActionItem(
                                        icon = Icons.Default.FolderOff,
                                        label = "Keluarkan",
                                        tint = Color(0xFFFF7777),
                                        onClick = {
                                            expandedAppPackage = null
                                            onRemoveFromFolder(app)
                                        }
                                    )

                                    // 5. Pindah ke Folder Lain
                                    InlineActionItem(
                                        icon = Icons.Default.Folder,
                                        label = "Folder",
                                        tint = Color.White,
                                        onClick = {
                                            expandedAppPackage = null
                                            appForMoveFolder = app
                                        }
                                    )

                                    // 6. Hide
                                    InlineActionItem(
                                        icon = Icons.Default.VisibilityOff,
                                        label = "Hide",
                                        onClick = {
                                            expandedAppPackage = null
                                            onHideApp(app)
                                        }
                                    )

                                    // 7. Info
                                    InlineActionItem(
                                        icon = Icons.Default.Info,
                                        label = "Info",
                                        onClick = {
                                            expandedAppPackage = null
                                            onOpenInfo(app)
                                        }
                                    )

                                    // 8. Tutup
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
    }

    // Dialogs within Folder Content Sheet
    if (showFolderOptionsDialog) {
        FolderOptionsDialog(
            folder = folder,
            onDismiss = { showFolderOptionsDialog = false },
            onRename = { newName ->
                onRenameFolder(folder, newName)
                showFolderOptionsDialog = false
            },
            onDelete = {
                onDeleteFolder(folder)
                showFolderOptionsDialog = false
                onDismiss()
            }
        )
    }

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

    appToRename?.let { app ->
        var newName by remember { mutableStateOf(app.displayLabel) }
        AlertDialog(
            onDismissRequest = { appToRename = null },
            title = { Text("Rename Aplikasi", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}
