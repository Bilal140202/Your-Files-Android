package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ── Data classes ────────────────────────────────────────────────────────────────

data class DupGroup(
    val hash: String,
    val size: Long,
    val files: List<DupFile>,
)

data class DupFile(
    val path: String,
    val lastModified: Long,
)

// ── MD5 helper ─────────────────────────────────────────────────────────────────

private fun md5(file: File): String {
    val digest = java.security.MessageDigest.getInstance("MD5")
    file.inputStream().use { stream ->
        val buffer = ByteArray(8192)
        var read: Int
        while (stream.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

// ── Screen ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerDuplicatesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val selectedPaths = remember { mutableStateListOf<String>() }
    var dupGroups by remember { mutableStateOf<List<DupGroup>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var scannedCount by remember { mutableStateOf(0) }
    var groupCount by remember { mutableStateOf(0) }
    var showSmartMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // ── Scan on launch ─────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        isScanning = true
        scannedCount = 0
        groupCount = 0

        withContext(Dispatchers.IO) {
            val maxSize = 50L * 1024 * 1024 // 50 MB skip threshold
            val hashMap = mutableMapOf<String, MutableList<DupFile>>()

            fun walk(dir: File) {
                val children = dir.listFiles() ?: return
                for (child in children) {
                    if (child.isDirectory) {
                        if (!child.name.startsWith(".")) {
                            walk(child)
                        }
                    } else if (child.isFile && child.length() <= maxSize && child.canRead()) {
                        try {
                            val hash = md5(child)
                            hashMap.getOrPut(hash) { mutableListOf() }
                                .add(DupFile(child.absolutePath, child.lastModified()))
                            scannedCount++
                        } catch (_: Exception) {
                            // Skip unreadable / corrupt files
                        }
                    }
                }
            }

            walk(Environment.getExternalStorageDirectory())

            val groups = hashMap
                .filter { it.value.size >= 2 }
                .map { (hash, files) ->
                    DupGroup(
                        hash = hash,
                        size = File(files.first().path).length(),
                        files = files,
                    )
                }
                .sortedByDescending { it.size * (it.files.size - 1) }

            groupCount = groups.size

            withContext(Dispatchers.Main) {
                dupGroups = groups
                isScanning = false
            }
        }
    }

    // ── Smart select logic ─────────────────────────────────────────────────────
    val applySmartSelect: (strategy: String) -> Unit = { strategy ->
        selectedPaths.clear()
        for (group in dupGroups) {
            when (strategy) {
                "newest" -> {
                    val newest = group.files.maxByOrNull { it.lastModified }!!
                    for (f in group.files) {
                        if (f.path != newest.path) selectedPaths.add(f.path)
                    }
                }
                "oldest" -> {
                    val oldest = group.files.minByOrNull { it.lastModified }!!
                    for (f in group.files) {
                        if (f.path != oldest.path) selectedPaths.add(f.path)
                    }
                }
                "shortest" -> {
                    val shortest = group.files.minByOrNull { it.path.length }!!
                    for (f in group.files) {
                        if (f.path != shortest.path) selectedPaths.add(f.path)
                    }
                }
            }
        }
    }

    // ── Delete logic ───────────────────────────────────────────────────────────
    val totalWasted = remember(dupGroups, selectedPaths) {
        val pathSet = selectedPaths.toSet()
        dupGroups.sumOf { group ->
            if (pathSet.any { it in group.files.map { f -> f.path } }) {
                group.size * (selectedPaths.count { it in group.files.map { f -> f.path } })
            } else 0L
        }
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
                // Refresh groups: remove deleted files
                dupGroups = dupGroups
                    .map { group ->
                        group.copy(
                            files = group.files.filter { File(it.path).exists() }
                        )
                    }
                    .filter { it.files.size >= 2 }
                    .sortedByDescending { it.size * (it.files.size - 1) }
            }
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Duplicate Files") },
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
                        text = "${selectedPaths.size} selected",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
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
            when {
                // ── Scanning ────────────────────────────────────────────────────
                isScanning -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Scanning... $scannedCount files hashed, $groupCount groups found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Empty result ────────────────────────────────────────────────
                dupGroups.isEmpty() && !isScanning -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "No duplicate files found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "All files on your device are unique",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // ── Results ─────────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // ── Smart select bar ────────────────────────────────────
                        item(key = "smart_select_bar") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "${dupGroups.size} duplicate group${if (dupGroups.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Box {
                                    TextButton(onClick = { showSmartMenu = true }) {
                                        Text("Smart Select")
                                    }
                                    DropdownMenu(
                                        expanded = showSmartMenu,
                                        onDismissRequest = { showSmartMenu = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Keep Newest") },
                                            onClick = {
                                                applySmartSelect("newest")
                                                showSmartMenu = false
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Keep Oldest") },
                                            onClick = {
                                                applySmartSelect("oldest")
                                                showSmartMenu = false
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Keep Shortest Path") },
                                            onClick = {
                                                applySmartSelect("shortest")
                                                showSmartMenu = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        // ── Group cards ────────────────────────────────────────
                        items(
                            items = dupGroups,
                            key = { it.hash },
                        ) { group ->
                            DupGroupCard(
                                group = group,
                                context = context,
                                selectedPaths = selectedPaths,
                            )
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
                    "Permanently delete ${selectedPaths.size} duplicate file${if (selectedPaths.size != 1) "s" else ""} " +
                        "(${Formatter.formatShortFileSize(context, totalWasted)})?\n\nThis action cannot be undone."
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

// ── Group card composable ──────────────────────────────────────────────────────

@Composable
private fun DupGroupCard(
    group: DupGroup,
    context: android.content.Context,
    selectedPaths: MutableList<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = 8.dp)) {
            // ── Group header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = Formatter.formatShortFileSize(context, group.size),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${group.files.size} copies",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = group.hash.take(16) + "\u2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                Text(
                    text = "Wasted: ${Formatter.formatShortFileSize(context, group.size * (group.files.size - 1))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── File list ──────────────────────────────────────────────────────
            group.files.forEach { file ->
                val isSelected = selectedPaths.contains(file.path)
                val fileObj = File(file.path)
                val parentName = fileObj.parentFile?.name ?: "unknown"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (file.path !in selectedPaths) selectedPaths.add(file.path)
                            } else {
                                selectedPaths.remove(file.path)
                            }
                        },
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileObj.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "($parentName)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}