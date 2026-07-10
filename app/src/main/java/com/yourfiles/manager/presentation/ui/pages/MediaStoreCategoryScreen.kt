package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.yourfiles.manager.app.LocalNavController
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.uim3.theme.getCategoryColor
import com.yourfiles.manager.app.uim3.theme.getCategoryIcon
import com.yourfiles.manager.domain.model.FileItem
import com.yourfiles.manager.presentation.ui.components.common.EmptyStateView
import com.yourfiles.manager.presentation.ui.components.common.ErrorStateView
import com.yourfiles.manager.presentation.ui.components.common.LoadingStateView
import com.yourfiles.manager.presentation.ui.components.common.ScreenScaffold
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
 * - Direct MediaStore query, no Paging 3 tokens
 */
@Composable
fun MediaStoreCategoryScreen(
    categoryType: CategoryType,
    viewModel: MediaStoreCategoryVM = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val navController = LocalNavController.current
    val subtitle = if (state.totalCount > 0) "%,d items".format(state.totalCount) else null

    ScreenScaffold(
        title = categoryType.label,
        subtitle = subtitle,
        onBack = { navController.popBackStack() },
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
                state.items.isEmpty() -> {
                    EmptyStateView(
                        title = "No ${categoryType.label.lowercase()} found",
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 0.dp),
                    ) {
                        items(state.items, key = { it.path }) { item ->
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
                    AsyncImage(
                        model = item.path,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                }
                CategoryType.VIDEOS -> {
                    AsyncImage(
                        model = item.path,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                    )
                }
                else -> {
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