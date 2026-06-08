package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/** File category for storage breakdown. */
enum class StorageCategory(val label: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APK("APKs"),
    OTHER("Other"),
}

/** A single row in the category breakdown. */
data class CategoryStats(
    val category: StorageCategory,
    val totalSize: Long,
    val fileCount: Int,
)

/** A row for top-largest-files list. */
data class LargestFileEntry(
    val path: String,
    val name: String,
    val size: Long,
)

/** UI state for the Storage Analyzer screen. */
data class AnalyzerUiState(
    val isScanning: Boolean = true,
    val totalUsedBytes: Long = 0L,
    val categories: List<CategoryStats> = emptyList(),
    val topLargestFiles: List<LargestFileEntry> = emptyList(),
    val error: String? = null,
)

class StorageAnalyzerVM(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AnalyzerUiState())
    val state: StateFlow<AnalyzerUiState> = _state.asStateFlow()

    private val rootPath = Environment.getExternalStorageDirectory().absolutePath

    init {
        analyzeStorage()
    }

    fun analyzeStorage() {
        _state.value = _state.value.copy(isScanning = true, error = null)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rootDir = File(rootPath)
                if (!rootDir.exists() || !rootDir.isDirectory) {
                    _state.value = _state.value.copy(isScanning = false, error = "Storage not accessible")
                    return@launch
                }

                // Category accumulators
                val categorySizes = mutableMapOf<StorageCategory, Long>()
                val categoryCounts = mutableMapOf<StorageCategory, Int>()
                StorageCategory.entries.forEach { cat ->
                    categorySizes[cat] = 0L
                    categoryCounts[cat] = 0
                }

                // Largest files tracking
                val largestFiles = mutableListOf<LargestFileEntry>()

                val statFs = android.os.StatFs(rootPath)
                val totalBytes = statFs.totalBytes
                val freeBytes = statFs.availableBytes
                val usedBytes = totalBytes - freeBytes

                // Walk first-level directories, then 2 levels deep for speed
                scanDirectory(rootDir, categorySizes, categoryCounts, largestFiles, depth = 0, maxDepth = 3)

                // Sort largest files descending
                val topFiles = largestFiles
                    .sortedByDescending { it.size }
                    .take(10)

                // Build category list sorted by size descending
                val categories = StorageCategory.entries.map { cat ->
                    CategoryStats(
                        category = cat,
                        totalSize = categorySizes[cat] ?: 0L,
                        fileCount = categoryCounts[cat] ?: 0,
                    )
                }.sortedByDescending { it.totalSize }

                _state.value = AnalyzerUiState(
                    isScanning = false,
                    totalUsedBytes = usedBytes,
                    categories = categories,
                    topLargestFiles = topFiles,
                )
            } catch (e: Exception) {
                Log.e("StorageAnalyzer", "Analysis failed", e)
                _state.value = _state.value.copy(isScanning = false, error = e.message)
            }
        }
    }

    /** Recursively scan files, categorizing them. */
    private fun scanDirectory(
        dir: File,
        categorySizes: MutableMap<StorageCategory, Long>,
        categoryCounts: MutableMap<StorageCategory, Int>,
        largestFiles: MutableList<LargestFileEntry>,
        depth: Int,
        maxDepth: Int,
    ) {
        if (depth > maxDepth) return
        val children = dir.listFiles() ?: return
        for (file in children) {
            if (file.isHidden || file.name.startsWith(".")) continue
            if (file.isFile) {
                val size = file.length()
                val cat = categorize(file.name)
                categorySizes[cat] = (categorySizes[cat] ?: 0L) + size
                categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1
                // Track large files (> 1MB)
                if (size > 1_000_000 && largestFiles.size < 50) {
                    largestFiles.add(LargestFileEntry(file.absolutePath, file.name, size))
                }
            } else if (file.isDirectory) {
                scanDirectory(file, categorySizes, categoryCounts, largestFiles, depth + 1, maxDepth)
            }
        }
    }

    /** Categorize a file based on its extension. */
    private fun categorize(filename: String): StorageCategory {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return when {
            ext in IMG_EXTS -> StorageCategory.IMAGES
            ext in VID_EXTS -> StorageCategory.VIDEOS
            ext in AUD_EXTS -> StorageCategory.AUDIO
            ext in DOC_EXTS -> StorageCategory.DOCUMENTS
            ext == "apk" -> StorageCategory.APK
            else -> StorageCategory.OTHER
        }
    }

    companion object {
        private val IMG_EXTS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif", "raw", "tiff")
        private val VID_EXTS = setOf("mp4", "mkv", "avi", "mov", "3gp", "flv", "wmv", "webm", "ts", "m4v")
        private val AUD_EXTS = setOf("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "amr")
        private val DOC_EXTS = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf", "html", "htm", "xml", "json", "zip", "rar", "7z", "tar", "gz")
    }
}
