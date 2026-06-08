package com.yourfiles.manager.presentation.ui.pages

import android.content.Intent
import android.text.format.Formatter
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import java.io.File
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.ContentPasteGo
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Surface
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current
    val displayItems = state.displayItems

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Search state
    var isSearchActive by remember { mutableStateOf(false) }
    var searchTextInput by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Rename dialog
    var showRenameDialog by remember { mutableStateOf(false) }
    var renamePath by remember { mutableStateOf("") }
    var renameInput by remember { mutableStateOf("") }

    // More bottom sheet
    var showMoreSheet by remember { mutableStateOf(false) }
    val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Info bottom sheet
    var showInfoSheet by remember { mutableStateOf(false) }
    var infoText by remember { mutableStateOf("") }

    val scrollState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.initWithNavPath(initialPath)
    }

    LaunchedEffect(state.currentPath) {
        if (scrollState.firstVisibleItemIndex > 0) {
            scrollState.animateScrollToItem(0)
        }
    }

    // Debounce search
    LaunchedEffect(Unit) {
        snapshotFlow { searchTextInput }
            .debounce(150)
            .distinctUntilChanged()
            .collect { viewModel.setSearchQuery(it) }
    }

    // Auto-focus keyboard when search activates
    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Back priority: search > selection > folder nav
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchTextInput = ""
        viewModel.setSearchQuery("")
        keyboardController?.hide()
    }
    BackHandler(enabled = state.isMultiSelectMode) {
        viewModel.exitMultiSelect()
    }
    BackHandler(enabled = !viewModel.isAtRoot() && !isSearchActive && !state.isMultiSelectMode) {
        viewModel.navigateUp()
    }

    // ===== DIALOGS =====

    // Create folder dialog
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

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete") },
            text = { Text("Delete ${state.selectedItems.size} selected items?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected { showDeleteDialog = false }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Rename dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("New name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameItem(renamePath, renameInput.trim()) {
                                showRenameDialog = false
                            }
                        }
                    },
                    enabled = renameInput.isNotBlank() && renameInput != java.io.File(renamePath).name,
                ) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
        )
    }

    // More bottom sheet
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "More options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                // Share
                BottomSheetAction(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    onClick = {
                        showMoreSheet = false
                        val firstFile = state.selectedItems.firstOrNull() ?: return@BottomSheetAction
                        val file = java.io.File(firstFile)
                        if (file.exists()) {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context, "${context.packageName}.provider", file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "*/*"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share"))
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Properties / Info
                BottomSheetAction(
                    icon = Icons.Outlined.Info,
                    label = "Properties",
                    onClick = {
                        showMoreSheet = false
                        val firstFile = state.selectedItems.firstOrNull() ?: return@BottomSheetAction
                        infoText = viewModel.getItemDetails(firstFile)
                        showInfoSheet = true
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Details (same as properties for now)
                BottomSheetAction(
                    icon = Icons.Outlined.Info,
                    label = "Details",
                    onClick = {
                        showMoreSheet = false
                        val firstFile = state.selectedItems.firstOrNull() ?: return@BottomSheetAction
                        infoText = viewModel.getItemDetails(firstFile)
                        showInfoSheet = true
                    },
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Info bottom sheet
    if (showInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = "Properties",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ===== SCAFFOLD =====

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            if (state.isMultiSelectMode) {
                // ===== SELECTION MODE TOP BAR (ES style) =====
                TopAppBar(
                    title = {
                        Text(
                            text = "${state.selectedItems.size}/${displayItems.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                    navigationIcon = {
                        // Hidden in selection mode (back handled by BackHandler)
                        Box(modifier = Modifier.size(48.dp))
                    },
                    actions = {
                        // Select All
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(
                                Icons.Outlined.SelectAll,
                                contentDescription = "Select All",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        // Select Interval
                        IconButton(
                            onClick = { viewModel.enterIntervalMode() },
                            enabled = state.selectedItems.size >= 1,
                        ) {
                            Icon(
                                Icons.Outlined.SwapHoriz,
                                contentDescription = "Select Interval",
                                tint = if (state.isIntervalMode)
                                    Color.Yellow
                                else
                                    MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        // Cancel
                        IconButton(onClick = { viewModel.exitMultiSelect() }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            } else if (isSearchActive) {
                // ===== SEARCH MODE =====
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
                // ===== NORMAL MODE: breadcrumb =====
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
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
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
                if (state.hasClipboard) {
                    // Paste FAB (appears when clipboard has items)
                    FloatingActionButton(
                        onClick = { viewModel.pasteClipboard({}) },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                    ) {
                        Icon(Icons.Filled.ContentPaste, "Paste", tint = MaterialTheme.colorScheme.onTertiary)
                    }
                } else {
                    FloatingActionButton(
                        onClick = { showCreateFolderDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            Icons.Outlined.CreateNewFolder,
                            "New Folder",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (state.isMultiSelectMode) {
                // ===== BOTTOM ACTION BAR (ES 2014 style, 56dp) =====
                BottomActionBar(
                    onCopy = { viewModel.copySelected() },
                    onCut = { viewModel.cutSelected() },
                    onDelete = { showDeleteDialog = true },
                    onRename = {
                        val firstPath = state.selectedItems.firstOrNull() ?: return@BottomActionBar
                        renamePath = firstPath
                        renameInput = java.io.File(firstPath).name
                        showRenameDialog = true
                    },
                    onMore = { showMoreSheet = true },
                    canRename = state.selectedItems.size == 1,
                )
            }
        },
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
                        isIntervalMode = state.isIntervalMode,
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

// ===== BOTTOM ACTION BAR (ES 2014) =====

@Composable
private fun BottomActionBar(
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    onMore: () -> Unit,
    canRename: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomActionBtn(
                icon = Icons.Filled.ContentCopy,
                label = "Copy",
                onClick = onCopy,
                modifier = Modifier.weight(1f, fill = true),
            )
            BottomActionBtn(
                icon = Icons.Filled.ContentCut,
                label = "Cut",
                onClick = onCut,
                modifier = Modifier.weight(1f, fill = true),
            )
            BottomActionBtn(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f, fill = true),
            )
            BottomActionBtn(
                icon = Icons.Outlined.DriveFileRenameOutline,
                label = "Rename",
                onClick = onRename,
                enabled = canRename,
                modifier = Modifier.weight(1f, fill = true),
            )
            BottomActionBtn(
                icon = Icons.Filled.MoreVert,
                label = "More",
                onClick = onMore,
                modifier = Modifier.weight(1f, fill = true),
            )
        }
    }
}

@Composable
private fun BottomActionBtn(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val alpha = if (enabled) 1f else 0.38f
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .combinedClickable(
                onClick = { if (enabled) onClick() },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint.copy(alpha = alpha),
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// ===== MORE BOTTOM SHEET ACTION =====

@Composable
private fun BottomSheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .combinedClickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ===== FILE LIST ITEM =====

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    item: FileItem,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    isIntervalMode: Boolean,
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
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isIntervalMode -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMultiSelectMode) {
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            } else {
                Icon(
                    Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Not selected",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
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

// ===== ICON HELPERS =====

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
