package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// ===== Data models =====

data class OrganiseCategory(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val extensions: Set<String>,
    val isSizeBased: Boolean = false,
    val sizeThreshold: Long = 0L,
)

private val ORGANISE_CATEGORIES = listOf(
    OrganiseCategory("Images", "Images", Icons.Outlined.Image, Color(0xFFE91E63),
        setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "bmp")),
    OrganiseCategory("Videos", "Videos", Icons.Outlined.Movie, Color(0xFF9C27B0),
        setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "flv", "wmv")),
    OrganiseCategory("Audio", "Audio", Icons.Outlined.AudioFile, Color(0xFFFF9800),
        setOf("mp3", "aac", "m4a", "flac", "wav", "ogg", "wma")),
    OrganiseCategory("Documents", "Documents", Icons.Outlined.Description, Color(0xFF2196F3),
        setOf("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv")),
    OrganiseCategory("PDFs", "PDFs", Icons.Outlined.PictureAsPdf, Color(0xFFF44336),
        setOf("pdf")),
    OrganiseCategory("APKs", "APKs", Icons.Outlined.Memory, Color(0xFF4CAF50),
        setOf("apk")),
    OrganiseCategory("Archives", "Archives", Icons.Outlined.Archive, Color(0xFF795548),
        setOf("zip", "rar", "7z", "tar", "gz")),
    OrganiseCategory("Large Files", "Large files (>100 MB)", Icons.Outlined.Folder,
        Color(0xFF607D8B), emptySet(),
        isSizeBased = true, sizeThreshold = 100L * 1024 * 1024),
    OrganiseCategory("Others", "Others", Icons.Outlined.FolderOpen, Color(0xFF9E9E9E), emptySet()),
)

data class MoveResult(
    val originalPath: String,
    val newPath: String,
)

