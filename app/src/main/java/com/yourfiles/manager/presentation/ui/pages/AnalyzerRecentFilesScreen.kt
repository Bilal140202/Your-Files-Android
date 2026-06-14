package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ────────────────────────────────────────────────────────────────────────────────
// Helper
// ────────────────────────────────────────────────────────────────────────────────

private fun timeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hr ago"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(java.util.Date(timestamp))
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Main screen
// ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerRecentFilesScreen() {
    val context = LocalContext.current
    val navController = remember { App.instance.navController() }
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(true) }
    var recentFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedModeOn by remember { mutableStateOf(false) }
    val selectedFiles = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteResultMessage by remember { mutableStateOf<String?>(null) }

    // Scan on launch
    LaunchedEffect(Unit) {
        val files = withContext(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val rootPath = root.absolutePath
            val sevenDaysMs = 7L * 24 * 3600_000L
            val now = System.currentTimeMillis()
            val result = mutableListOf<File>()

            fun walk(dir: File, depth: Int) {
                if (depth > 20) return // safety limit
                val children = dir.listFiles() ?: return
                for (child in children) {
                    if (child.isFile) {
                        val age = now - child.lastModified()
                        if (age < sevenDaysMs && age >= 0) {
                            result.add(child)
                        }
                    } else if (child.isDirectory && !child.name.startsWith(".")) {
                        walk(child, depth + 1)
                    }
                }
            }

            walk(root, 0)

            // Sort by lastModified descending (newest first)
            result.sortedByDescending { it.lastModified() }
        }

        recentFiles = files
        isScanning = false
    }

    // Delete logic
    fun performDelete() {
        showDeleteDialog = false
        isDeleting = true
        val toDelete = selectedFiles.toList()
        scope.launch(Dispatchers.IO) {
            var deletedCount = 0
            for (path in toDelete) {
                val file = File(path)
                if (file.delete()) {
                    deletedCount++
                }
            }
            withContext(Dispatchers.Main) {
                recentFiles = recentFiles.filter { it.absolutePath !in toDelete }
                selectedFiles.clear()
                selectedModeOn = false
                isDeleting = false
                deleteResultMessage = "Deleted $deletedCount file${if (deletedCount != 1) "s" else ""}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedModeOn) "${selectedFiles.size} selected"
                        else "Recently Created",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedModeOn) {
                            selectedModeOn = false
                            selectedFiles.clear()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!selectedModeOn && !isScanning && recentFiles.isNotEmpty()) {
                        TextButton(onClick = { selectedModeOn = true }) {
                            Text("Select")
                        }
                    } else if (selectedModeOn) {
                        TextButton(onClick = {
                            if (recentFiles.all { it.absolutePath in selectedFiles }) {
                                selectedFiles.clear()
                            } else {
                                selectedFiles.clear()
                                selectedFiles.addAll(recentFiles.map { it.absolutePath })
                            }
                        }) {
                            Text(
                                if (recentFiles.all { it.absolutePath in selectedFiles }) "Deselect All"
                                else "Select All"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            if (selectedModeOn && selectedFiles.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            if (isDeleting) "Deleting..."
                            else "DELETE ${selectedFiles.size} FILE${if (selectedFiles.size != 1) "S" else ""}",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isScanning -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Scanning recent files...", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                recentFiles.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No recent files found",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Files modified in the last 7 days will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    val rootPath = Environment.getExternalStorageDirectory().absolutePath
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Summary
                        item {
                            Text(
                                "Found ${recentFiles.size} files modified in the last 7 days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        items(recentFiles, key = { it.absolutePath }) { file ->
                            RecentFileRow(
                                file = file,
                                rootPath = rootPath,
                                isSelected = file.absolutePath in selectedFiles,
                                selectionMode = selectedModeOn,
                                onLongPress = {
                                    if (!selectedModeOn) {
                                        selectedModeOn = true
                                        selectedFiles.add(file.absolutePath)
                                    }
                                },
                                onClick = {
                                    if (selectedModeOn) {
                                        if (file.absolutePath in selectedFiles) {
                                            selectedFiles.remove(file.absolutePath)
                                        } else {
                                            selectedFiles.add(file.absolutePath)
                                        }
                                    }
                                },
                            )
                        }

                        // Bottom spacer
                        item { Spacer(Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Files") },
            text = {
                Text("Permanently delete ${selectedFiles.size} file${if (selectedFiles.size != 1) "s" else ""}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = { performDelete() }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete result dialog
    deleteResultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { deleteResultMessage = null },
            title = { Text("Delete Complete") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { deleteResultMessage = null }) {
                    Text("OK")
                }
            },
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// File row
// ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentFileRow(
    file: File,
    rootPath: String,
    isSelected: Boolean,
    selectionMode: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val relativePath = file.absolutePath
        .removePrefix("$rootPath/")
        .removePrefix("$rootPath")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox in selection mode
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                )
                Spacer(Modifier.width(4.dp))
            }

            // File icon
            Icon(
                Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.width(12.dp))

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        relativePath,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }

            // Size + time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Formatter.formatShortFileSize(context, file.length()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    timeAgo(file.lastModified()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = if (selectionMode) 58.dp else 16.dp))
    }
}