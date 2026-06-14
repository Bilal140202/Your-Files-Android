package com.yourfiles.manager.presentation.ui.pages

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.App
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.presentation.vm.StorageAnalyzerVM
import com.yourfiles.manager.presentation.vm.StorageCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private data class CategoryFileEntry(
    val path: String,
    val name: String,
    val size: Long,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AnalyzerCategoryFilesScreen(
    category: StorageCategory,
) {
    val context = LocalContext.current
    val navController = remember { App.instance.navController() }
    val scope = rememberCoroutineScope()

    var scanning by remember { mutableStateOf(true) }
    val files = remember { mutableStateListOf<CategoryFileEntry>() }
    val selectedPaths = remember { mutableStateListOf<String>() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        scanning = true
        files.clear()
        selectedPaths.clear()
        scope.launch(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val allKnownExts = StorageAnalyzerVM.IMG_EXTS +
                StorageAnalyzerVM.VID_EXTS +
                StorageAnalyzerVM.AUD_EXTS +
                StorageAnalyzerVM.DOC_EXTS +
                StorageAnalyzerVM.ARC_EXTS +
                setOf("apk")

            val exts = StorageAnalyzerVM.extensionsFor(category)
            val isOther = category == StorageCategory.OTHER

            val result = mutableListOf<CategoryFileEntry>()

            root.walkTopDown()
                .onEnter { dir -> !dir.name.startsWith(".") }
                .filter { it.isFile }
                .forEach { file ->
                    val ext = file.extension.lowercase()
                    val matches = ext in exts
                    if (isOther) {
                        if (ext !in allKnownExts) {
                            result.add(
                                CategoryFileEntry(
                                    path = file.absolutePath,
                                    name = file.name,
                                    size = file.length(),
                                )
                            )
                        }
                    } else {
                        if (matches) {
                            result.add(
                                CategoryFileEntry(
                                    path = file.absolutePath,
                                    name = file.name,
                                    size = file.length(),
                                )
                            )
                        }
                    }
                }

            result.sortByDescending { it.size }
            files.addAll(result)
            scanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(category.label, fontWeight = FontWeight.Bold)
                },
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
            if (selectedPaths.isNotEmpty()) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            "${selectedPaths.size} selected",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        TextButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        if (scanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Scanning ${category.label}...")
                }
            }
        } else if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No ${category.label.lowercase()} found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(files, key = { it.path }) { entry ->
                    val isSelected = entry.path in selectedPaths
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (selectedPaths.isNotEmpty()) {
                                        if (isSelected) {
                                            selectedPaths.remove(entry.path)
                                        } else {
                                            selectedPaths.add(entry.path)
                                        }
                                    } else {
                                        navController.navigate(
                                            "${Routes.FILE_DETAIL_VIEWER}?url=${Uri.encode(entry.path)}&category=file"
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (isSelected) {
                                        selectedPaths.remove(entry.path)
                                    } else {
                                        selectedPaths.add(entry.path)
                                    }
                                },
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    selectedPaths.add(entry.path)
                                } else {
                                    selectedPaths.remove(entry.path)
                                }
                            },
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = entry.path,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = Formatter.formatShortFileSize(context, entry.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Files") },
            text = {
                Text("Delete ${selectedPaths.size} selected files?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    scope.launch(Dispatchers.IO) {
                        val toRemove = selectedPaths.toList()
                        toRemove.forEach { path ->
                            File(path).delete()
                        }
                        selectedPaths.clear()
                        files.removeAll { it.path in toRemove }
                    }
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
}
