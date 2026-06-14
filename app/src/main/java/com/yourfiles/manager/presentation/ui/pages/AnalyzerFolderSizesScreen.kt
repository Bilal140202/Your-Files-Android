package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private data class ChildEntry(
    val path: String,
    val name: String,
    val size: Long,
    val isDirectory: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerFolderSizesScreen(
    initialPath: String? = null,
) {
    val context = LocalContext.current
    val navController = remember { App.instance.navController() }
    val scope = rememberCoroutineScope()
    val rootPath = remember { Environment.getExternalStorageDirectory().absolutePath }

    var currentPath by remember(initialPath) {
        mutableStateOf(initialPath ?: rootPath)
    }
    var scanning by remember { mutableStateOf(true) }
    val children = remember { mutableStateListOf<ChildEntry>() }
    var folderCount by remember { mutableStateOf(0) }
    var fileCount by remember { mutableStateOf(0) }
    var totalSize by remember { mutableStateOf(0L) }

    LaunchedEffect(currentPath) {
        scanning = true
        children.clear()
        folderCount = 0
        fileCount = 0
        totalSize = 0L
        scope.launch(Dispatchers.IO) {
            val dir = File(currentPath)
            if (!dir.exists() || !dir.isDirectory) {
                scanning = false
                return@launch
            }

            val entries = dir.listFiles()
                ?.filter { !it.name.startsWith(".") }
                ?.map { child ->
                    val size = if (child.isDirectory) {
                        child.walkTopDown()
                            .onEnter { d -> !d.name.startsWith(".") }
                            .filter { it.isFile }
                            .sumOf { it.length() }
                    } else {
                        child.length()
                    }
                    ChildEntry(
                        path = child.absolutePath,
                        name = child.name,
                        size = size,
                        isDirectory = child.isDirectory,
                    )
                }
                ?: emptyList()

            val sorted = entries.sortedByDescending { it.size }
            children.addAll(sorted)

            var folders = 0
            var files = 0
            var total = 0L
            for (entry in sorted) {
                if (entry.isDirectory) folders++ else files++
                total += entry.size
            }
            folderCount = folders
            fileCount = files
            totalSize = total
            scanning = false
        }
    }

    val folderName = remember(currentPath) {
        File(currentPath).name.ifEmpty { "Internal Storage" }
    }

    val isAtRoot = currentPath == rootPath

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = folderName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isAtRoot) {
                            val parent = File(currentPath).parentFile?.absolutePath
                            if (parent != null) {
                                currentPath = parent
                            } else {
                                navController.popBackStack()
                            }
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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
            if (!scanning && children.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Text(
                            "$folderCount folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "$fileCount files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            Formatter.formatShortFileSize(context, totalSize) + " total",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
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
                    Text("Scanning folder...")
                }
            }
        } else if (children.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Empty folder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val maxSize = children.firstOrNull()?.size ?: 1L

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(children, key = { it.path }) { entry ->
                    val fraction = if (maxSize > 0) entry.size.toFloat() / maxSize.toFloat() else 0f

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = entry.isDirectory) {
                                currentPath = entry.path
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (entry.isDirectory) Icons.Outlined.Folder
                            else Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = null,
                            tint = if (entry.isDirectory) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (entry.isDirectory) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = Formatter.formatShortFileSize(context, entry.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceIn(0.02f, 1f))
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                ) {}
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}