data class OrganiseResult(
    val movedCount: Int,
    val skippedCount: Int,
    val foldersCreated: Set<String>,
    val moves: List<MoveResult>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderOrganiserScreen(initialPath: String? = null) {
    val scope = rememberCoroutineScope()
    val navigator = remember { App.instance.navController() }

    var folderPath by remember {
        mutableStateOf(initialPath ?: Environment.getExternalStorageDirectory().absolutePath + "/Download")
    }
    var showFolderPicker by remember { mutableStateOf(false) }
    val checkedCategories = remember { mutableStateListOf<String>() }
    var fileCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var isCounting by remember { mutableStateOf(false) }
    var isOrganising by remember { mutableStateOf(false) }
    var organiseProgress by remember { mutableStateOf("") }
    var organiseResult by remember { mutableStateOf<OrganiseResult?>(null) }
    var showUndoTimer by remember { mutableStateOf(false) }
    var undoSeconds by remember { mutableIntStateOf(10) }
    var isUndoing by remember { mutableStateOf(false) }

    // Safety: existing category sub-folders
    val existingCategoryFolders = remember(folderPath) {
        val dir = File(folderPath)
        if (!dir.exists() || !dir.isDirectory) return@remember emptySet<String>()
        ORGANISE_CATEGORIES.map { it.key }.filter { cat ->
            File(dir, cat).exists() && File(dir, cat).isDirectory
        }.toSet()
    }

    // Count files per category
    LaunchedEffect(folderPath, checkedCategories.toSet()) {
        if (folderPath.isEmpty() || checkedCategories.isEmpty()) {
            fileCounts = emptyMap()
            return@LaunchedEffect
        }
        isCounting = true
        val counts = withContext(Dispatchers.IO) {
            countFilesInFolder(folderPath, checkedCategories.toSet())
        }
        fileCounts = counts
        isCounting = false
    }

    // Undo countdown
    LaunchedEffect(showUndoTimer) {
        if (showUndoTimer) {
            undoSeconds = 10
            while (undoSeconds > 0) {
                delay(1000)
                undoSeconds--
            }
            showUndoTimer = false
        }
    }

    fun performUndo() {
        showUndoTimer = false
        val result = organiseResult ?: return
        isUndoing = true
        scope.launch(Dispatchers.IO) {
            result.moves.reversed().forEach { move ->
                val src = File(move.newPath)
                val dst = File(move.originalPath)
                if (src.exists()) {
                    dst.parentFile?.mkdirs()
                    src.renameTo(dst)
                }
            }
            result.foldersCreated.forEach { folderName ->
                val folder = File(folderPath, folderName)
                if (folder.exists() && folder.listFiles()?.isEmpty() == true) {
                    folder.delete()
                }
            }
            withContext(Dispatchers.Main) {
                isUndoing = false
                organiseResult = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organise: ${File(folderPath).name}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navigator.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
        ) {
            // Folder selector
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Folder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = folderPath,
                            onValueChange = {},
                            label = { Text("Folder path") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            readOnly = true,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(onClick = { showFolderPicker = true }) {
                            Text("Browse")
                        }
                    }
                }
            }

            // Safety warning
            if (existingCategoryFolders.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Category folders already exist", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(existingCategoryFolders.joinToString(", ") + " skipped to prevent double-organise", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            // Category checklist header
            item {
                Text("Select Categories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }

            // Category items
            items(ORGANISE_CATEGORIES) { category ->
                val isChecked = checkedCategories.contains(category.key)
                val count = fileCounts[category.key] ?: 0

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isChecked) category.color.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) checkedCategories.add(category.key)
                                else checkedCategories.remove(category.key)
                            },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(category.icon, null, tint = category.color, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(category.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        if (isCounting && isChecked) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else if (isChecked) {
                            Text(if (count > 0) "${String.format("%,d", count)} files" else "0 files", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Organise button
            item {
                val totalFiles = fileCounts.values.sum()
                val canOrganise = checkedCategories.isNotEmpty() && totalFiles > 0 && !isOrganising
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            isOrganising = true
                            scope.launch(Dispatchers.IO) {
                                val result = organiseFolder(File(folderPath), checkedCategories.toSet(), existingCategoryFolders) { progress ->
                                    withContext(Dispatchers.Main) { organiseProgress = progress }
                                }
                                withContext(Dispatchers.Main) {
                                    isOrganising = false
                                    organiseResult = result
                                    if (result.movedCount > 0) showUndoTimer = true
                                }
                            }
                        },
                        enabled = canOrganise,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isOrganising) organiseProgress else "Organise Now" + if (totalFiles > 0) " ($totalFiles files)" else "")
                    }
                }
            }

            // Result
            if (organiseResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Organisation Complete", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Moved ${organiseResult!!.movedCount} files into ${organiseResult!!.foldersCreated.size} folders", style = MaterialTheme.typography.bodyMedium)
                            if (organiseResult!!.skippedCount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Skipped ${organiseResult!!.skippedCount} files (duplicates or in-use)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (organiseResult!!.foldersCreated.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Folders: ${organiseResult!!.foldersCreated.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            if (showUndoTimer) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(onClick = { performUndo() }, enabled = !isUndoing) {
                                    if (isUndoing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Undoing...")
                                    } else {
                                        Text("Undo ($undoSeconds s)")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Folder picker dialog
    if (showFolderPicker) {
        FolderPickerDialog(
            initialPath = folderPath,
            onFolderSelected = { folderPath = it; showFolderPicker = false },
            onDismiss = { showFolderPicker = false },
        )
    }
}

// ===== Folder Picker =====

@Composable
private fun FolderPickerDialog(
    initialPath: String,
    onFolderSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    var entries by remember { mutableStateOf(listOf<File>()) }

    LaunchedEffect(currentPath) {
        val dir = File(currentPath)
        entries = withContext(Dispatchers.IO) {
            dir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") }?.sortedByDescending { it.name } ?: emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Folder") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(currentPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    val parent = File(currentPath).parentFile
                    if (parent != null && parent.canRead()) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().clickable { currentPath = parent.absolutePath }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("..", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    items(entries) { folder ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { currentPath = folder.absolutePath }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(folder.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    if (entries.isEmpty()) {
                        item {
                            Text("No sub-folders", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onFolderSelected(currentPath) }) { Text("Select This Folder") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

// ===== Business logic =====

private suspend fun countFilesInFolder(folderPath: String, selectedCategories: Set<String>): Map<String, Int> = withContext(Dispatchers.IO) {
    val dir = File(folderPath)
    if (!dir.exists() || !dir.isDirectory) return@withContext emptyMap()
    val counts = mutableMapOf<String, Int>()
    val files = dir.listFiles() ?: return@withContext emptyMap()
    for (file in files) {
        if (!file.isFile) continue
        val cat = categoriseFile(file, selectedCategories) ?: continue
        counts[cat] = (counts[cat] ?: 0) + 1
    }
    counts
}

private fun categoriseFile(file: File, selectedCategories: Set<String>): String? {
    val ext = file.extension.lowercase()
    for (cat in ORGANISE_CATEGORIES) {
        if (cat.key !in selectedCategories) continue
        when {
            cat.isSizeBased && file.length() >= cat.sizeThreshold -> return cat.key
            ext in cat.extensions -> return cat.key
            cat.key == "Others" -> return cat.key
        }
    }
    return null
}

private suspend fun organiseFolder(
    sourceDir: File,
    selectedCategories: Set<String>,
    existingFolders: Set<String>,
    onProgress: suspend (String) -> Unit,
): OrganiseResult = withContext(Dispatchers.IO) {
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        return@withContext OrganiseResult(0, 0, emptySet(), emptyList())
    }

    val files = sourceDir.listFiles() ?: emptyArray()
    val moves = mutableListOf<MoveResult>()
    val foldersCreated = mutableSetOf<String>()
    var skippedCount = 0
    val plan = mutableListOf<Pair<File, File>>()

    for (file in files) {
        if (!file.isFile) continue
        val catKey = categoriseFile(file, selectedCategories) ?: continue
        if (catKey in existingFolders) { skippedCount++; continue }

        val categoryDir = File(sourceDir, catKey)
        if (!categoryDir.exists()) { categoryDir.mkdirs(); foldersCreated.add(catKey) }

        var targetFile = File(categoryDir, file.name)
        var counter = 1
        while (targetFile.exists() && targetFile.absolutePath != file.absolutePath) {
            val nameWithoutExt = file.nameWithoutExtension
            val ext = file.extension
            targetFile = File(categoryDir, "${nameWithoutExt}($counter).${ext}")
            counter++
            if (counter > 100) break
        }
        if (targetFile.exists() && targetFile.absolutePath != file.absolutePath) { skippedCount++; continue }
        plan.add(file to targetFile)
    }

    val total = plan.size
    for ((index, pair) in plan.withIndex()) {
        val (src, dst) = pair
        if (src.renameTo(dst)) moves.add(MoveResult(src.absolutePath, dst.absolutePath))
        else skippedCount++
        onProgress("Moving... ${((index + 1) * 100 / total)}% (${index + 1}/$total)")
    }

    OrganiseResult(movedCount = moves.size, skippedCount = skippedCount, foldersCreated = foldersCreated, moves = moves)
}
