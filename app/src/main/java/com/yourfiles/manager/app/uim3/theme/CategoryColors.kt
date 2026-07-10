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
import androidx.compose.ui.graphics.vector.ImageVector
import com.yourfiles.manager.presentation.vm.CategoryType
import com.yourfiles.manager.presentation.vm.FileCategory
import com.yourfiles.manager.presentation.vm.StorageCategory

/** Colors for each storage category. */
internal val CATEGORY_COLORS = mapOf(
    StorageCategory.IMAGES to AppColors.CategoryImages,
    StorageCategory.VIDEOS to AppColors.CategoryVideos,
    StorageCategory.AUDIO to AppColors.CategoryAudio,
    StorageCategory.DOCUMENTS to AppColors.CategoryDocuments,
    StorageCategory.APK to AppColors.CategoryApk,
    StorageCategory.ARCHIVES to AppColors.CategoryArchives,
    StorageCategory.OTHER to AppColors.CategoryOther,
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
val FolderColor: Color get() = AppColors.FolderColor

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

// ── CategoryType (MediaStore) helpers ─────────────────────────────────────

private val CATEGORY_TYPE_TO_STORAGE = mapOf(
    CategoryType.IMAGES to StorageCategory.IMAGES,
    CategoryType.VIDEOS to StorageCategory.VIDEOS,
    CategoryType.AUDIO to StorageCategory.AUDIO,
    CategoryType.DOCUMENTS to StorageCategory.DOCUMENTS,
    CategoryType.APK to StorageCategory.APK,
)

/** Get the category color for a [CategoryType] (used by MediaStore screens). */
fun getCategoryColor(type: CategoryType): Color {
    return CATEGORY_COLORS[CATEGORY_TYPE_TO_STORAGE[type]] ?: AppColors.CategoryOther
}

/** Get the category icon for a [CategoryType] (used by MediaStore screens). */
fun getCategoryIcon(type: CategoryType): ImageVector {
    return CATEGORY_ICONS[CATEGORY_TYPE_TO_STORAGE[type]] ?: Icons.Outlined.QuestionMark
}

// ── FileCategory (FolderOrganiser) helpers ────────────────────────────────

private val FILE_CATEGORY_TO_STORAGE = mapOf(
    FileCategory.IMAGES to StorageCategory.IMAGES,
    FileCategory.VIDEOS to StorageCategory.VIDEOS,
    FileCategory.AUDIO to StorageCategory.AUDIO,
    FileCategory.DOCUMENTS to StorageCategory.DOCUMENTS,
    FileCategory.APK to StorageCategory.APK,
    FileCategory.ARCHIVES to StorageCategory.ARCHIVES,
    FileCategory.OTHER to StorageCategory.OTHER,
)

/** Get the category color for a [FileCategory] (used by FolderOrganiser screen). */
fun getCategoryColorForFileCategory(type: FileCategory): Color {
    return CATEGORY_COLORS[FILE_CATEGORY_TO_STORAGE[type]] ?: AppColors.CategoryOther
}