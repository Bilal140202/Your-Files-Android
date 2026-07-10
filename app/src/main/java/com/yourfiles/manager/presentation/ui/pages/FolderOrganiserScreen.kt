package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.R
import com.yourfiles.manager.app.uim3.theme.AppColors
import com.yourfiles.manager.app.uim3.theme.getCategoryColorForFileCategory
import com.yourfiles.manager.presentation.ui.components.common.EmptyStateView
import com.yourfiles.manager.presentation.ui.components.common.ErrorStateView
import com.yourfiles.manager.presentation.ui.components.common.LoadingStateView
import com.yourfiles.manager.presentation.ui.components.common.ScreenScaffold
import com.yourfiles.manager.presentation.vm.FileCategory
import com.yourfiles.manager.presentation.vm.FolderOrganiserState
import com.yourfiles.manager.presentation.vm.FolderOrganiserViewModel
import com.yourfiles.manager.presentation.vm.GroupHeader
import com.yourfiles.manager.presentation.vm.OrganiserFileItem
import com.yourfiles.manager.presentation.vm.SortOption

@Composable
fun FolderOrganiserScreen(
    viewModel: FolderOrganiserViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    val navController = com.yourfiles.manager.app.LocalNavController.current

    ScreenScaffold(
        title = stringResource(R.string.organise_title, state.folderName),
        onBack = { navController.popBackStack() },
        actions = {
            Box {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = stringResource(R.string.cd_sort),
                    )
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                ) {
                    SortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                viewModel.setSortOption(option)
                                showSortMenu = false
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // --- Summary bar ---
            SummaryBar(state = state, context = context)

            // --- Group by type toggle ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.organise_group_by_type),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = state.groupByType,
                    onCheckedChange = { viewModel.toggleGroupByType() },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Content ---
            if (state.isLoading) {
                LoadingStateView()
            } else if (state.error != null) {
                ErrorStateView(message = state.error ?: stringResource(R.string.organise_unknown_error))
            } else {
                val displayItems = state.displayItems
                if (displayItems.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Outlined.FolderOpen,
                        title = stringResource(R.string.organise_empty_folder),
                        subtitle = stringResource(R.string.organise_no_files),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = displayItems,
                            key = { item ->
                                when (item) {
                                    is OrganiserFileItem -> item.path
                                    is GroupHeader -> "header_${item.label}"
                                    else -> item.toString()
                                }
                            },
                        ) { item ->
                            when (item) {
                                is GroupHeader -> {
                                    GroupHeaderRow(header = item)
                                }
                                is OrganiserFileItem -> {
                                    FileRow(
                                        item = item,
                                        formattedDate = viewModel.formatDate(item.lastModified),
                                        formattedSize = if (item.isDirectory) "" else Formatter.formatFileSize(context, item.size),
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
}

@Composable
private fun SummaryBar(state: FolderOrganiserState, context: android.content.Context) {
    val totalSizeStr = Formatter.formatFileSize(context, state.totalSize)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.organise_file_count, state.fileCount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = totalSizeStr,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.organise_sort_label, state.sortOption.label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GroupHeaderRow(header: GroupHeader) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = header.label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.organise_group_header_count, header.count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FileRow(
    item: OrganiserFileItem,
    formattedDate: String,
    formattedSize: String,
) {
    val icon = getFileIconFor(item)
    val iconColor = getFileIconColorFor(item)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (formattedSize.isNotEmpty()) {
                    Text(
                        text = formattedSize,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (formattedDate.isNotEmpty()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// ===== ICON HELPERS (consistent with FileBrowserScreen) =====

private fun getFileIconFor(item: OrganiserFileItem): ImageVector {
    return if (item.isDirectory) {
        Icons.Outlined.Folder
    } else {
        when (item.category) {
            FileCategory.IMAGES -> Icons.Outlined.Image
            FileCategory.VIDEOS -> Icons.Outlined.Movie
            FileCategory.AUDIO -> Icons.Outlined.MusicNote
            FileCategory.DOCUMENTS -> Icons.Outlined.Description
            FileCategory.APK -> Icons.Outlined.Android
            FileCategory.ARCHIVES -> Icons.Outlined.Archive
            FileCategory.OTHER -> Icons.AutoMirrored.Outlined.InsertDriveFile
        }
    }
}

private fun getFileIconColorFor(item: OrganiserFileItem): Color {
    return if (item.isDirectory) {
        AppColors.FolderColor
    } else {
        getCategoryColorForFileCategory(item.category)
    }
}