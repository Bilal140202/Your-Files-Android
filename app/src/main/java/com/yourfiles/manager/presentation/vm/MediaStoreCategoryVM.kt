package com.yourfiles.manager.presentation.vm

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.yourfiles.manager.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
 * ViewModel for MediaStore-based category screens.
 * Uses Paging 3 to lazily load files from MediaStore,
 * automatically covering both internal storage and SD card.
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

    /** Total count of items in this category, refreshed on each query. */
    var totalCount: Int = 0
        private set

    /**
     * PagingData flow — automatically handles loading pages of 50 items
     * from MediaStore on IO dispatcher, cached across config changes.
     */
    val pagedItems: StateFlow<androidx.paging.PagingData<FileItem>> =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                initialLoadSize = 100,
                prefetchDistance = 30,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MediaStorePagingSource(
                    context = getApplication(),
                    categoryType = categoryType,
                    onCountLoaded = { count -> totalCount = count },
                )
            }
        )
            .flow
            .cachedIn(viewModelScope)
            .stateIn(viewModelScope, SharingStarted.Lazily, androidx.paging.PagingData.empty())
}

/**
 * PagingSource that queries MediaStore for a specific [CategoryType].
 * MediaStore automatically aggregates files from ALL mounted volumes
 * (internal + SD card), so no manual volume iteration is needed.
 *
 * Each page is a [LoadResult.Page] of [FileItem] objects, sorted by
 * DATE_MODIFIED DESC (newest first) as per ES File Explorer behavior.
 */
private class MediaStorePagingSource(
    private val context: android.content.Context,
    private val categoryType: CategoryType,
    private val onCountLoaded: (Int) -> Unit,
) : PagingSource<Int, FileItem>() {

    override fun getRefreshKey(state: PagingState<Int, FileItem>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FileItem> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            val query = buildMediaStoreQuery(categoryType)
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_MODIFIED,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE,
            )

            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT $pageSize OFFSET $page"

            val cursor = context.contentResolver.query(
                query.uri,
                projection,
                query.selection,
                query.selectionArgs,
                sortOrder,
            )

            val items = mutableListOf<FileItem>()
            var nextKey: Int? = null

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
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

                // If we got a full page, there might be more
                if (items.size == pageSize) {
                    nextKey = page + pageSize
                }
            }

            // On first load, query total count
            if (page == 0) {
                totalCount(context, categoryType, query).let { onCountLoaded(it) }
            }

            LoadResult.Page(
                data = items,
                prevKey = if (page > 0) page - pageSize else null,
                nextKey = nextKey,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /** Get the total count of items for this category. */
    private fun totalCount(
        context: android.content.Context,
        type: CategoryType,
        query: MediaStoreQuery,
    ): Int {
        return try {
            context.contentResolver.query(
                query.uri,
                arrayOf("COUNT(*)"),
                query.selection,
                query.selectionArgs,
                null,
            )?.use { it.moveToFirst(); it.getInt(0) } ?: 0
        } catch (_: Exception) { 0 }
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
                // Query Files with document MIME types
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
