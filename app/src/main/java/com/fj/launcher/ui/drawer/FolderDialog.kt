package com.fj.launcher.ui.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
        containerColor = Color(0xFF161616),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Buat Folder Baru", color = Color.White) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("Nama Folder") },
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
                    if (folderName.isNotBlank()) {
                        val newFolder = AppFolder(
                            id = UUID.randomUUID().toString(),
                            name = folderName.trim(),
                            colorHex = "#FFFFFF"
                        )
                        onConfirm(newFolder)
                        onDismiss()
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
fun AssignFolderDialog(
    app: AppItem,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onAssign: (AppFolder?) -> Unit,
    onCreateNewFolder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Atur Folder: ${app.displayLabel}", color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Option 1: Remove from folder
                TextButton(
                    onClick = {
                        onAssign(null)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (app.folderId != null) "Keluarkan dari Folder" else "Tanpa Folder",
                            color = if (app.folderId != null) Color.White else Color(0xFFAAAAAA),
                            fontWeight = if (app.folderId != null) FontWeight.Medium else FontWeight.Normal
                        )
                        if (app.folderId == null) {
                            Text("✓", color = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF262626))

                // Option 2: List of existing folders to assign/move to
                folders.forEach { folder ->
                    val isCurrent = app.folderId == folder.id
                    TextButton(
                        onClick = {
                            onAssign(folder)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = folder.name,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCurrent) {
                                Text("✓ (Aktif)", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF262626))

                // Option 3: Create new folder
                TextButton(
                    onClick = {
                        onDismiss()
                        onCreateNewFolder()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Buat Folder Baru", color = Color.White)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.White)
            }
        }
    )
}

@Composable
fun FolderOptionsDialog(
    folder: AppFolder,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        titleContentColor = Color.White,
        title = {
            Row(
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
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = {
                        onDismiss()
                        onRename()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Ganti Nama",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Ganti Nama Folder", color = Color.White, fontSize = 16.sp)
                    }
                }

                HorizontalDivider(color = Color(0xFF262626))

                TextButton(
                    onClick = {
                        onDismiss()
                        onDelete()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Folder",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Hapus Folder", color = Color(0xFFFF5252), fontSize = 16.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@Composable
fun RenameFolderDialog(
    folder: AppFolder,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(folder.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        titleContentColor = Color.White,
        title = { Text("Ganti Nama Folder", color = Color.White) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nama Folder") },
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
                    if (newName.isNotBlank()) {
                        onConfirm(newName.trim())
                        onDismiss()
                    }
                }
            ) {
                Text("Simpan", color = Color.White)
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
fun DeleteFolderConfirmationDialog(
    folder: AppFolder,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        title = { Text("Hapus Folder?", color = Color.White) },
        text = {
            Text(
                "Folder \"${folder.name}\" akan dihapus. Aplikasi di dalamnya tidak akan terhapus dan akan kembali ke daftar utama.",
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Hapus", color = Color(0xFFFF5252))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.Gray)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderContentSheet(
    folder: AppFolder,
    appsInFolder: List<AppItem>,
    folders: List<AppFolder> = emptyList(),
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onTogglePin: (AppItem) -> Unit = {},
    onRenameApp: (AppItem, String) -> Unit = { _, _ -> },
    onHideApp: (AppItem) -> Unit = {},
    onOpenInfo: (AppItem) -> Unit = {},
    onUninstall: (AppItem) -> Unit = {},
    onAssignFolder: (AppItem, String?) -> Unit = { _, _ -> },
    onCreateFolder: (AppFolder) -> Unit = {},
    onRenameFolder: (AppFolder, String) -> Unit = { _, _ -> },
    onDeleteFolder: (AppFolder) -> Unit
) {
    val currentFolder = remember(folders, folder) {
        folders.find { it.id == folder.id } ?: folder
    }

    var expandedAppPackage by remember { mutableStateOf<String?>(null) }
    var appToRename by remember { mutableStateOf<AppItem?>(null) }
    var appToAssignFolder by remember { mutableStateOf<AppItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    var showFolderOptions by remember { mutableStateOf(false) }
    var showRenameFolderDialog by remember { mutableStateOf(false) }
    var showDeleteFolderConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF141414),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Folder title matching the main drawer style & color, long-click shows rename/delete options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { showFolderOptions = true }
                    )
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = currentFolder.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = currentFolder.name,
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
                        text = "${appsInFolder.size}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFF242424),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (appsInFolder.isEmpty()) {
                Text(
                    text = "Belum ada aplikasi di folder ini.\nTahan lama aplikasi di drawer untuk memasukkannya ke sini.",
                    color = Color(0xFF888888),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(
                        items = appsInFolder,
                        key = { it.packageName },
                        contentType = { "folder_app_item" }
                    ) { app ->
                        val isExpanded = expandedAppPackage == app.packageName
                        DrawerAppItem(
                            app = app,
                            isExpanded = isExpanded,
                            onItemClick = {
                                if (isExpanded) {
                                    expandedAppPackage = null
                                } else {
                                    onDismiss()
                                    onAppClick(app)
                                }
                            },
                            onItemLongClick = {
                                expandedAppPackage = if (isExpanded) null else app.packageName
                            },
                            onUninstall = {
                                expandedAppPackage = null
                                onDismiss()
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
                                onDismiss()
                                onHideApp(app)
                            },
                            onInfo = {
                                expandedAppPackage = null
                                onDismiss()
                                onOpenInfo(app)
                            },
                            onClose = {
                                expandedAppPackage = null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Rename App Dialog
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

    // Folder Options Dialog (Ganti Nama, Hapus Folder)
    if (showFolderOptions) {
        FolderOptionsDialog(
            folder = currentFolder,
            onDismiss = { showFolderOptions = false },
            onRename = { showRenameFolderDialog = true },
            onDelete = { showDeleteFolderConfirm = true }
        )
    }

    // Rename Folder Dialog
    if (showRenameFolderDialog) {
        RenameFolderDialog(
            folder = currentFolder,
            onDismiss = { showRenameFolderDialog = false },
            onConfirm = { newName ->
                onRenameFolder(currentFolder, newName)
            }
        )
    }

    // Delete Folder Confirm Dialog
    if (showDeleteFolderConfirm) {
        DeleteFolderConfirmationDialog(
            folder = currentFolder,
            onDismiss = { showDeleteFolderConfirm = false },
            onConfirm = {
                onDeleteFolder(currentFolder)
                onDismiss()
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

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { newFolder ->
                onCreateFolder(newFolder)
                appToAssignFolder?.let { targetApp ->
                    onAssignFolder(targetApp, newFolder.id)
                    appToAssignFolder = null
                }
            }
        )
    }
}
