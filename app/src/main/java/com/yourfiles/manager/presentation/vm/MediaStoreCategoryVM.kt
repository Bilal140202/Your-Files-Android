package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.yourfiles.manager.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Category types for MediaStore aggregated views.
 * Each type maps to a specific MediaStore URI and query.
 */
enum class CategoryType(val key: String, val label: String) {
    IMAGES("images", "Images"),
    VIDEOS("videos", "Videos"),
    AUDIO("audio", "Music"),
    DOCUMENTS("documents", "Documents"),
    APK("apk", "APKs"),
    ;

    companion object {
        fun fromKey(key: String): CategoryType =
            entries.firstOrNull { it.key == key } ?: IMAGES
    }
}

/**
 * Simple UI state for the category screen.
 */
data class CategoryUiState(
    val isLoading: Boolean = true,
    val items: List<FileItem> = emptyList(),
    val totalCount: Int = 0,
    val error: String? = null,
)

/**
 * ViewModel for MediaStore-based category screens.
 *
 * Loads ALL files of a given type from MediaStore in one shot on IO dispatcher.
 * MediaStore automatically covers both internal storage and SD card — no manual
 * volume iteration needed.
 *
 * NO Paging 3, NO LIMIT/OFFSET tokens — direct cursor query only.
 */
class MediaStoreCategoryVM(
    app: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

    private val categoryType: CategoryType = run {
        val typeKey = savedStateHandle.get<String>("categoryType") ?: "images"
        CategoryType.fromKey(typeKey)
    }

    val currentCategory: CategoryType get() = categoryType

    private val _state = MutableStateFlow(CategoryUiState())
    val state: StateFlow<CategoryUiState> = _state

    init {
        loadMedia()
    }

    /** Load all items from MediaStore on IO thread, update state on Main. */
    @Suppress("DEPRECATION") // MediaColumns.DATA is deprecated on Android 10+, but the app
    // holds MANAGE_EXTERNAL_STORAGE which grants all-files access, so DATA remains the most
    // pragmatic way to resolve file paths. Future migration should use ContentUris + ContentResolver.
    private fun loadMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = buildMediaStoreQuery(categoryType)
                val projection = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.MIME_TYPE,
                )

                val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

                val cursor = getApplication<Application>().contentResolver.query(
                    query.uri,
                    projection,
                    query.selection,
                    query.selectionArgs,
                    sortOrder,
                )

                val items = mutableListOf<FileItem>()
                cursor?.use {
                    val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val dateCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                    val dataCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    val mimeCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                    while (it.moveToNext()) {
                        val name = it.getString(nameCol) ?: continue
                        val dataPath = it.getString(dataCol) ?: continue
                        val size = it.getLong(sizeCol)
                        val dateModified = it.getLong(dateCol)
                        val mimeType = it.getString(mimeCol)

                        items.add(
                            FileItem(
                                name = name,
                                path = dataPath,
                                isDirectory = false,
                                size = size,
                                lastModified = dateModified * 1000L, // seconds → millis
                                mimeType = mimeType,
                            )
                        )
                    }
                }

                Log.d("MediaStoreVM", "Loaded ${items.size} ${categoryType.key} items")
                withContext(Dispatchers.Main) {
                    _state.value = CategoryUiState(
                        isLoading = false,
                        items = items,
                        totalCount = items.size,
                    )
                }
            } catch (e: Exception) {
                Log.e("MediaStoreVM", "Error loading ${categoryType.key}", e)
                withContext(Dispatchers.Main) {
                    _state.value = CategoryUiState(
                        isLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    fun retry() {
        _state.value = _state.value.copy(isLoading = true, error = null, items = emptyList())
        loadMedia()
    }

    /** Build the correct MediaStore URI, selection, and selectionArgs for each category. */
    private fun buildMediaStoreQuery(type: CategoryType): MediaStoreQuery {
        return when (type) {
            CategoryType.IMAGES -> MediaStoreQuery(
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            )
            CategoryType.VIDEOS -> MediaStoreQuery(
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            )
            CategoryType.AUDIO -> MediaStoreQuery(
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            )
            CategoryType.DOCUMENTS -> {
                val mimeTypes = listOf(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.ms-excel",
                    "application/vnd.ms-powerpoint",
                    "text/plain",
                    "text/csv",
                    "application/rtf",
                )
                val placeholders = mimeTypes.map { "?" }.joinToString(",")
                MediaStoreQuery(
                    uri = MediaStore.Files.getContentUri("external"),
                    selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN ($placeholders) " +
                        "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_NONE}",
                    selectionArgs = mimeTypes.toTypedArray(),
                )
            }
            CategoryType.APK -> {
                MediaStoreQuery(
                    uri = MediaStore.Files.getContentUri("external"),
                    selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ?",
                    selectionArgs = arrayOf("application/vnd.android.package-archive"),
                )
            }
        }
    }
}

/** Holds the MediaStore query parameters. */
private data class MediaStoreQuery(
    val uri: android.net.Uri,
    val selection: String? = null,
    val selectionArgs: Array<String>? = null,
)
