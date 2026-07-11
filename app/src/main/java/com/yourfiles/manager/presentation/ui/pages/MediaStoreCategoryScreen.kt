package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.LocalNavController
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.uim3.theme.getCategoryColor
import com.yourfiles.manager.app.uim3.theme.getCategoryIcon
import com.yourfiles.manager.domain.model.FileItem
import com.yourfiles.manager.presentation.ui.components.common.EmptyStateView
import com.yourfiles.manager.presentation.ui.components.common.ErrorStateView
import com.yourfiles.manager.presentation.ui.components.common.LoadingStateView
import com.yourfiles.manager.presentation.ui.components.common.ScreenScaffold
import com.yourfiles.manager.presentation.ui.components.common.thumbnail.FileThumbnailCompose
import com.yourfiles.manager.presentation.vm.CategoryType
import com.yourfiles.manager.presentation.vm.MediaStoreCategoryVM
import com.yourfiles.manager.presentation.vm.SortType
import com.yourfiles.manager.presentation.vm.ViewMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen file list for a given [CategoryType] from MediaStore.
 *
 * ES File Explorer / MiXplorer style features:
 * - List + Grid view toggle
 * - Sort options (name, size, date)
 * - Proper thumbnails for images, videos, APKs
 * - Flat list of all files of a type
 */
@Composable
fun MediaStoreCategoryScreen(
    categoryType: CategoryType,
    onOpenDrawer: () -> Unit = {},
    viewModel: MediaStoreCategoryVM = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val navController = LocalNavController.current
    val context = LocalContext.current

    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var sortType by remember { mutableStateOf(SortType.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Apply sorting to items
    val sortedItems = remember(state.items, sortType) {
        when (sortType) {
            SortType.NAME_ASC  -> state.items.sortedBy { it.name.lowercase() }
            SortType.NAME_DESC -> state.items.sortedByDescending { it.name.lowercase() }
            SortType.SIZE_ASC  -> state.items.sortedBy { it.size }
            SortType.SIZE_DESC -> state.items.sortedByDescending { it.size }
            SortType.DATE_ASC  -> state.items.sortedBy { it.lastModified }
            SortType.DATE_DESC -> state.items.sortedByDescending { it.lastModified }
            SortType.TYPE_ASC  -> state.items.sortedBy { it.name.substringAfterLast('.', "").lowercase() }
            SortType.TYPE_DESC -> state.items.sortedByDescending { it.name.substringAfterLast('.', "").lowercase() }
        }
    }

    val subtitle = if (state.totalCount > 0) "%,d items".format(state.totalCount) else null

    ScreenScaffold(
        title = categoryType.label,
        subtitle = subtitle,
        onOpenDrawer = onOpenDrawer,
        actions = {
            // Sort button
            IconButton(onClick = { showSortMenu = true }) {
                Icon(
                    Icons.Outlined.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            // View toggle
            IconButton(onClick = {
                viewMode = if (viewMode == ViewMode.LIST) ViewMode.GRID else ViewMode.LIST
            }) {
                Icon(
                    if (viewMode == ViewMode.GRID) Icons.Outlined.ViewList else Icons.Outlined.GridView,
                    contentDescription = "Toggle view",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            // Sort dropdown
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                SortType.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = { Text(sort.label) },
                        onClick = {
                            sortType = sort
                            showSortMenu = false
                        },
                        trailingIcon = {
                            if (sortType == sort) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    LoadingStateView()
                }
                state.error != null -> {
                    ErrorStateView(
                        message = state.error ?: "Error",
                        onRetry = { viewModel.retry() },
                    )
                }
                sortedItems.isEmpty() -> {
                    EmptyStateView(
                        title = "No ${categoryType.label.lowercase()} found",
                    )
                }
                else -> {
                    if (viewMode == ViewMode.GRID) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            gridItems(sortedItems, key = { it.path }) { item ->
                                CategoryGridItem(
                                    item = item,
                                    categoryType = categoryType,
                                    onClick = {
                                        navController.navigate(
                                            "${Routes.FILE_DETAIL_VIEWER}?url=${android.net.Uri.encode(item.path)}&category=file"
                                        )
                                    },
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 0.dp),
                        ) {
                            items(sortedItems, key = { it.path }) { item ->
                                CategoryFileRow(
                                    item = item,
                                    categoryType = categoryType,
                                    onClick = {
                                        navController.navigate(
                                            "${Routes.FILE_DETAIL_VIEWER}?url=${android.net.Uri.encode(item.path)}&category=file"
                                        )
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===== GRID ITEM FOR CATEGORY (ES / MiXplorer style) =====

@Composable
private fun CategoryGridItem(
    item: FileItem,
    categoryType: CategoryType,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val dateStr = if (item.lastModified > 0) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.lastModified))
    } else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Thumbnail area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceDim),
            contentAlignment = Alignment.Center,
        ) {
            // Use FileThumbnailCompose for all types - handles image, video, apk automatically
            FileThumbnailCompose(
                model = item.path,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // File name
        Text(
            text = item.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Size
        Text(
            text = Formatter.formatShortFileSize(context, item.size),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            fontSize = 10.sp,
        )
    }
}

// ===== LIST ROW FOR CATEGORY =====

@Composable
private fun CategoryFileRow(
    item: FileItem,
    categoryType: CategoryType,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val dateStr = if (item.lastModified > 0) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.lastModified))
    } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail: use real thumbnails for images/videos, icon for others
        val ext = item.name.substringAfterLast('.', "").lowercase()
        val isMediaFile = ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") ||
            ext in listOf("mp4", "mkv", "webm", "avi", "mov", "3gp", "flv") ||
            ext == "apk"

        if (isMediaFile) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                FileThumbnailCompose(
                    model = item.path,
                    modifier = Modifier.size(40.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = getCategoryIcon(categoryType),
                    contentDescription = null,
                    tint = getCategoryColor(categoryType),
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name + meta
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Text(
                text = Formatter.formatShortFileSize(context, item.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            if (dateStr.isNotEmpty()) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontSize = 10.sp,
                )
            }
        }
    }
}