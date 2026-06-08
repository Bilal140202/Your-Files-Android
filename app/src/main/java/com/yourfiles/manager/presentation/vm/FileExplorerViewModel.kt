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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExplorerState(
    val currentPath: String = "",
    val items: List<FileItem> = emptyList(),        // full unfiltered list
    val searchQuery: String = "",                   // active search filter
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItems: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
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
        // Restore saved path from SavedStateHandle — survives ViewModel recreation
        val savedPath = savedStateHandle.get<String>(KEY_CURRENT_PATH)
        val startPath = savedPath ?: rootPath
        navigateTo(startPath, saveState = false) // don't double-save on init
        if (savedPath != null) {
            savedStateHandle[KEY_CURRENT_PATH] = savedPath
        }
    }

    /**
     * Called from FileBrowserScreen with the nav argument path.
     * Only applies if ViewModel has no saved path (first launch).
     */
    fun initWithNavPath(navPath: String?) {
        val savedPath = savedStateHandle.get<String>(KEY_CURRENT_PATH)
        if (savedPath == null && navPath != null) {
            navigateTo(navPath)
        }
        // If savedPath exists, ViewModel already restored it — ignore nav argument
    }

    fun navigateTo(path: String, saveState: Boolean = true) {
        _state.value = _state.value.copy(
            currentPath = path,
            isLoading = true,
            error = null,
            selectedItems = emptySet(),
            isMultiSelectMode = false,
            searchQuery = "", // clear search on folder change
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
                    Log.e("FileExplorer", "listFiles() returned null for $path")
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

    /**
     * Update search query. Called from UI after 150ms debounce.
     */
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

    fun toggleSelection(path: String) {
        val current = _state.value.selectedItems.toMutableSet()
        if (current.contains(path)) {
            current.remove(path)
        } else {
            current.add(path)
        }
        _state.value = _state.value.copy(
            selectedItems = current,
            isMultiSelectMode = current.isNotEmpty()
        )
    }

    fun exitMultiSelect() {
        _state.value = _state.value.copy(
            selectedItems = emptySet(),
            isMultiSelectMode = false,
        )
    }

    fun createFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(_state.value.currentPath, folderName)
            if (dir.mkdirs()) {
                navigateTo(_state.value.currentPath)
            }
        }
    }

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

    /** Get the current folder name for search hint. */
    fun getCurrentFolderName(): String {
        return File(_state.value.currentPath).name.ifEmpty { "Internal Storage" }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return ""
        return dateFormat.format(Date(timestamp))
    }
}
