package com.yourfiles.manager.app.uim3.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.ui.graphics.Color
import com.yourfiles.manager.presentation.vm.StorageCategory

/** Colors for each storage category. */
internal val CATEGORY_COLORS = mapOf(
    StorageCategory.IMAGES to Color(0xFFE91E63),
    StorageCategory.VIDEOS to Color(0xFF9C27B0),
    StorageCategory.AUDIO to Color(0xFFFF9800),
    StorageCategory.DOCUMENTS to Color(0xFF2196F3),
    StorageCategory.APK to Color(0xFF4CAF50),
    StorageCategory.ARCHIVES to Color(0xFF795548),
    StorageCategory.OTHER to Color(0xFF607D8B),
)

/** Icons for each storage category. */
internal val CATEGORY_ICONS = mapOf(
    StorageCategory.IMAGES to Icons.Outlined.Image,
    StorageCategory.VIDEOS to Icons.Outlined.Movie,
    StorageCategory.AUDIO to Icons.Outlined.MusicNote,
    StorageCategory.DOCUMENTS to Icons.Outlined.Description,
    StorageCategory.APK to Icons.Outlined.Memory,
    StorageCategory.ARCHIVES to Icons.Outlined.Archive,
    StorageCategory.OTHER to Icons.Outlined.QuestionMark,
)

/** Category → Android folder path for "open filtered" navigation. */
internal val CATEGORY_FOLDERS = mapOf(
    StorageCategory.IMAGES to "Pictures",
    StorageCategory.VIDEOS to "Movies",
    StorageCategory.AUDIO to "Music",
    StorageCategory.DOCUMENTS to "Documents",
    StorageCategory.APK to "Download",
    StorageCategory.ARCHIVES to "Download",
    StorageCategory.OTHER to null,
)

/** Color used for folder icons in the file browser. */
val FolderColor = Color(0xFFFF9800)

/** Image file extensions (lowercase, with leading dot). */
private val IMAGE_EXTENSIONS = setOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp")

/** Video file extensions (lowercase, with leading dot). */
private val VIDEO_EXTENSIONS = setOf(".mp4", ".mkv", ".avi", ".mov", ".3gp", ".flv")

/** Audio file extensions (lowercase, with leading dot). */
private val AUDIO_EXTENSIONS = setOf(".mp3", ".wav", ".flac", ".aac", ".ogg")

/**
 * Returns the centralized category color for a given file extension
 * (lowercase, with leading dot, e.g. ".jpg").
 * Falls back to the OTHER category color for unrecognized extensions.
 */
fun categoryColorForExtension(extension: String): Color {
    return when {
        extension in IMAGE_EXTENSIONS -> CATEGORY_COLORS[StorageCategory.IMAGES]!!
        extension in VIDEO_EXTENSIONS -> CATEGORY_COLORS[StorageCategory.VIDEOS]!!
        extension in AUDIO_EXTENSIONS -> CATEGORY_COLORS[StorageCategory.AUDIO]!!
        else -> CATEGORY_COLORS[StorageCategory.OTHER]!!
    }
}