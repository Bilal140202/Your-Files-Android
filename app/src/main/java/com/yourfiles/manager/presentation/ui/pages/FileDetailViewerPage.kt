package com.yourfiles.manager.presentation.ui.pages

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.App
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.data.model.LocalFile
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import com.yourfiles.manager.presentation.ui.components.VideoPlayer
import com.yourfiles.manager.presentation.ui.components.common.ImageViewer
import com.yourfiles.manager.presentation.ui.components.common.PopupCompose
import com.yourfiles.manager.presentation.ui.components.common.thumbnail.OtherFileThumbnailCompose
import com.yourfiles.manager.presentation.vm.FileDetailViewerVM
import com.yourfiles.manager.utils.getMimeType
import com.yourfiles.manager.utils.isFileArchive
import com.yourfiles.manager.utils.isFileAudio
import com.yourfiles.manager.utils.isFileCode
import com.yourfiles.manager.utils.isFileImage
import com.yourfiles.manager.utils.isFilePdf
import com.yourfiles.manager.utils.isFileText
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
    val navigator = App.instance.navController()
    val showDeleteDialog = remember { vm.showDeleteDialog }
    val isDeleting = remember { vm.isDeleting }
    val context = LocalContext.current

    val currentFile = currentFiles.getOrNull(pagerState.currentPage)

    if (showDeleteDialog.value || isDeleting.value) {
        PopupCompose(show = true, onPopupDismissed = { if (!isDeleting.value) vm.cancelDelete() }) {
            AlertDialog(
                onDismissRequest = { if (!isDeleting.value) vm.cancelDelete() },
                title = { Text(if (isDeleting.value) "Moving to Trash..." else "Move to Trash") },
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
                        Text("Move \"${currentFile?.fileName}\" to the Trash? You can restore it later from the Trash.")
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
                        }) { Text("Move to Bin", color = MaterialTheme.colorScheme.error) }
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
        containerColor = Color.Black, // opaque black bg — prevent ghost overlay
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
                    // ── Universal file type dispatcher ──
                    // ES principle: images/video/text/audio in-app, APK→installer, rest→system
                    val mime = file.fileType
                    val filePath = file.id
                    when {
                        // IMAGES — in-app viewer (works)
                        isFileImage(mime) -> ImageViewer(file.id)

                        // VIDEO — in-app player (works)
                        isFileVideo(mime) -> VideoPlayer(file.id)

                        // TEXT & CODE — in-app monospace viewer
                        isFileText(mime) || isFileCode(filePath) ->
                            TextViewerScreen(filePath = filePath)

                        // AUDIO — in-app player with ExoPlayer
                        isFileAudio(mime) -> AudioPlayerScreen(filePath = filePath)

                        // ZIP archives — in-app browser showing contents
                        isFileArchive(mime) && isZipFile(filePath) ->
                            ZipBrowserScreen(filePath = filePath)

                        // PDF — in-app viewer
                        isFilePdf(mime) -> PdfViewerScreen(filePath = filePath)

                        // APK — navigate to info screen with Install button
                        mime == "application/vnd.android.package-archive" || isApkFile(filePath) -> {
                            LaunchedEffect(filePath) {
                                navigator.currentDestination?.route?.let { currentRoute ->
                                    navigator.navigate("${Routes.APK_INFO}?filePath=${Uri.encode(filePath)}") {
                                        popUpTo(currentRoute) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        // EVERYTHING ELSE — system chooser (Always / This Time Only)
                        else -> {
                            LaunchedEffect(filePath) {
                                openWithSystemChooser(context, filePath, mime)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Opening...", color = Color.White.copy(alpha = 0.7f))
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

/**
 * Open any file with the system's Intent chooser.
 * Shows Always / This Time Only dialog, exactly like ES File Explorer.
 * Uses FileProvider for N+ to share file URI safely.
 */
private fun openWithSystemChooser(context: android.content.Context, filePath: String, mimeType: String?) {
    val file = File(filePath)
    try {
        if (!file.exists()) return
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context, "${context.packageName}.provider", file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open with"))
    } catch (e: android.content.ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open ${file.extension}", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
    }
}

/** Check if file is a ZIP-based archive (zip only — rar/7z go to system). */
private fun isZipFile(filePath: String): Boolean {
    val dotIndex = filePath.lastIndexOf('.')
    if (dotIndex < 0) return false
    return filePath.substring(dotIndex + 1).lowercase() == "zip"
}

/** Check if file is an APK by extension (fallback when MIME type is null). */
private fun isApkFile(filePath: String): Boolean {
    val dotIndex = filePath.lastIndexOf('.')
    if (dotIndex < 0) return false
    return filePath.substring(dotIndex + 1).lowercase() == "apk"
}
