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

// ===== Enums =====

enum class AnalyzerPhase { HOME, SCANNING, RESULTS }

enum class ScanOption(val label: String) {
    LARGE_FILES("Large Files"),
    REDUNDANT("Redundant"),
    RECENTLY_CREATED("Recently Created"),
    ALL_FILES("All Files"),
    APP_FOLDERS("App Folders"),
}

enum class StorageCategory(val label: String) {
    IMAGES("Pictures"),
    VIDEOS("Videos"),
    AUDIO("Audio"),
    DOCUMENTS("Documents"),
    APK("APKs"),
    ARCHIVES("Archives"),
    OTHER("Others"),
}

// ===== Data classes =====

data class CategoryStats(
    val category: StorageCategory,
    val totalSize: Long,
    val fileCount: Int,
)

data class FolderSizeEntry(
    val path: String,
    val name: String,
    val size: Long,
)

data class AnalyzerUiState(
    val phase: AnalyzerPhase = AnalyzerPhase.HOME,
    val scanProgress: Int = 0,
    val scannedCount: Long = 0L,
    val totalCapacity: Long = 0L,
    val usedBytes: Long = 0L,
    val categories: List<CategoryStats> = emptyList(),
    val folderSizes: List<FolderSizeEntry> = emptyList(),
    val error: String? = null,
)

// ===== ViewModel =====

class StorageAnalyzerVM(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AnalyzerUiState())
    val state: StateFlow<AnalyzerUiState> = _state.asStateFlow()

    private val rootPath = Environment.getExternalStorageDirectory().absolutePath
    private var scanJob: Job? = null
    private var cacheTimestamp: Long = 0L
    private var cachedState: AnalyzerUiState? = null

    val hasAllFilesAccess: Boolean
        get() = Environment.isExternalStorageManager()

    fun startAnalysis() {
        val now = System.currentTimeMillis()
        if (cachedState != null && (now - cacheTimestamp) < 3600_000L) {
            _state.value = cachedState!!
            return
        }

        scanJob?.cancel()
        _state.value = AnalyzerUiState(phase = AnalyzerPhase.SCANNING)
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val rootDir = File(rootPath)
                if (!rootDir.exists() || !rootDir.isDirectory) {
                    _state.value = _state.value.copy(
                        phase = AnalyzerPhase.HOME,
                        error = "Storage not accessible",
                    )
                    return@launch
                }

                val statFs = StatFs(rootPath)
                val totalCapacity = statFs.totalBytes
                val usedBytes = totalCapacity - statFs.availableBytes

                val catSizes = StorageCategory.entries.associateWith { 0L }.toMutableMap()
                val catCounts = StorageCategory.entries.associateWith { 0 }.toMutableMap()
                val folderSizeMap = mutableMapOf<String, Long>()

                var scannedCount = 0L
                val rootChildren = rootDir.listFiles()?.filter { !it.name.startsWith(".") } ?: emptyList()
                val estimatedTotal = (rootChildren.size * 20).toLong().coerceAtLeast(1000L)

                rootDir.walkTopDown()
                    .onEnter { dir -> !dir.name.startsWith(".") }
                    .filter { it.isFile }
                    .forEach { file ->
                        val size = file.length()
                        val cat = categorize(file.name)
                        catSizes[cat] = (catSizes[cat] ?: 0L) + size
                        catCounts[cat] = (catCounts[cat] ?: 0) + 1

                        // Accumulate per top-level folder
                        val relPath = file.absolutePath.removePrefix(rootPath).removePrefix("/")
                        val firstSeg = relPath.split("/").firstOrNull() ?: ""
                        if (firstSeg.isNotEmpty()) {
                            folderSizeMap[firstSeg] = (folderSizeMap[firstSeg] ?: 0L) + size
                        }

                        scannedCount++
                        if (scannedCount % 500L == 0L) {
                            val pct = (scannedCount.toFloat()
                                / estimatedTotal.coerceAtLeast(scannedCount) * 100)
                                .toInt().coerceIn(0, 99)
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    scanProgress = pct,
                                    scannedCount = scannedCount,
                                    totalCapacity = totalCapacity,
                                    usedBytes = usedBytes,
                                )
                            }
                        }
                    }

                val categories = StorageCategory.entries.map { cat ->
                    CategoryStats(cat, catSizes[cat] ?: 0L, catCounts[cat] ?: 0)
                }.sortedByDescending { it.totalSize }

                val folderSizes = folderSizeMap.entries
                    .map { (name, size) -> FolderSizeEntry("$rootPath/$name", name, size) }
                    .sortedByDescending { it.size }

                val newState = AnalyzerUiState(
                    phase = AnalyzerPhase.RESULTS,
                    scanProgress = 100,
                    scannedCount = scannedCount,
                    totalCapacity = totalCapacity,
                    usedBytes = usedBytes,
                    categories = categories,
                    folderSizes = folderSizes,
                )

                cachedState = newState
                cacheTimestamp = System.currentTimeMillis()
                _state.value = newState

                Log.d("Analyzer", "Done: $scannedCount files, ${folderSizes.size} folders")
            } catch (e: Exception) {
                Log.e("Analyzer", "Scan failed", e)
                _state.value = _state.value.copy(
                    phase = AnalyzerPhase.HOME,
                    error = e.message,
                )
            }
        }
    }

    fun refresh() {
        cachedState = null
        startAnalysis()
    }

    private fun categorize(filename: String): StorageCategory {
        val dot = filename.lastIndexOf('.')
        if (dot < 0) return StorageCategory.OTHER
        val ext = filename.substring(dot + 1).lowercase()
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
        val IMG_EXTS = setOf(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif", "raw", "tiff"
        )
        val VID_EXTS = setOf(
            "mp4", "mkv", "avi", "mov", "3gp", "flv", "wmv", "webm", "ts", "m4v"
        )
        val AUD_EXTS = setOf(
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus", "amr"
        )
        val DOC_EXTS = setOf(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "rtf", "html", "htm", "xml", "json",
        )
        val ARC_EXTS = setOf(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg"
        )

        fun extensionsFor(category: StorageCategory): Set<String> = when (category) {
            StorageCategory.IMAGES -> IMG_EXTS
            StorageCategory.VIDEOS -> VID_EXTS
            StorageCategory.AUDIO -> AUD_EXTS
            StorageCategory.DOCUMENTS -> DOC_EXTS
            StorageCategory.APK -> setOf("apk")
            StorageCategory.ARCHIVES -> ARC_EXTS
            StorageCategory.OTHER -> emptySet()
        }
    }
}