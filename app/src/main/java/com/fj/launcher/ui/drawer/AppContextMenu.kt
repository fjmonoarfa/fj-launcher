package com.fj.launcher.ui.drawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContextMenu(
    app: AppItem,
    folders: List<AppFolder>,
    onDismiss: () -> Unit,
    onRename: (AppItem, String) -> Unit,
    onTogglePin: (AppItem) -> Unit,
    onToggleHide: (AppItem) -> Unit,
    onAssignFolder: (AppItem, String?) -> Unit,
    onOpenInfo: (AppItem) -> Unit,
    onUninstall: (AppItem) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showFolderSelectDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161616),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // App Title Header
            Text(
                text = app.displayLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = app.packageName,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider(color = Color(0xFF262626), modifier = Modifier.padding(bottom = 8.dp))

            // Action Items
            MenuActionRow(
                icon = Icons.Default.Edit,
                title = "Rename / Ganti Nama",
                onClick = { showRenameDialog = true }
            )

            MenuActionRow(
                icon = if (app.isPinned) Icons.Default.BookmarkRemove else Icons.Default.BookmarkAdd,
                title = if (app.isPinned) "Hapus dari Home" else "Pin ke Home",
                onClick = {
                    onTogglePin(app)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Default.Folder,
                title = "Atur Folder",
                onClick = { showFolderSelectDialog = true }
            )

            MenuActionRow(
                icon = Icons.Default.VisibilityOff,
                title = "Sembunyikan Aplikasi (Hide)",
                onClick = {
                    onToggleHide(app)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Default.Info,
                title = "Info Aplikasi",
                onClick = {
                    onOpenInfo(app)
                    onDismiss()
                }
            )

            MenuActionRow(
                icon = Icons.Default.Delete,
                title = "Uninstall",
                tint = Color(0xFFFF5252),
                onClick = {
                    onUninstall(app)
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(app.displayLabel) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Aplikasi") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Tampilan") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onRename(app, newName.trim())
                    showRenameDialog = false
                    onDismiss()
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Folder Select Dialog
    if (showFolderSelectDialog) {
        AlertDialog(
            onDismissRequest = { showFolderSelectDialog = false },
            title = { Text("Pilih Folder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onAssignFolder(app, null)
                            showFolderSelectDialog = false
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tanpa Folder (Keluarkan)", color = Color(0xFFAAAAAA))
                    }

                    folders.forEach { folder ->
                        TextButton(
                            onClick = {
                                onAssignFolder(app, folder.id)
                                showFolderSelectDialog = false
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📁 ${folder.name}", color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFolderSelectDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun MenuActionRow(
    icon: ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = title, color = tint, fontSize = 16.sp)
    }
}
