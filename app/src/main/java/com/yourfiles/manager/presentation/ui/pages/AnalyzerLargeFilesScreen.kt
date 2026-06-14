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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ── Data class for a large file entry ──────────────────────────────────────────

private data class LargeFileEntry(
    val path: String,
    val size: Long,
)

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnalyzerLargeFilesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedPaths = remember { mutableStateListOf<String>() }
    var largeFiles by remember { mutableStateOf<List<LargeFileEntry>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // ── Threshold state ────────────────────────────────────────────────────────
    var thresholdText by remember { mutableStateOf("10") }
    var thresholdUnit by remember { mutableStateOf("MB") }
    var showUnitMenu by remember { mutableStateOf(false) }

    val thresholdBytes = remember(thresholdText, thresholdUnit) {
        val value = thresholdText.toLongOrNull() ?: 0L
        value * if (thresholdUnit == "MB") 1_000_000L else 1_000_000_000L
    }

    // ── Scan logic ─────────────────────────────────────────────────────────────
    fun startScan() {
        if (isScanning) return
        isScanning = true
        hasScanned = false
        selectedPaths.clear()
        largeFiles = emptyList()

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val threshold = thresholdBytes
                val found = mutableListOf<LargeFileEntry>()

                fun walk(dir: File) {
                    val children = dir.listFiles() ?: return
                    for (child in children) {
                        if (child.isDirectory) {
                            if (!child.name.startsWith(".")) {
                                walk(child)
                            }
                        } else if (child.isFile && child.canRead() && child.length() >= threshold) {
                            found.add(
                                LargeFileEntry(
                                    path = child.absolutePath,
                                    size = child.length(),
                                )
                            )
                        }
                    }
                }

                walk(Environment.getExternalStorageDirectory())

                found.sortedByDescending { it.size }
            }

            largeFiles = result
            isScanning = false
            hasScanned = true
        }
    }

    // ── Delete logic ───────────────────────────────────────────────────────────
    val selectedTotalSize = remember(largeFiles, selectedPaths) {
        val pathSet = selectedPaths.toSet()
        largeFiles.filter { it.path in pathSet }.sumOf { it.size }
    }

    LaunchedEffect(isDeleting) {
        if (isDeleting) {
            withContext(Dispatchers.IO) {
                for (path in selectedPaths.toList()) {
                    try { File(path).delete() } catch (_: Exception) { }
                }
            }
            withContext(Dispatchers.Main) {
                selectedPaths.clear()
                isDeleting = false
                largeFiles = largeFiles.filter { File(it.path).exists() }
            }
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Large Files") },
                navigationIcon = {
                    IconButton(onClick = { App.instance.navController().navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            if (selectedPaths.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${selectedPaths.size} selected, ${Formatter.formatShortFileSize(context, selectedTotalSize)} total",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        enabled = !isDeleting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Selected")
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Threshold selector ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it.filter { c -> c.isDigit() } },
                        label = { Text("Min size") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        TextButton(onClick = { showUnitMenu = true }) {
                            Text(thresholdUnit)
                        }
                        DropdownMenu(
                            expanded = showUnitMenu,
                            onDismissRequest = { showUnitMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("MB") },
                                onClick = {
                                    thresholdUnit = "MB"
                                    showUnitMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("GB") },
                                onClick = {
                                    thresholdUnit = "GB"
                                    showUnitMenu = false
                                },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { startScan() },
                        enabled = !isScanning && thresholdText.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Scan")
                    }
                }

                // ── Content area ─────────────────────────────────────────────────
                when {
                    // Scanning
                    isScanning -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Scanning for large files\u2026",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Not yet scanned
                    !hasScanned -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "Set a minimum file size and tap Scan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Empty result
                    largeFiles.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No large files found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try lowering the minimum size threshold",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Results list
                    else -> {
                        val totalSize = largeFiles.sumOf { it.size }

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Summary
                            Text(
                                text = "Found ${largeFiles.size} large file${if (largeFiles.size != 1) "s" else ""} " +
                                    "(${Formatter.formatShortFileSize(context, totalSize)} total)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 4.dp,
                                ),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                items(
                                    items = largeFiles,
                                    key = { it.path },
                                ) { entry ->
                                    LargeFileRow(
                                        entry = entry,
                                        context = context,
                                        isSelected = selectedPaths.contains(entry.path),
                                        onToggle = {
                                            if (it) {
                                                if (entry.path !in selectedPaths) {
                                                    selectedPaths.add(entry.path)
                                                }
                                            } else {
                                                selectedPaths.remove(entry.path)
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Delete confirmation dialog ─────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Selected Files") },
            text = {
                Text(
                    "Permanently delete ${selectedPaths.size} file${if (selectedPaths.size != 1) "s" else ""} " +
                        "(${Formatter.formatShortFileSize(context, selectedTotalSize)})?\n\nThis action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    isDeleting = true
                }) {
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

    // ── Deleting progress dialog ───────────────────────────────────────────────
    if (isDeleting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Deleting\u2026") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Permanently deleting ${selectedPaths.size} file${if (selectedPaths.size != 1) "s" else ""}")
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = { },
        )
    }
}

// ── File row composable ────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LargeFileRow(
    entry: LargeFileEntry,
    context: android.content.Context,
    isSelected: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val fileObj = File(entry.path)
    val isVeryLarge = entry.size > 100L * 1024 * 1024 // > 100 MB
    val sizeColor = if (isVeryLarge) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onToggle(!isSelected) },
                onLongClick = { onToggle(!isSelected) },
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = onToggle,
        )

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileObj.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = fileObj.parent ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = Formatter.formatShortFileSize(context, entry.size),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = sizeColor,
        )
    }
}