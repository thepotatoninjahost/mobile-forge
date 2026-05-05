package com.mobileforge.ui.files

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobileforge.ui.IDEViewModel
import com.mobileforge.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilePanel(viewModel: IDEViewModel) {
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showNewDirDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<String?>(null) }
    var newItemName by remember { mutableStateOf("") }
    var expandedPaths by remember { mutableStateOf(setOf(viewModel.currentProjectPath)) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showNewFileDialog = true }) {
                Icon(Icons.Default.NoteAdd, "New File", tint = AccentBlue)
            }
            IconButton(onClick = { showNewDirDialog = true }) {
                Icon(Icons.Default.CreateNewFolder, "New Folder", tint = AccentGreen)
            }
            IconButton(onClick = { viewModel.loadProject() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = Color(0xFF888888))
            }
            Spacer(Modifier.weight(1f))
            Text(
                viewModel.currentProjectPath.substringAfterLast("/"),
                color = Color(0xFF888888),
                fontSize = 12.sp
            )
        }

        // Directory tree
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            items(viewModel.files) { file ->
                FileTreeItem(
                    file = file,
                    depth = 0,
                    expandedPaths = expandedPaths,
                    onToggle = { path ->
                        expandedPaths = if (path in expandedPaths) {
                            expandedPaths - path
                        } else {
                            expandedPaths + path
                        }
                    },
                    onOpen = { viewModel.openFile(it) },
                    onDelete = { selectedItem = it.path; showRenameDialog = true },
                    onRename = { selectedItem = it.path; showRenameDialog = true }
                )
            }
        }
    }

    // New file dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("New File") },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("File name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createFile(newItemName)
                    newItemName = ""
                    showNewFileDialog = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // New directory dialog
    if (showNewDirDialog) {
        AlertDialog(
            onDismissRequest = { showNewDirDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createDirectory(newItemName)
                    newItemName = ""
                    showNewDirDialog = false
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDirDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete confirmation
    if (selectedItem != null && !showRenameDialog) {
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text("Delete?") },
            text = { Text("Delete ${selectedItem?.substringAfterLast("/')}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedItem?.let { viewModel.deleteItem(it) }
                    selectedItem = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { selectedItem = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FileTreeItem(
    file: com.mobileforge.ui.FileItem,
    depth: Int,
    expandedPaths: Set<String>,
    onToggle: (String) -> Unit,
    onOpen: (com.mobileforge.ui.FileItem) -> Unit,
    onDelete: (com.mobileforge.ui.FileItem) -> Unit,
    onRename: (com.mobileforge.ui.FileItem) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val isExpanded = file.path in expandedPaths

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (file.isDirectory) {
                    onToggle(file.path)
                } else {
                    onOpen(file)
                }
            }
            .padding(start = (depth * 20).dp, top = 4.dp, bottom = 4.dp)
    ) {
        if (file.isDirectory) {
            Icon(
                if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                "Expand", 16.dp, tint = Color(0xFF888888),
                modifier = Modifier.clickable { onToggle(file.path) }
            )
            Icon(
                if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                "Folder", 18.dp, tint = AccentYellow
            )
        } else {
            Spacer(Modifier.width(24.dp))
            Icon(
                getFileIcon(file.name),
                "File", 18.dp, tint = getFileColor(file.name)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            file.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Monospace
        )
        Spacer(Modifier.weight(1f))
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.MoreVert, "Menu", 16.dp, tint = Color(0xFF666666))
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = { showMenu = false; onRename(file) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDelete(file) }
                )
            }
        }
    }
}

fun getFileIcon(name: String) = when {
    name.endsWith(".kt") || name.endsWith(".java") -> Icons.Default.Code
    name.endsWith(".xml") -> Icons.Default.DataObject
    name.endsWith(".json") -> Icons.Default.DataArray
    name.endsWith(".gradle") -> Icons.Default.Settings
    name.endsWith(".md") -> Icons.Default.Article
    name.endsWith(".sh") -> Icons.Default.Terminal
    name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".svg") -> Icons.Default.Image
    name.endsWith(".zip") || name.endsWith(".tar") -> Icons.Default.Archive
    else -> Icons.Default.InsertDriveFile
}

fun getFileColor(name: String) = when {
    name.endsWith(".kt") -> AccentBlue
    name.endsWith(".java") -> Color(0xFFB07219)
    name.endsWith(".xml") -> AccentGreen
    name.endsWith(".json") -> AccentYellow
    name.endsWith(".gradle") -> AccentPurple
    name.endsWith(".sh") -> AccentGreen
    name.endsWith(".md") -> Color(0xFF888888)
    else -> Color(0xFF888888)
}