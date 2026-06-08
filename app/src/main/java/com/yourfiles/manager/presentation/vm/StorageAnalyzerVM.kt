package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.PriorityQueue
import kotlin.math.roundToInt

/** File category for storage breakdown. */
enum class StorageCategory(val label: String) {
    IMAGES("Images"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APK("APKs"),
    ARCHIVES("Archives"),
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
    val scanProgress: Int = 0,          // 0–100 estimated progress
    val scannedFileCount: Long = 0L,     // files processed so far
    val totalUsedBytes: Long = 0L,       // StatFs total − free
    val categories: List<CategoryStats> = emptyList(),
    val topLargestFiles: List<LargestFileEntry> = emptyList(),
    val error: String? = null,
)

class StorageAnalyzerVM(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AnalyzerUiState())
    val state: StateFlow<AnalyzerUiState> = _state.asStateFlow()

    private val rootPath = Environment.getExternalStorageDirectory().absolutePath

    private var scanJob: Job? = null

    // Cache: keep results for 1 hour so returning to the screen is instant
    private var cacheTimestamp: Long = 0L
    private var cachedState: AnalyzerUiState? = null

    init {
        analyzeStorage()
    }

    fun analyzeStorage() {
        // Return cached result if less than 1 hour old
        val now = System.currentTimeMillis()
        if (cachedState != null && (now - cacheTimestamp) < 3600_000L) {
            _state.value = cachedState!!
            return
        }

        scanJob?.cancel()
        _state.value = AnalyzerUiState(isScanning = true)
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val rootDir = File(rootPath)
                if (!rootDir.exists() || !rootDir.isDirectory) {
                    _state.value = _state.value.copy(isScanning = false, error = "Storage not accessible")
                    return@launch
                }

                val statFs = StatFs(rootPath)
                val usedBytes = statFs.totalBytes - statFs.availableBytes

                // Accumulators
                val categorySizes = mutableMapOf<StorageCategory, Long>()
                val categoryCounts = mutableMapOf<StorageCategory, Int>()
                StorageCategory.entries.forEach { cat ->
                    categorySizes[cat] = 0L
                    categoryCounts[cat] = 0
                }

                // Top-10 largest files via min-heap
                val largestHeap = PriorityQueue<LargestFileEntry>(11, compareBy { it.size })

                var scannedCount = 0L
                // Estimate total entries for progress (rough: count first-level items)
                val rootChildren = rootDir.listFiles()?.filter { !it.name.startsWith(".") } ?: emptyList()
                val estimatedMultiplier = 20  // heuristic: each top-level dir has ~20 descendants
                val estimatedTotal = (rootChildren.size * estimatedMultiplier).toLong().coerceAtLeast(1000L)

                // Full recursive walk — no depth limit
                rootDir.walkTopDown()
                    .onEnter { dir -> !dir.name.startsWith(".") }
                    .filter { it.isFile }
                    .forEach { file ->
                        val size = file.length()
                        val cat = categorize(file.name)
                        categorySizes[cat] = (categorySizes[cat] ?: 0L) + size
                        categoryCounts[cat] = (categoryCounts[cat] ?: 0) + 1

                        // Maintain top-10 largest files
                        if (largestHeap.size < 10) {
                            largestHeap.add(LargestFileEntry(file.absolutePath, file.name, size))
                        } else if (size > largestHeap.peek().size) {
                            largestHeap.poll()
                            largestHeap.add(LargestFileEntry(file.absolutePath, file.name, size))
                        }

                        scannedCount++
                        // Update progress every 500 files to avoid excessive Main-thread posts
                        if (scannedCount % 500 == 0L) {
                            val progress = (scannedCount.toFloat() / estimatedTotal.coerceAtLeast(scannedCount).toFloat() * 100)
                                .toInt().coerceIn(0, 99)
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    scanProgress = progress,
                                    scannedFileCount = scannedCount,
                                )
                            }
                        }
                    }

                // Sort largest descending
                val topFiles = largestHeap.toList().sortedByDescending { it.size }

                // Build category list sorted by size descending
                val categories = StorageCategory.entries.map { cat ->
                    CategoryStats(
                        category = cat,
                        totalSize = categorySizes[cat] ?: 0L,
                        fileCount = categoryCounts[cat] ?: 0,
                    )
                }.sortedByDescending { it.totalSize }

                val newState = AnalyzerUiState(
                    isScanning = false,
                    scanProgress = 100,
                    scannedFileCount = scannedCount,
                    totalUsedBytes = usedBytes,
                    categories = categories,
                    topLargestFiles = topFiles,
                )

                // Cache
                cachedState = newState
                cacheTimestamp = System.currentTimeMillis()

                _state.value = newState

                Log.d("StorageAnalyzer",
                    "Scan complete: $scannedCount files, " +
                    "${categories.sumOf { it.fileCount }} categorized, " +
                    "top file: ${topFiles.firstOrNull()?.name} (${topFiles.firstOrNull()?.size ?: 0})"
                )
            } catch (e: Exception) {
                Log.e("StorageAnalyzer", "Analysis failed", e)
                _state.value = _state.value.copy(isScanning = false, error = e.message)
            }
        }
    }

    /** Categorize a file based on its extension. */
    private fun categorize(filename: String): StorageCategory {
        val dotIndex = filename.lastIndexOf('.')
        if (dotIndex < 0) return StorageCategory.OTHER
        val ext = filename.substring(dotIndex + 1).lowercase()
        return when {
            ext in IMG_EXTS -> StorageCategory.IMAGES
            ext in VID_EXTS -> StorageCategory.VIDEOS
            ext in AUD_EXTS -> StorageCategory.AUDIO
            ext in DOC_EXTS -> StorageCategory.DOCUMENTS
            ext in ARC_EXTS -> StorageCategory.ARCHIVES
            ext == "apk" -> StorageCategory.APK
            else -> StorageCategory.OTHER
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
