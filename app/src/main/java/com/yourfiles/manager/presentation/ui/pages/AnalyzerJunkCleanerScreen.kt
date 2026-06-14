package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
// Data model
// ────────────────────────────────────────────────────────────────────────────────

data class JunkCategory(
    val key: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val files: List<File>,
)

// ────────────────────────────────────────────────────────────────────────────────
// Main screen
// ────────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerJunkCleanerScreen() {
    val context = LocalContext.current
    val navController = remember { App.instance.navController() }
    val scope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(true) }
    var junkCategories by remember { mutableStateOf<List<JunkCategory>>(emptyList()) }
    val checkedCategories = remember { mutableStateListOf<String>() }
    var isCleaning by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    // Scan on launch
    LaunchedEffect(Unit) {
        val categories = withContext(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val results = mutableListOf<JunkCategory>()

            // 1. Obsolete APKs
            val apkDir = File(root, "Download")
            val apks = apkDir.listFiles { file -> file.isFile && file.extension.equals("apk", ignoreCase = true) }
                ?.toList() ?: emptyList()
            results.add(
                JunkCategory(
                    key = "obsolete_apks",
                    label = "Obsolete APKs",
                    description = "APK files in Downloads",
                    icon = Icons.Outlined.Memory,
                    color = Color(0xFF4CAF50),
                    files = apks,
                )
            )

            // 2. Thumbnails
            val thumbDirs = listOf(
                File(root, "DCIM/.thumbnails"),
                File(root, "Pictures/.thumbnails"),
            )
            val thumbnails = thumbDirs.flatMap { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.toList() ?: emptyList()
                } else emptyList()
            }
            results.add(
                JunkCategory(
                    key = "thumbnails",
                    label = "Thumbnails",
                    description = "Cached thumbnail files",
                    icon = Icons.Outlined.Image,
                    color = Color(0xFFE91E63),
                    files = thumbnails,
                )
            )

            // 3. Temp Files (.tmp, .log in root and Download)
            val tempDirs = listOf(root, File(root, "Download"))
            val tempFiles = tempDirs.flatMap { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles { file ->
                        file.isFile && (
                            file.extension.equals("tmp", ignoreCase = true) ||
                                file.extension.equals("log", ignoreCase = true)
                            )
                    }?.toList() ?: emptyList()
                } else emptyList()
            }
            results.add(
                JunkCategory(
                    key = "temp_files",
                    label = "Temp Files",
                    description = "Temporary and log files",
                    icon = Icons.Outlined.Description,
                    color = Color(0xFFFF9800),
                    files = tempFiles,
                )
            )

            // 4. Empty Folders (skip hidden, max depth 3)
            val emptyFolders = mutableListOf<File>()
            fun findEmptyDirs(dir: File, depth: Int) {
                if (depth > 3) return
                val children = dir.listFiles() ?: return
                if (children.isEmpty()) {
                    emptyFolders.add(dir)
                } else {
                    children
                        .filter { it.isDirectory && !it.name.startsWith(".") }
                        .forEach { findEmptyDirs(it, depth + 1) }
                }
            }
            root.listFiles()
                ?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.forEach { findEmptyDirs(it, 1) }
            results.add(
                JunkCategory(
                    key = "empty_folders",
                    label = "Empty Folders",
                    description = "Folders with no contents",
                    icon = Icons.Outlined.Folder,
                    color = Color(0xFF9E9E9E),
                    files = emptyFolders,
                )
            )

            results.filter { it.files.isNotEmpty() }
        }

        junkCategories = categories
        checkedCategories.clear()
        checkedCategories.addAll(categories.map { it.key })
        isScanning = false
    }

    // Compute totals for checked items
    val checkedCats = junkCategories.filter { it.key in checkedCategories }
    val totalFileCount = checkedCats.sumOf { it.files.size }
    val totalSize = checkedCats.sumOf { cat ->
        if (cat.key == "empty_folders") 0L
        else cat.files.sumOf { it.length() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Junk Cleaner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            if (!isScanning && junkCategories.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Button(
                        onClick = {
                            isCleaning = true
                            scope.launch(Dispatchers.IO) {
                                var deletedCount = 0
                                var freedBytes = 0L
                                for (cat in junkCategories) {
                                    if (cat.key !in checkedCategories) continue
                                    for (file in cat.files) {
                                        val success = if (file.isDirectory) {
                                            file.deleteRecursively()
                                        } else {
                                            file.delete()
                                        }
                                        if (success) {
                                            deletedCount++
                                            if (!file.isDirectory) {
                                                freedBytes += file.length()
                                            }
                                        }
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    isCleaning = false
                                    resultMessage =
                                        "Cleaned $deletedCount files, freed ${
                                            Formatter.formatShortFileSize(
                                                context,
                                                freedBytes,
                                            )
                                        }"
                                    // Refresh: remove cleaned categories
                                    junkCategories = junkCategories.filter { cat ->
                                        val remaining = if (cat.key == "empty_folders") {
                                            cat.files.any { f -> f.exists() && f.isDirectory && (f.listFiles()?.isEmpty() == true) }
                                        } else {
                                            cat.files.any { it.exists() }
                                        }
                                        if (!remaining) {
                                            checkedCategories.remove(cat.key)
                                        }
                                        remaining
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        enabled = !isCleaning && totalFileCount > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            if (isCleaning) "Cleaning..."
                            else "CLEAN NOW  ·  $totalFileCount items  ·  ${Formatter.formatShortFileSize(context, totalSize)}",
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
                        Text("Scanning for junk files...", style = MaterialTheme.typography.bodyLarge)
                    }
                }

                junkCategories.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No junk files found",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Your storage looks clean!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    ) {
                        item {
                            Text(
                                "Found ${junkCategories.sumOf { it.files.size }} junk items",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        items(junkCategories, key = { it.key }) { category ->
                            JunkCategoryCard(
                                category = category,
                                isChecked = category.key in checkedCategories,
                                isExpanded = expandedCategory == category.key,
                                onCheckedChange = { checked ->
                                    if (checked) checkedCategories.add(category.key)
                                    else checkedCategories.remove(category.key)
                                },
                                onExpandToggle = {
                                    expandedCategory =
                                        if (expandedCategory == category.key) null else category.key
                                },
                            )
                        }

                        // Bottom spacer for the bottom bar
                        item { Spacer(Modifier.height(72.dp)) }
                    }
                }
            }
        }
    }

    // Result dialog
    resultMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { resultMessage = null },
            title = { Text("Clean Complete") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { resultMessage = null }) {
                    Text("OK")
                }
            },
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Category card
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun JunkCategoryCard(
    category: JunkCategory,
    isChecked: Boolean,
    isExpanded: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onExpandToggle: () -> Unit,
) {
    val context = LocalContext.current
    val totalSize = if (category.key == "empty_folders") {
        0L
    } else {
        category.files.sumOf { it.length() }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
            )
            Spacer(Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = category.color.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${category.files.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (totalSize > 0) {
                    Text(
                        Formatter.formatShortFileSize(context, totalSize),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = category.color,
                    )
                }
            }
        }

        // Expandable file list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, end = 12.dp, bottom = 12.dp),
            ) {
                androidx.compose.material3.HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                category.files.forEach { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            file.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        if (file.isFile) {
                            Text(
                                Formatter.formatShortFileSize(context, file.length()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        // Expand/collapse tap area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(
                onClick = onExpandToggle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (isExpanded) "Hide files" else "Show files",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}