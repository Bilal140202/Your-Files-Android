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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.presentation.vm.FileCategory
import com.yourfiles.manager.presentation.vm.FolderOrganiserState
import com.yourfiles.manager.presentation.vm.FolderOrganiserViewModel
import com.yourfiles.manager.presentation.vm.GroupHeader
import com.yourfiles.manager.presentation.vm.OrganiserFileItem
import com.yourfiles.manager.presentation.vm.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderOrganiserScreen(
    viewModel: FolderOrganiserViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Organise: ${state.folderName}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { com.yourfiles.manager.app.App.instance.navController().popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                actions = {
                    // Sort dropdown
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Outlined.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onPrimary,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
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
                    text = "Group by type",
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                val displayItems = state.displayItems
                if (displayItems.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Empty folder",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
            text = "${state.fileCount} file${if (state.fileCount != 1) "s" else ""}",
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
            text = "Sort: ${state.sortOption.label}",
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
            text = "(${header.count})",
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
            FileCategory.OTHER -> Icons.Outlined.InsertDriveFile
        }
    }
}

private fun getFileIconColorFor(item: OrganiserFileItem): Color {
    return if (item.isDirectory) {
        Color(0xFFFF9800)
    } else {
        when (item.category) {
            FileCategory.IMAGES -> Color(0xFFE91E63)
            FileCategory.VIDEOS -> Color(0xFF9C27B0)
            FileCategory.AUDIO -> Color(0xFFFF5722)
            FileCategory.DOCUMENTS -> Color(0xFF1976D2)
            FileCategory.APK -> Color(0xFF4CAF50)
            FileCategory.ARCHIVES -> Color(0xFF795548)
            FileCategory.OTHER -> Color(0xFF607D8B)
        }
    }
}