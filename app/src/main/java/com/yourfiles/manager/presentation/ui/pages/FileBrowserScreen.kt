package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.App
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.domain.model.FileItem
import com.yourfiles.manager.presentation.vm.FileExplorerViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun FileBrowserScreen(
    initialPath: String? = null,
    onOpenDrawer: () -> Unit,
    viewModel: FileExplorerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchTextInput by remember { mutableStateOf("") } // raw TextField input
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Derived display list (filtered if searching)
    val displayItems = state.displayItems

    val scrollState = rememberLazyListState()

    // Only apply nav argument on very first launch — never override SavedStateHandle
    LaunchedEffect(Unit) {
        viewModel.initWithNavPath(initialPath)
    }

    // When currentPath changes (folder navigation), reset scroll to top
    LaunchedEffect(state.currentPath) {
        if (scrollState.firstVisibleItemIndex > 0) {
            scrollState.animateScrollToItem(0)
        }
    }

    // Debounce: push raw search text to ViewModel after 150ms of inactivity
    LaunchedEffect(Unit) {
        snapshotFlow { searchTextInput }
            .debounce(150)
            .distinctUntilChanged()
            .collect { query ->
                viewModel.setSearchQuery(query)
            }
    }

    // Auto-focus keyboard when search activates
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100) // small delay for TextField to be laid out
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Back handler: close search first, then navigate up folders
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchTextInput = ""
        viewModel.setSearchQuery("")
        keyboardController?.hide()
    }
    BackHandler(enabled = !viewModel.isAtRoot() && !isSearchActive) {
        viewModel.navigateUp()
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName.trim())
                            showCreateFolderDialog = false
                            newFolderName = ""
                        }
                    },
                    enabled = newFolderName.isNotBlank(),
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete") },
            text = { Text("Delete ${state.selectedItems.size} selected items?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected { showDeleteDialog = false }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (isSearchActive) {
                // SEARCH MODE: inline search bar (ES 2014 style)
                TopAppBar(
                    title = {
                        TextField(
                            value = searchTextInput,
                            onValueChange = { searchTextInput = it },
                            placeholder = {
                                Text(
                                    "Search in ${viewModel.getCurrentFolderName()}",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { keyboardController?.hide() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                            ),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchTextInput = ""
                            viewModel.setSearchQuery("")
                            keyboardController?.hide()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close search",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    actions = {
                        if (searchTextInput.isNotEmpty()) {
                            IconButton(onClick = {
                                searchTextInput = ""
                                viewModel.setSearchQuery("")
                            }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            } else {
                // NORMAL MODE: breadcrumb navigation
                TopAppBar(
                    title = {
                        val segments = viewModel.getBreadcrumbSegments()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            segments.forEachIndexed { index, (name, path) ->
                                if (index > 0) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        if (path != state.currentPath) {
                                            viewModel.navigateTo(path)
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                                ) {
                                    Text(
                                        text = name,
                                        color = if (index == segments.lastIndex)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                        style = if (index == segments.lastIndex)
                                            MaterialTheme.typography.titleSmall
                                        else
                                            MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Drawer",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    actions = {
                        if (state.isMultiSelectMode) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Outlined.Delete, "Delete", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            TextButton(onClick = { viewModel.exitMultiSelect() }) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        } else {
                            IconButton(onClick = {
                                isSearchActive = true
                            }) {
                                Icon(Icons.Outlined.Search, "Search", tint = MaterialTheme.colorScheme.onPrimary)
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
            }
        },
        floatingActionButton = {
            if (!state.isMultiSelectMode && !isSearchActive) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Outlined.CreateNewFolder, "New Folder", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.navigateTo(state.currentPath) }) {
                        Text("Retry")
                    }
                }
            }
        } else {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }

                if (!state.isLoading && displayItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (state.searchQuery.isNotEmpty()) "No matches" else "Empty folder",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                items(displayItems, key = { it.path }) { item ->
                    FileListItem(
                        item = item,
                        isSelected = state.selectedItems.contains(item.path),
                        isMultiSelectMode = state.isMultiSelectMode,
                        dateFormatter = { viewModel.formatDate(it) },
                        onClick = {
                            if (state.isMultiSelectMode) {
                                viewModel.toggleSelection(item.path)
                            } else if (item.isDirectory) {
                                viewModel.navigateTo(item.path)
                            } else {
                                App.instance.navController().navigate(
                                    "${Routes.FILE_DETAIL_VIEWER}?url=${android.net.Uri.encode(item.path)}&category=file"
                                )
                            }
                        },
                        onLongClick = {
                            viewModel.toggleSelection(item.path)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    dateFormatter: (Long) -> String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val context = LocalContext.current
    val icon = getFileIcon(item)
    val iconColor = getFileIconColor(item)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onLongClick() },
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

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
                text = if (item.isDirectory) {
                    "${item.childCount} items"
                } else {
                    Formatter.formatShortFileSize(context, item.size)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            val dateStr = dateFormatter(item.lastModified)
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

private fun getFileIcon(item: FileItem): ImageVector {
    return if (item.isDirectory) {
        Icons.Outlined.Folder
    } else {
        val name = item.name.lowercase()
        when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
            name.endsWith(".gif") || name.endsWith(".webp") || name.endsWith(".bmp") ->
                Icons.Outlined.Image
            name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi") ||
            name.endsWith(".mov") || name.endsWith(".3gp") || name.endsWith(".flv") ->
                Icons.Outlined.Movie
            name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") ||
            name.endsWith(".aac") || name.endsWith(".ogg") ->
                Icons.Outlined.MusicNote
            else -> Icons.Outlined.InsertDriveFile
        }
    }
}

private fun getFileIconColor(item: FileItem): Color {
    return if (item.isDirectory) {
        Color(0xFFFF9800)
    } else {
        val name = item.name.lowercase()
        when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
            name.endsWith(".gif") || name.endsWith(".webp") ->
                Color(0xFFE91E63)
            name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".avi") ||
            name.endsWith(".mov") ->
                Color(0xFF9C27B0)
            name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") ->
                Color(0xFFFF5722)
            else -> Color(0xFF607D8B)
        }
    }
}
