package com.yourfiles.manager.presentation.ui.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.App
import com.yourfiles.manager.data.model.LocalFile
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import com.yourfiles.manager.presentation.ui.components.VideoPlayer
import com.yourfiles.manager.presentation.ui.components.common.ImageViewer
import com.yourfiles.manager.presentation.ui.components.common.PopupCompose
import com.yourfiles.manager.presentation.ui.components.common.thumbnail.OtherFileThumbnailCompose
import com.yourfiles.manager.presentation.vm.FileDetailViewerVM
import com.yourfiles.manager.utils.getMimeType
import com.yourfiles.manager.utils.isFileImage
import com.yourfiles.manager.utils.isFileVideo
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailViewerCompose(
    filePath: String,
    category: String,
    md5: String? = null,
    vm: FileDetailViewerVM = viewModel()
) {
    // --- Direct file mode: when opening from file browser (category="file") ---
    // Skip the database query entirely — build the file list from the filesystem path.
    val isDirectFile = (category == "file")

    // If direct file, build a single-item list from the file path
    val directFiles = remember(filePath) {
        if (isDirectFile) {
            val file = File(filePath)
            if (file.exists()) {
                listOf(
                    LocalFile(
                        id = file.absolutePath,
                        fileName = file.name,
                        fileType = getMimeType(file.absolutePath),
                        size = file.length(),
                        modifiedTime = file.lastModified(),
                        md5CheckSum = null,
                    )
                )
            } else {
                emptyList()
            }
        } else {
            null // null signals "use database loading"
        }
    }

    // Database loading only for non-direct categories
    LaunchedEffect(category, md5) {
        if (!isDirectFile) {
            if (category == "category_duplicates" && md5 != null) {
                vm.loadFilesByMd5(md5)
            } else {
                vm.loadFilesByCategory(category)
            }
        }
    }

    // Resolve current file list: direct files take priority over DB results
    val dbFiles by vm.categoryFiles.collectAsState()
    val currentFiles: List<LocalFile> = if (directFiles != null) directFiles else (dbFiles ?: emptyList())

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { currentFiles.size }
    )

    // Scroll to the correct file in DB-backed pagers
    LaunchedEffect(currentFiles, filePath) {
        if (!isDirectFile && currentFiles.isNotEmpty()) {
            val index = currentFiles.indexOfFirst { it.id == filePath }
            if (index != -1 && index != pagerState.currentPage) {
                pagerState.animateScrollToPage(index)
            }
        }
    }

    val infoPopUpVisibility = remember { mutableStateOf(false) }
    val navigator = remember { App.instance.navController() }
    val showDeleteDialog = remember { vm.showDeleteDialog }
    val isDeleting = remember { vm.isDeleting }

    val currentFile = currentFiles.getOrNull(pagerState.currentPage)

    if (showDeleteDialog.value || isDeleting.value) {
        PopupCompose(show = true, onPopupDismissed = { if (!isDeleting.value) vm.cancelDelete() }) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting.value) vm.cancelDelete() },
                title = { Text(if (isDeleting.value) "Deleting..." else "Delete File") },
                text = {
                    if (isDeleting.value) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text("Please wait...")
                        }
                    } else {
                        Text("Are you sure you want to delete ${currentFile?.fileName}? You will not be able to recover it.")
                    }
                },
                confirmButton = {
                    if (!isDeleting.value) {
                        TextButton(onClick = {
                            currentFile?.let { file ->
                                vm.confirmDelete(file.id) {
                                    if (currentFiles.size <= 1) navigator.navigateUp()
                                }
                            }
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    }
                },
                dismissButton = {
                    if (!isDeleting.value) {
                        TextButton(onClick = { vm.cancelDelete() }) { Text("Cancel") }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentFile?.fileName ?: "", fontSize = 14.sp) },
                navigationIcon = { BackNavigationIconCompose() },
                actions = {
                    if (currentFile != null) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "info",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { infoPopUpVisibility.value = true }
                        )
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable { vm.requestDelete() }
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            // No loading spinner — content appears instantly for direct files
            if (currentFiles.isEmpty()) {
                Text(
                    text = "File not found",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                return@Scaffold
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 16.dp
            ) { pageIndex ->
                val file = currentFiles.getOrNull(pageIndex) ?: return@HorizontalPager

                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isFileImage(file.fileType) -> ImageViewer(file.id)
                        isFileVideo(file.fileType) -> VideoPlayer(file.id)
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                OtherFileThumbnailCompose(filePath = file.fileName)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = vm.getFileInfo(file),
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 18.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }

            if (infoPopUpVisibility.value && currentFile != null) {
                PopupCompose(show = true, onPopupDismissed = { infoPopUpVisibility.value = false }) {
                    Card(modifier = Modifier.width(300.dp)) {
                        Text(
                            text = vm.getFileInfo(currentFile),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
