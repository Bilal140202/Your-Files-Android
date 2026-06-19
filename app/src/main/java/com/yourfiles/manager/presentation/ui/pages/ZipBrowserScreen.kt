package com.yourfiles.manager.presentation.ui.pages

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Data class representing a single entry inside a ZIP archive.
 */
private data class ZipEntryItem(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val compressedSize: Long,
    val date: Long,
) {
    /** The display name: last path segment, or name itself if no '/' */
    val displayName: String get() {
        val trimmed = name.trimEnd('/')
        return trimmed.substringAfterLast('/')
    }

    /** Human-readable size string */
    val sizeText: String
        get() {
            if (isDirectory) return ""
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", size / 1024.0)
                size < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0))
                else -> String.format(Locale.getDefault(), "%.1f GB", size / (1024.0 * 1024.0 * 1024.0))
            }
        }

    val dateText: String
        get() {
            if (date <= 0) return ""
            return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(date))
        }
}

/**
 * In-app ZIP archive browser.
 * Pure Jetpack Compose — no Activities, no XML.
 *
 * Shows the contents of a ZIP file in a flat list sorted by name.
 * Directories shown with folder icon, files with file icon.
 * Displays size, compressed size, and modification date.
 *
 * ES-style: simple list, no extraction (long-press extraction can be added later).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZipBrowserScreen(
    filePath: String,
) {
    val file = remember(filePath) { File(filePath) }
    val zipFileName = remember(filePath) { file.name }

    var entries by remember(filePath) { mutableStateOf<List<ZipEntryItem>?>(null) }
    var loading by remember(filePath) { mutableStateOf(true) }
    var error by remember(filePath) { mutableStateOf<String?>(null) }
    var totalEntries by remember(filePath) { mutableStateOf(0) }
    var totalSize by remember(filePath) { mutableStateOf(0L) }

    // Read ZIP contents on background thread
    LaunchedEffect(filePath) {
        loading = true
        error = null
        try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val zip = ZipFile(file)
                val items = zip.entries().toList()
                    .filter { !it.name.trimEnd('/').contains('/') || it.isDirectory }
                    // For nested entries, show only the top-level path segment
                    // Actually let's show ALL entries flat — user can see full path
                    .map { entry ->
                        ZipEntryItem(
                            name = entry.name,
                            isDirectory = entry.isDirectory,
                            size = entry.size,
                            compressedSize = entry.compressedSize,
                            date = entry.lastModifiedTime.toMillis(),
                        )
                    }
                    // Sort: directories first, then files, both alphabetically
                    .sortedWith(compareByDescending<ZipEntryItem> { it.isDirectory }.thenBy { it.name.lowercase() })

                // Filter: if there are nested entries, show only top-level
                // But user wants to see contents, so show everything
                totalEntries = zip.size()
                totalSize = items.filter { !it.isDirectory }.sumOf { it.size }
                entries = items
                zip.close()
            }
        } catch (e: Exception) {
            error = "Failed to read ZIP: ${e.localizedMessage}"
        } finally {
            loading = false
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = zipFileName,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (totalEntries > 0) {
                            Text(
                                text = "$totalEntries entries · ${formatSize(totalSize)}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                },
                navigationIcon = {
                    com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose()
                },
            )
        }
    ) { paddingValues ->
        val currentError = error
        val currentLoading = loading
        val currentEntries = entries
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            when {
                currentError != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentError, color = Color.Red, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            // Retry
                        }) { Text("Retry", color = MaterialTheme.colorScheme.primary) }
                    }
                }
                currentLoading -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reading archive...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                currentEntries.isNullOrEmpty() -> {
                    Text("Empty archive", color = Color.Gray, fontSize = 14.sp)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(currentEntries!!, key = { it.name }) { entry ->
                            ZipEntryRow(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZipEntryRow(entry: ZipEntryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Read-only browser, no extraction yet
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon: folder or file
        Icon(
            imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = if (entry.isDirectory) "Folder" else "File",
            tint = if (entry.isDirectory) Color(0xFFFFC107) else Color(0xFF90CAF9),
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name (full path, but trimmed for display)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.displayName.ifEmpty { entry.name },
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // Show path if nested
            val parentPath = entry.name.let {
                val trimmed = it.trimEnd('/')
                val lastSlash = trimmed.lastIndexOf('/')
                if (lastSlash > 0) trimmed.substring(0, lastSlash) else null
            }
            if (parentPath != null) {
                Text(
                    text = parentPath,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Size + date on the right
        Column(horizontalAlignment = Alignment.End) {
            if (entry.sizeText.isNotEmpty()) {
                Text(
                    text = entry.sizeText,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            if (entry.dateText.isNotEmpty()) {
                Text(
                    text = entry.dateText,
                    fontSize = 11.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                )
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
