package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.collection.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfiles.manager.domain.model.FileItem
import com.yourfiles.manager.utils.TrashManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Clipboard state for Copy/Cut operations. */
enum class ClipboardMode { COPY, CUT }

data class ExplorerState(
    val currentPath: String = "",
    val items: List<FileItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItems: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    // Select interval state
    val intervalAnchor: String? = null,   // first item path for range selection
    val isIntervalMode: Boolean = false,
    // Clipboard state
    val clipboardPaths: List<String> = emptyList(),
    val clipboardMode: ClipboardMode? = null,
) {
    /** Derive display list: filtered by searchQuery, folders first always. */
    val displayItems: List<FileItem>
        get() {
            if (searchQuery.isEmpty()) return items
            return items.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }.sortedWith(
                compareByDescending<FileItem> { it.isDirectory }
                    .thenBy { it.name.lowercase() }
            )
        }

    /** True if clipboard has items to paste. */
    val hasClipboard: Boolean get() = clipboardPaths.isNotEmpty()
}

class FileExplorerViewModel(
    app: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ExplorerState())
    val state: StateFlow<ExplorerState> = _state

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val rootPath = Environment.getExternalStorageDirectory().absolutePath

    /** LRU cache for folder contents — holds up to 20 folders. */
    private val folderCache = LruCache<String, List<FileItem>>(20)

    companion object {
        private const val KEY_CURRENT_PATH = "current_path"
    }

    init {
        val savedPath = savedStateHandle.get<String>(KEY_CURRENT_PATH)
        val startPath = savedPath ?: rootPath
        navigateTo(startPath, saveState = false)
        if (savedPath != null) {
            savedStateHandle[KEY_CURRENT_PATH] = savedPath
        }
    }

    fun initWithNavPath(navPath: String?) {
        val savedPath = savedStateHandle.get<String>(KEY_CURRENT_PATH)
        if (savedPath == null && navPath != null) {
            navigateTo(navPath)
        }
    }

    /**
     * Navigate to a folder. Clears old items immediately (no flash),
     * loads from cache or filesystem.
     */
    fun navigateTo(path: String, saveState: Boolean = true) {
        // BUG 3 FIX: Clear items IMMEDIATELY so old content doesn't flash
        _state.value = _state.value.copy(
            currentPath = path,
            items = emptyList(),   // clear immediately — no content flash
            isLoading = true,
            error = null,
            selectedItems = emptySet(),
            isMultiSelectMode = false,
            searchQuery = "",
            intervalAnchor = null,
            isIntervalMode = false,
        )
        if (saveState) {
            savedStateHandle[KEY_CURRENT_PATH] = path
        }

        // BUG 2 FIX: Check cache FIRST — instant return
        val cached = folderCache.get(path)
        if (cached != null) {
            Log.d("FileExplorer", "Cache hit: $path (${cached.size} items)")
            _state.value = _state.value.copy(
                isLoading = false,
                items = cached,
            )
            // Update child counts in background (may have stale counts)
            updateChildCountsAsync(path, cached)
            return
        }

        // Load from filesystem
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = loadFolderFast(path)
                folderCache.put(path, items)
                Log.d("FileExplorer", "Listed ${items.size} items in $path")
                _state.value = _state.value.copy(
                    isLoading = false,
                    items = items,
                )
                // Count children async after list is shown
                updateChildCountsAsync(path, items)
            } catch (e: Exception) {
                Log.e("FileExplorer", "Error listing $path", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Load folder contents WITHOUT counting children (instant).
     * Only lists top-level entries and their metadata.
     */
    private fun loadFolderFast(path: String): List<FileItem> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) {
            throw IllegalArgumentException("Directory not found: $path")
        }
        val entries = dir.listFiles()
        if (entries == null) return emptyList()

        return entries
            .filter { !it.name.startsWith(".") }
            .map { FileItem.fromFile(it) }  // childCount = 0, no blocking I/O
            .sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
    }

    /**
     * Count children for directories in the background and update state.
     * This runs AFTER the list is already visible to the user.
     */
    private fun updateChildCountsAsync(path: String, items: List<FileItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val dirItems = items.filter { it.isDirectory }
            val updates = mutableMapOf<String, Int>()
            for (item in dirItems) {
                try {
                    val count = File(item.path).list()?.size ?: 0
                    updates[item.path] = count
                } catch (_: Exception) { }
            }
            if (updates.isEmpty()) return@launch

            // Update state with new child counts
            withContext(Dispatchers.Main) {
                val currentItems = _state.value.items.toMutableList()
                var changed = false
                for (i in currentItems.indices) {
                    val count = updates[currentItems[i].path]
                    if (count != null && currentItems[i].childCount != count) {
                        currentItems[i] = currentItems[i].copy(childCount = count)
                        changed = true
                    }
                }
                if (changed) {
                    _state.value = _state.value.copy(items = currentItems)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    /**
     * Check if we're at the root of the current storage tree.
     * For SD card, the root is the SD card mount point itself.
     * For internal, it's /storage/emulated/0.
     */
    fun isAtRoot(): Boolean {
        val current = _state.value.currentPath
        if (current.isEmpty()) return true
        if (current == rootPath) return true
        // Check if parent is one of the known storage roots
        val parent = File(current).parentFile?.absolutePath ?: return true
        return parent == rootPath || parent == "/storage" || parent == "/"
    }

    fun navigateUp(): Boolean {
        if (isAtRoot()) return false
        val current = File(_state.value.currentPath)
        val parent = current.parentFile ?: return false
        navigateTo(parent.absolutePath)
        return true
    }

    // ===== SELECTION =====

    fun toggleSelection(path: String) {
        val current = _state.value
        if (current.isIntervalMode && current.intervalAnchor != null) {
            selectRange(current.intervalAnchor, path)
            _state.value = _state.value.copy(
                intervalAnchor = null,
                isIntervalMode = false,
            )
            return
        }
        val selected = current.selectedItems.toMutableSet()
        if (selected.contains(path)) {
            selected.remove(path)
        } else {
            selected.add(path)
        }
        _state.value = _state.value.copy(
            selectedItems = selected,
            isMultiSelectMode = selected.isNotEmpty(),
        )
    }

    fun selectAll() {
        val allPaths = _state.value.displayItems.map { it.path }.toSet()
        _state.value = _state.value.copy(
            selectedItems = allPaths,
            isMultiSelectMode = allPaths.isNotEmpty(),
        )
    }

    fun enterIntervalMode() {
        _state.value = _state.value.copy(
            isIntervalMode = true,
            intervalAnchor = _state.value.selectedItems.firstOrNull(),
        )
    }

    private fun selectRange(fromPath: String, toPath: String) {
        val displayPaths = _state.value.displayItems.map { it.path }
        val fromIdx = displayPaths.indexOf(fromPath).coerceAtLeast(0)
        val toIdx = displayPaths.indexOf(toPath).coerceAtLeast(0)
        val start = minOf(fromIdx, toIdx)
        val end = maxOf(fromIdx, toIdx)
        val rangePaths = displayPaths.subList(start, end + 1).toSet()
        _state.value = _state.value.copy(
            selectedItems = rangePaths,
            isMultiSelectMode = rangePaths.isNotEmpty(),
        )
    }

    fun exitMultiSelect() {
        _state.value = _state.value.copy(
            selectedItems = emptySet(),
            isMultiSelectMode = false,
            intervalAnchor = null,
            isIntervalMode = false,
        )
    }

    // ===== CLIPBOARD: COPY / CUT / PASTE =====

    fun copySelected() {
        _state.value = _state.value.copy(
            clipboardPaths = _state.value.selectedItems.toList(),
            clipboardMode = ClipboardMode.COPY,
        )
        exitMultiSelect()
    }

    fun cutSelected() {
        _state.value = _state.value.copy(
            clipboardPaths = _state.value.selectedItems.toList(),
            clipboardMode = ClipboardMode.CUT,
        )
        exitMultiSelect()
    }

    fun pasteClipboard(onComplete: () -> Unit) {
        val clipboard = _state.value
        if (clipboard.clipboardPaths.isEmpty() || clipboard.clipboardMode == null) return

        viewModelScope.launch(Dispatchers.IO) {
            val destDir = File(_state.value.currentPath)
            try {
                clipboard.clipboardPaths.forEach { srcPath ->
                    val src = File(srcPath)
                    if (!src.exists()) return@forEach
                    val dest = File(destDir, src.name)
                    if (!dest.exists()) {
                        src.copyTo(dest, overwrite = false)
                    }
                    if (clipboard.clipboardMode == ClipboardMode.CUT) {
                        if (src.isDirectory) src.deleteRecursively()
                        else src.delete()
                    }
                }
                if (clipboard.clipboardMode == ClipboardMode.CUT) {
                    _state.value = _state.value.copy(
                        clipboardPaths = emptyList(),
                        clipboardMode = null,
                    )
                }
                // Invalidate cache for destination and source folders
                folderCache.remove(_state.value.currentPath)
                clipboard.clipboardPaths.forEach { p -> File(p).parent?.let { folderCache.remove(it) } }
                navigateTo(_state.value.currentPath)
                onComplete()
            } catch (e: Exception) {
                Log.e("FileExplorer", "Paste error", e)
            }
        }
    }

    fun clearClipboard() {
        _state.value = _state.value.copy(
            clipboardPaths = emptyList(),
            clipboardMode = null,
        )
    }

    // ===== DELETE =====

    fun deleteSelected(onComplete: () -> Unit) {
        val pathsToDelete = _state.value.selectedItems.toSet()
        viewModelScope.launch(Dispatchers.IO) {
            // Move to recycle bin instead of permanent delete
            TrashManager.moveToTrash(pathsToDelete)
            // Invalidate cache
            folderCache.remove(_state.value.currentPath)
            pathsToDelete.forEach { p -> File(p).parent?.let { folderCache.remove(it) } }
            navigateTo(_state.value.currentPath)
            onComplete()
        }
    }

    // ===== RENAME =====

    fun renameItem(oldPath: String, newName: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldFile = File(oldPath)
                if (!oldFile.exists()) {
                    Log.e("FileExplorer", "Rename: file not found $oldPath")
                    return@launch
                }
                val newFile = File(oldFile.parentFile, newName)
                if (oldFile.renameTo(newFile)) {
                    folderCache.remove(_state.value.currentPath)
                    navigateTo(_state.value.currentPath)
                    onComplete()
                } else {
                    Log.e("FileExplorer", "Rename failed: $oldPath -> $newName")
                }
            } catch (e: Exception) {
                Log.e("FileExplorer", "Rename error", e)
            }
        }
    }

    // ===== FILE OPERATIONS =====

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(_state.value.currentPath, folderName)
            if (dir.mkdirs()) {
                folderCache.remove(_state.value.currentPath)
                navigateTo(_state.value.currentPath)
            }
        }
    }

    fun getBreadcrumbSegments(): List<Pair<String, String>> {
        val fullPath = _state.value.currentPath
        val segments = mutableListOf<Pair<String, String>>()

        // Determine if we're browsing SD card
        val isSdCard = fullPath.isNotEmpty() && !fullPath.startsWith(rootPath)

        // Walk up the path to find the storage root
        val effectiveRoot = if (isSdCard) {
            // For SD card, the root is the mount point (e.g. /storage/76E3-1ADF)
            var p = fullPath
            while (p.isNotEmpty() && p != "/" && p != "/storage") {
                val parent = File(p).parentFile?.absolutePath ?: break
                if (parent == "/storage" || parent == "/") break
                p = parent
            }
            p
        } else {
            rootPath
        }

        val rootLabel = if (isSdCard) {
            "SD Card"
        } else {
            "Internal Storage"
        }

        // Build path segments from root
        val parts = mutableListOf<String>()
        var current = fullPath
        while (current.isNotEmpty() && current != effectiveRoot && current != "/") {
            val file = File(current)
            parts.add(0, file.name)
            val parent = file.parentFile ?: break
            current = parent.absolutePath
        }

        segments.add(rootLabel to effectiveRoot)
        var buildPath = effectiveRoot
        for (part in parts) {
            buildPath = File(buildPath, part).absolutePath
            segments.add(part to buildPath)
        }
        return segments
    }

    fun getCurrentFolderName(): String {
        val path = _state.value.currentPath
        if (path == rootPath || path.isEmpty()) return "Internal Storage"
        val name = File(path).name
        return name.ifEmpty { "Internal Storage" }
    }

    fun getItemDetails(path: String): String {
        val file = File(path)
        if (!file.exists()) return "File not found"
        val sb = StringBuilder()
        sb.append("Name: ${file.name}\n")
        sb.append("Path: ${file.absolutePath}\n")
        sb.append("Size: ${android.text.format.Formatter.formatFileSize(getApplication(), file.length())}\n")
        sb.append("Modified: ${dateFormat.format(Date(file.lastModified()))}\n")
        sb.append("Type: ${if (file.isDirectory) "Folder" else file.extension.uppercase() + " file"}")
        if (file.isDirectory) {
            val children = file.listFiles()
            sb.append("\nContents: ${children?.size ?: 0} items")
        }
        return sb.toString()
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return ""
        return dateFormat.format(Date(timestamp))
    }

    /** Clear the folder cache (e.g. after file operations). */
    fun clearCache() {
        folderCache.evictAll()
    }
}
