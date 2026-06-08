package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfiles.manager.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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

    fun navigateTo(path: String, saveState: Boolean = true) {
        _state.value = _state.value.copy(
            currentPath = path,
            isLoading = true,
            error = null,
            selectedItems = emptySet(),
            isMultiSelectMode = false,
            searchQuery = "",
            intervalAnchor = null,
            isIntervalMode = false,
            // Keep clipboard across navigation (ES behavior)
        )
        if (saveState) {
            savedStateHandle[KEY_CURRENT_PATH] = path
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dir = File(path)
                if (!dir.exists() || !dir.isDirectory) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Directory not found: $path"
                    )
                    return@launch
                }
                val entries = dir.listFiles()
                if (entries == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        items = emptyList(),
                        error = null
                    )
                    return@launch
                }
                val items = entries
                    .filter { !it.name.startsWith(".") }
                    .map { FileItem.fromFile(it) }
                    .sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() })

                Log.d("FileExplorer", "Listed ${items.size} items in $path")
                _state.value = _state.value.copy(
                    isLoading = false,
                    items = items,
                )
            } catch (e: Exception) {
                Log.e("FileExplorer", "Error listing $path", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun isAtRoot(): Boolean {
        val current = _state.value.currentPath
        return current == rootPath || current.isEmpty()
    }

    fun navigateUp(): Boolean {
        if (isAtRoot()) return false
        val current = File(_state.value.currentPath)
        val parent = current.parentFile ?: return false
        if (parent.absolutePath == rootPath || parent.canRead()) {
            navigateTo(parent.absolutePath)
            return true
        }
        return false
    }

    // ===== SELECTION =====

    fun toggleSelection(path: String) {
        val current = _state.value
        if (current.isIntervalMode && current.intervalAnchor != null) {
            // Interval mode: select range from anchor to this item
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

    /** Select all items in current display list. */
    fun selectAll() {
        val allPaths = _state.value.displayItems.map { it.path }.toSet()
        _state.value = _state.value.copy(
            selectedItems = allPaths,
            isMultiSelectMode = allPaths.isNotEmpty(),
        )
    }

    /** Enter interval select mode. Next tap sets the range end. */
    fun enterIntervalMode() {
        _state.value = _state.value.copy(
            isIntervalMode = true,
            intervalAnchor = _state.value.selectedItems.firstOrNull(),
        )
    }

    /** Select range between two paths (inclusive). */
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

    /** Paste clipboard items into current folder. For CUT, deletes sources after copy. */
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
                // Clear clipboard if was CUT mode (ES behavior)
                if (clipboard.clipboardMode == ClipboardMode.CUT) {
                    _state.value = _state.value.copy(
                        clipboardPaths = emptyList(),
                        clipboardMode = null,
                    )
                }
                navigateTo(_state.value.currentPath)
                onComplete()
            } catch (e: Exception) {
                Log.e("FileExplorer", "Paste error", e)
            }
        }
    }

    /** Clear clipboard. */
    fun clearClipboard() {
        _state.value = _state.value.copy(
            clipboardPaths = emptyList(),
            clipboardMode = null,
        )
    }

    // ===== DELETE =====

    fun deleteSelected(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value.selectedItems.forEach { path ->
                try {
                    val file = File(path)
                    if (file.exists()) {
                        if (file.isDirectory) file.deleteRecursively()
                        else file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("FileExplorer", "Error deleting $path", e)
                }
            }
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
                navigateTo(_state.value.currentPath)
            }
        }
    }

    fun getBreadcrumbSegments(): List<Pair<String, String>> {
        val fullPath = _state.value.currentPath
        val segments = mutableListOf<Pair<String, String>>()
        var current = fullPath
        val parts = mutableListOf<String>()

        while (current.isNotEmpty() && current != rootPath && current != "/") {
            val file = File(current)
            parts.add(0, file.name)
            val parent = file.parentFile ?: break
            current = parent.absolutePath
            if (current == rootPath) {
                parts.add(0, "Internal Storage")
                break
            }
        }
        if (parts.isEmpty()) {
            parts.add("Internal Storage")
        }

        var buildPath = rootPath
        segments.add("Internal Storage" to rootPath)
        for (i in 1 until parts.size) {
            buildPath = File(buildPath, parts[i]).absolutePath
            segments.add(parts[i] to buildPath)
        }
        return segments
    }

    fun getCurrentFolderName(): String {
        return File(_state.value.currentPath).name.ifEmpty { "Internal Storage" }
    }

    /** Get item details for the "More" bottom sheet. */
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
}
