package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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

data class ExplorerState(
    val currentPath: String = "",
    val items: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedItems: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
)

class FileExplorerViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ExplorerState())
    val state: StateFlow<ExplorerState> = _state

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        navigateTo(Environment.getExternalStorageDirectory().absolutePath)
    }

    fun navigateTo(path: String) {
        _state.value = _state.value.copy(
            currentPath = path,
            isLoading = true,
            error = null,
            selectedItems = emptySet(),
            isMultiSelectMode = false,
        )
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

    fun isAtRoot(): Boolean {
        val current = _state.value.currentPath
        val root = Environment.getExternalStorageDirectory().absolutePath
        return current == root || current.isEmpty()
    }

    fun navigateUp(): Boolean {
        if (isAtRoot()) return false
        val current = File(_state.value.currentPath)
        val parent = current.parentFile ?: return false
        if (parent.absolutePath == Environment.getExternalStorageDirectory().absolutePath ||
            parent.canRead()) {
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
        val root = Environment.getExternalStorageDirectory().absolutePath
        var current = fullPath
        val parts = mutableListOf<String>()

        while (current.isNotEmpty() && current != root && current != "/") {
            val file = File(current)
            parts.add(0, file.name)
            val parent = file.parentFile ?: break
            current = parent.absolutePath
            if (current == root) {
                parts.add(0, "Internal Storage")
                break
            }
        }
        if (parts.isEmpty()) {
            parts.add("Internal Storage")
        }

        var buildPath = root
        segments.add("Internal Storage" to root)
        for (i in 1 until parts.size) {
            buildPath = File(buildPath, parts[i]).absolutePath
            segments.add(parts[i] to buildPath)
        }
        return segments
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return ""
        return dateFormat.format(Date(timestamp))
    }
}
