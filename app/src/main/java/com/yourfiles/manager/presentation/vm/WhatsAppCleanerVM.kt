package com.yourfiles.manager.presentation.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourfiles.manager.data.model.LocalFile
import com.yourfiles.manager.utils.TrashManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GraphicEq

// ────────────────────────────────────────────────────────────────────────────────
// Constants
// ────────────────────────────────────────────────────────────────────────────────

private const val WHATSAPP_MEDIA_BASE = "/storage/emulated/0/WhatsApp/WhatsApp Media"

enum class WhatsAppCategory(
    val label: String,
    val icon: ImageVector,
    val relativePath: String,
    val extensions: Set<String>,
) {
    IMAGES(
        label = "Images",
        icon = Icons.Filled.Image,
        relativePath = "WhatsApp Images",
        extensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp"),
    ),
    VIDEOS(
        label = "Videos",
        icon = Icons.Filled.VideoFile,
        relativePath = "WhatsApp Video",
        extensions = setOf("mp4", "3gp", "avi", "mkv", "webm"),
    ),
    DOCUMENTS(
        label = "Documents",
        icon = Icons.Outlined.Description,
        relativePath = "WhatsApp Documents",
        extensions = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip", "rar"),
    ),
    VOICE_NOTES(
        label = "Voice Notes",
        icon = Icons.Outlined.GraphicEq,
        relativePath = "WhatsApp Audio",
        extensions = setOf("opus", "mp3", "aac", "m4a", "ogg", "wav"),
    ),
}

// ────────────────────────────────────────────────────────────────────────────────
// ViewModel
// ────────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the WhatsApp Cleaner page.
 *
 * Scans the WhatsApp media directory on the device and categorises files by type
 * (Images, Videos, Documents, Voice Notes). Extends [SelectableDeletableVM] to
 * reuse the app-wide select-and-delete workflow.
 */
class WhatsAppCleanerVM : SelectableDeletableVM() {

    // ── Category filter state ──────────────────────────────────────────────────
    var activeCategory by mutableStateOf(WhatsAppCategory.IMAGES)
        private set

    fun selectCategory(category: WhatsAppCategory) {
        activeCategory = category
    }

    // ── Scanned files state ───────────────────────────────────────────────────
    private val _allFiles = MutableStateFlow<Map<WhatsAppCategory, List<LocalFile>>>(emptyMap())
    val allFiles: StateFlow<Map<WhatsAppCategory, List<LocalFile>>> = _allFiles.asStateFlow()

    /** Files for the currently selected category. */
    val currentCategoryFiles: List<LocalFile>
        get() = _allFiles.value[activeCategory] ?: emptyList()

    /** Whether the initial scan is still running. */
    val isScanning = mutableStateOf(true)

    /** WhatsApp folder exists on this device. */
    val whatsappExists = mutableStateOf(true)

    init {
        scanMedia()
    }

    /**
     * Override the parent's confirmDeleteFiles to also refresh the file list
     * after deletion, since WhatsApp files are not tracked in the database.
     */
    override fun confirmDeleteFiles() {
        val filesToDelete = pendingDeleteFiles
        showDeleteDialog.value = false

        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = filesToDelete.sumOf { File(it).length() }
            withContext(Dispatchers.Main) { isDeleting.value = true }

            // Move to trash instead of permanent delete
            TrashManager.moveToTrash(filesToDelete)

            // Refresh scanned files (parent's DB delete is a no-op for WhatsApp files)
            scanMediaInternal()

            withContext(Dispatchers.Main) {
                selectedFiles.value -= filesToDelete
                pendingDeleteFiles = emptySet()
                showDeleteDialog.value = false
                selectedModeOn.value = false
                isDeleting.value = false
            }
        }
    }

    private fun scanMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            scanMediaInternal()
        }
    }

    private suspend fun scanMediaInternal() {
        withContext(Dispatchers.Main) { isScanning.value = true }

        val baseDir = File(WHATSAPP_MEDIA_BASE)
        if (!baseDir.exists() || !baseDir.isDirectory) {
            withContext(Dispatchers.Main) {
                whatsappExists.value = false
                isScanning.value = false
            }
            return
        }

        val result = mutableMapOf<WhatsAppCategory, List<LocalFile>>()

        for (category in WhatsAppCategory.entries) {
            val categoryDir = File(baseDir, category.relativePath)
            val files = mutableListOf<LocalFile>()

            if (categoryDir.exists() && categoryDir.isDirectory) {
                // Recursively scan subdirectories (e.g., Sent, Private)
                scanDirectoryRecursive(categoryDir, category.extensions, files)
            }

            // Sort newest first
            result[category] = files.sortedByDescending { it.modifiedTime ?: 0L }
        }

        _allFiles.value = result
        withContext(Dispatchers.Main) { isScanning.value = false }
    }

    private fun scanDirectoryRecursive(
        dir: File,
        allowedExtensions: Set<String>,
        accumulator: MutableList<LocalFile>,
    ) {
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                scanDirectoryRecursive(child, allowedExtensions, accumulator)
            } else if (child.isFile) {
                val ext = child.extension.lowercase()
                if (ext in allowedExtensions) {
                    accumulator.add(
                        LocalFile(
                            fileType = getMimeTypeSimple(child),
                            modifiedTime = child.lastModified(),
                            fileName = child.name,
                            size = child.length(),
                            md5CheckSum = null,
                            id = child.absolutePath,
                        )
                    )
                }
            }
        }
    }

    /** Simple MIME type detection from extension. */
    private fun getMimeTypeSimple(file: File): String? {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "mp4" -> "video/mp4"
            "3gp" -> "video/3gpp"
            "avi" -> "video/avi"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "opus" -> "audio/opus"
            "mp3" -> "audio/mpeg"
            "aac" -> "audio/aac"
            "m4a" -> "audio/mp4"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            else -> "application/octet-stream"
        }
    }
}
