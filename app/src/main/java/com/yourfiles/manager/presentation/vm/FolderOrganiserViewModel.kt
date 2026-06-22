package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)"),
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
}

/** File category for grouping — mirrors StorageCategory from StorageAnalyzerVM. */
enum class FileCategory(val label: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APK("APKs"),
    ARCHIVES("Archives"),
    OTHER("Other"),
}

data class OrganiserFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val category: FileCategory,
)

data class FolderOrganiserState(
    val folderPath: String = "",
    val folderName: String = "",
    val allFiles: List<OrganiserFileItem> = emptyList(),
    val sortOption: SortOption = SortOption.NAME_ASC,
    val groupByType: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    /** Total file count (non-directories). */
    val fileCount: Int get() = allFiles.count { !it.isDirectory }

    /** Total size of all files (non-directories). */
    val totalSize: Long get() = allFiles.filter { !it.isDirectory }.sumOf { it.size }

    /** Sorted and optionally grouped display items. */
    val displayItems: List<Any>
        get() {
            val sorted = when (sortOption) {
                SortOption.NAME_ASC -> allFiles.sortedWith(compareBy { it.name.lowercase() })
                SortOption.NAME_DESC -> allFiles.sortedWith(compareByDescending { it.name.lowercase() })
                SortOption.SIZE_DESC -> allFiles.sortedWith(compareByDescending<OrganiserFileItem> { it.size }.thenBy { it.name.lowercase() })
                SortOption.SIZE_ASC -> allFiles.sortedWith(compareBy<OrganiserFileItem> { it.size }.thenBy { it.name.lowercase() })
                SortOption.DATE_DESC -> allFiles.sortedWith(compareByDescending<OrganiserFileItem> { it.lastModified }.thenBy { it.name.lowercase() })
                SortOption.DATE_ASC -> allFiles.sortedWith(compareBy<OrganiserFileItem> { it.lastModified }.thenBy { it.name.lowercase() })
            }

            if (!groupByType) return sorted

            // Group by category, preserving order
            val result = mutableListOf<Any>()
            val groups = sorted.filter { !it.isDirectory }.groupBy { it.category }
            val dirs = sorted.filter { it.isDirectory }

            // Directories first (un-grouped)
            if (dirs.isNotEmpty()) {
                result.add(GroupHeader("Folders", dirs.size))
                result.addAll(dirs)
            }

            // Then each category
            for (cat in FileCategory.entries) {
                val items = groups[cat]
                if (!items.isNullOrEmpty()) {
                    result.add(GroupHeader(cat.label, items.size))
                    result.addAll(items)
                }
            }
            return result
        }
}

/** Header item used when grouping by type. */
data class GroupHeader(val label: String, val count: Int)

class FolderOrganiserViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(FolderOrganiserState())
    val state: StateFlow<FolderOrganiserState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        val path = savedStateHandle.get<String>("path")
        if (path != null) {
            loadFolder(path)
        }
    }

    fun loadFolder(path: String) {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Directory not found: $path",
            )
            return
        }

        _state.value = _state.value.copy(
            folderPath = path,
            folderName = dir.name.ifEmpty { "Storage" },
            isLoading = true,
            error = null,
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = dir.listFiles()
                if (entries == null) {
                    withContext(Dispatchers.Main) {
                        _state.value = _state.value.copy(isLoading = false, error = "Cannot read directory")
                    }
                    return@launch
                }

                val items = entries
                    .filter { !it.name.startsWith(".") }
                    .map { file ->
                        OrganiserFileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            category = categorize(file.name),
                        )
                    }

                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(
                        allFiles = items,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                Log.e("FolderOrganiser", "Error loading $path", e)
                withContext(Dispatchers.Main) {
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    fun setSortOption(option: SortOption) {
        _state.value = _state.value.copy(sortOption = option)
    }

    fun toggleGroupByType() {
        _state.value = _state.value.copy(groupByType = !_state.value.groupByType)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return ""
        return dateFormat.format(Date(timestamp))
    }

    /** Categorize a file based on its extension — same logic as StorageAnalyzerVM. */
    private fun categorize(filename: String): FileCategory {
        val dotIndex = filename.lastIndexOf('.')
        if (dotIndex < 0) return FileCategory.OTHER
        val ext = filename.substring(dotIndex + 1).lowercase()
        return when {
            ext in IMG_EXTS -> FileCategory.IMAGES
            ext in VID_EXTS -> FileCategory.VIDEOS
            ext in AUD_EXTS -> FileCategory.AUDIO
            ext in DOC_EXTS -> FileCategory.DOCUMENTS
            ext in ARC_EXTS -> FileCategory.ARCHIVES
            ext == "apk" -> FileCategory.APK
            else -> FileCategory.OTHER
        }
    }

    companion object {
        private val IMG_EXTS = setOf(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif", "raw", "tiff"
        )
        private val VID_EXTS = setOf(
            "mp4", "mkv", "avi", "mov", "3gp", "flv", "wmv", "webm", "ts", "m4v"
        )
        private val AUD_EXTS = setOf(
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "amr"
        )
        private val DOC_EXTS = setOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "rtf", "html", "htm", "xml", "json",
        )
        private val ARC_EXTS = setOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg"
        )
    }
}