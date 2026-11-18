package com.fj.launcher.ui.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📁 ${folder.name}",
                                color = Color.White
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderContentSheet(
    folder: AppFolder,
    appsInFolder: List<AppItem>,
    folders: List<AppFolder> = emptyList(),
    onDismiss: () -> Unit,
    onAppClick: (AppItem) -> Unit,
    onAppLongClick: (AppItem) -> Unit = {},
    onAssignFolder: (AppItem, String?) -> Unit = { _, _ -> },
    onCreateFolder: (AppFolder) -> Unit = {},
    onDeleteFolder: (AppFolder) -> Unit
) {
    var appToManage by remember { mutableStateOf<AppItem?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Monochrome B/W Folder title
                Text(
                    text = "📁 ${folder.name}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(onClick = { onDeleteFolder(folder); onDismiss() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Folder",
                        tint = Color.White
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFF242424),
                modifier = Modifier.padding(vertical = 12.dp)
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(appsInFolder, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onAppClick(app) },
                                    onLongClick = { appToManage = app }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.displayLabel,
                                color = Color.White,
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { appToManage = app }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                    contentDescription = "Pindah / Keluarkan",
                                    tint = Color(0xFFAAAAAA),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Dialog to Move or Remove app from folder
    appToManage?.let { targetApp ->
        AssignFolderDialog(
            app = targetApp,
            folders = folders,
            onDismiss = { appToManage = null },
            onAssign = { newFolder ->
                onAssignFolder(targetApp, newFolder?.id)
                appToManage = null
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
                appToManage?.let { targetApp ->
                    onAssignFolder(targetApp, newFolder.id)
                    appToManage = null
                }
            }
        )
    }
}
