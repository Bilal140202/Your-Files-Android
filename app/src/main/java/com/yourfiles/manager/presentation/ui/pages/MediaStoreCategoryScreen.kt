package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.yourfiles.manager.app.App
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.domain.model.FileItem
import com.yourfiles.manager.presentation.vm.CategoryType
import com.yourfiles.manager.presentation.vm.MediaStoreCategoryVM
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen flat list of files from MediaStore for a given [CategoryType].
 *
 * Matches ES File Explorer 2014 behavior:
 * - Shows ALL files of a type from Internal + SD, flat list, no folders
 * - Sorted by date modified DESC (newest first)
 * - Thumbnails for images/videos via Coil
 * - Paginated — first 100 items load instantly, rest on scroll
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaStoreCategoryScreen(
    categoryType: CategoryType,
    viewModel: MediaStoreCategoryVM = viewModel(),
) {
    val pagingItems = viewModel.pagedItems.collectAsLazyPagingItems()

    // Track total count from ViewModel
    var totalCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(viewModel.totalCount) {
        totalCount = viewModel.totalCount
    }

    // Refresh count when items finish loading
    val loadState = pagingItems.loadState
    LaunchedEffect(loadState.refresh) {
        if (loadState.refresh is LoadState.NotLoading) {
            totalCount = viewModel.totalCount
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = categoryType.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (totalCount > 0) {
                            Text(
                                text = "%,d items".format(totalCount),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        App.instance.navController().popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                // First load — show spinner
                loadState.refresh is LoadState.Loading && pagingItems.itemCount == 0 -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                // Error on refresh
                loadState.refresh is LoadState.Error -> {
                    val error = (loadState.refresh as LoadState.Error).error
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = error.message ?: "Error loading files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { pagingItems.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                // Empty result
                loadState.refresh is LoadState.NotLoading && pagingItems.itemCount == 0 -> {
                    Text(
                        text = "No ${categoryType.label.lowercase()} found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                // Normal — show the list
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 0.dp),
                    ) {
                        items(
                            count = pagingItems.itemCount,
                            key = { index ->
                                pagingItems[index]?.path ?: index
                            },
                        ) { index ->
                            val item = pagingItems[index]
                            if (item != null) {
                                CategoryFileRow(
                                    item = item,
                                    categoryType = categoryType,
                                    onClick = {
                                        // Open file in viewer
                                        App.instance.navController().navigate(
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

                        // Load more indicator at bottom
                        if (loadState.append is LoadState.Loading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }

                        // Append error
                        if (loadState.append is LoadState.Error) {
                            item {
                                TextButton(
                                    onClick = { pagingItems.retry() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Load more failed. Tap to retry.")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single row for a file in the category list.
 * Shows thumbnail (for images/videos) or icon, name, size, and date.
 */
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
        // Thumbnail or icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            when (categoryType) {
                CategoryType.IMAGES -> {
                    // Coil thumbnail for images
                    AsyncImage(
                        model = item.path,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                }
                CategoryType.VIDEOS -> {
                    // Coil can decode video frames
                    AsyncImage(
                        model = item.path,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                }
                else -> {
                    // Use Material icon
                    Icon(
                        imageVector = getCategoryIcon(categoryType),
                        contentDescription = null,
                        tint = getCategoryColor(categoryType),
                        modifier = Modifier.size(24.dp),
                    )
                }
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

private fun getCategoryIcon(type: CategoryType): ImageVector = when (type) {
    CategoryType.IMAGES -> Icons.Outlined.Image
    CategoryType.VIDEOS -> Icons.Outlined.Movie
    CategoryType.AUDIO -> Icons.Outlined.MusicNote
    CategoryType.DOCUMENTS -> Icons.Outlined.Description
    CategoryType.APK -> Icons.Outlined.Memory
}

private fun getCategoryColor(type: CategoryType): Color = when (type) {
    CategoryType.IMAGES -> Color(0xFFE91E63)
    CategoryType.VIDEOS -> Color(0xFF9C27B0)
    CategoryType.AUDIO -> Color(0xFFFF9800)
    CategoryType.DOCUMENTS -> Color(0xFF2196F3)
    CategoryType.APK -> Color(0xFF4CAF50)
}
