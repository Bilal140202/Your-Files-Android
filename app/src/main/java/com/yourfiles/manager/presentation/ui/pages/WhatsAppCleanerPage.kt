package com.yourfiles.manager.presentation.ui.pages

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.yourfiles.manager.app.itemSpacing
import com.yourfiles.manager.data.model.LocalFile
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import com.yourfiles.manager.presentation.ui.components.common.flatFileManager.FlatFileManagerDeleteComposable
import com.yourfiles.manager.presentation.vm.WhatsAppCleanerVM
import com.yourfiles.manager.presentation.vm.WhatsAppCategory
import java.io.File

// ────────────────────────────────────────────────────────────────────────────────
// Page Composable
// ────────────────────────────────────────────────────────────────────────────────

/**
 * WhatsApp Media Cleaner page.
 *
 * Scans the WhatsApp media folder on the device and presents files grouped by type
 * (Images, Videos, Documents, Voice Notes). Users can select files and delete them
 * via the same select/delete workflow used across the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppCleanerPage(vm: WhatsAppCleanerVM = viewModel()) {

    val allFiles by vm.allFiles.collectAsState()
    val selectedModeOn = remember { vm.selectedModeOn }
    val selectedFiles = remember { vm.selectedFiles }

    val configuration = LocalConfiguration.current
    val columns = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 6
    val thumbnailSize = configuration.screenWidthDp.dp / columns

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Cleaner") },
                actions = {
                    if (!selectedModeOn.value) {
                        TextButton(onClick = { selectedModeOn.value = true }) {
                            Text("Select")
                        }
                    } else {
                        TextButton(onClick = {
                            selectedModeOn.value = false
                            selectedFiles.value = emptySet()
                        }) {
                            Text("Cancel")
                        }
                    }
                },
                navigationIcon = { BackNavigationIconCompose() },
            )
        },
        floatingActionButton = {
            if (selectedModeOn.value) {
                val currentFiles = vm.currentCategoryFiles
                val selectedSize = currentFiles
                    .filter { it.id in selectedFiles.value }
                    .sumOf { it.size }
                FlatFileManagerDeleteComposable(vm = vm, selectedSize = selectedSize)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // ── WhatsApp not found ────────────────────────────────────────
                !vm.whatsappExists.value && !vm.isScanning.value -> {
                    WhatsAppNotFoundState()
                }

                // ── Scanning ──────────────────────────────────────────────────
                vm.isScanning.value -> {
                    WhatsAppScanningState()
                }

                // ── Loaded (possibly empty) ───────────────────────────────────
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // ── Category chips ────────────────────────────────────
                        CategoryChipRow(
                            allFiles = allFiles,
                            activeCategory = vm.activeCategory,
                            onCategorySelected = vm::selectCategory,
                        )

                        // ── File grid ─────────────────────────────────────────
                        val currentFiles = vm.currentCategoryFiles
                        if (currentFiles.isEmpty()) {
                            WhatsAppEmptyCategoryState(category = vm.activeCategory)
                        } else {
                            WhatsAppMediaGrid(
                                files = currentFiles,
                                columns = columns,
                                thumbnailSize = thumbnailSize,
                                selectedFiles = selectedFiles.value,
                                selectedModeOn = selectedModeOn.value,
                                onSelectionChanged = { fileId, selected ->
                                    if (selected) selectedFiles.value += fileId
                                    else selectedFiles.value -= fileId
                                },
                                onLongPress = {
                                    if (!selectedModeOn.value) {
                                        selectedModeOn.value = true
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Sub-composables
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryChipRow(
    allFiles: Map<WhatsAppCategory, List<LocalFile>>,
    activeCategory: WhatsAppCategory,
    onCategorySelected: (WhatsAppCategory) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(WhatsAppCategory.entries) { category ->
            val count = allFiles[category]?.size ?: 0
            val isSelected = category == activeCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${category.label} ($count)")
                    }
                },
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun WhatsAppMediaGrid(
    files: List<LocalFile>,
    columns: Int,
    thumbnailSize: Dp,
    selectedFiles: Set<String>,
    selectedModeOn: Boolean,
    onSelectionChanged: (String, Boolean) -> Unit,
    onLongPress: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(itemSpacing),
    ) {
        items(
            items = files,
            key = { it.id }
        ) { file ->
            WhatsAppMediaGridItem(
                file = file,
                thumbnailSize = thumbnailSize,
                isSelected = selectedFiles.contains(file.id),
                selectionEnabled = selectedModeOn,
                onSelectionChanged = onSelectionChanged,
                onLongPress = onLongPress,
            )
        }
    }
}

@Composable
private fun WhatsAppMediaGridItem(
    file: LocalFile,
    thumbnailSize: Dp,
    isSelected: Boolean,
    selectionEnabled: Boolean,
    onSelectionChanged: (String, Boolean) -> Unit,
    onLongPress: () -> Unit,
) {
    val isImage = file.fileType?.startsWith("image/") == true
    val isVideo = file.fileType?.startsWith("video/") == true

    Card(
        modifier = Modifier
            .size(thumbnailSize)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        onClick = {
            if (selectionEnabled) {
                onSelectionChanged(file.id, !isSelected)
            }
        },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isImage || isVideo) {
                AsyncImage(
                    model = File(file.id),
                    contentDescription = file.fileName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                // Non-media files: show a placeholder icon + filename
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = file.fileName ?: "File",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Selection indicator
            if (selectionEnabled) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChanged(file.id, !isSelected) },
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopEnd),
                )
            }
        }
    }
}

@Composable
private fun WhatsAppNotFoundState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Description,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "WhatsApp not found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Could not find WhatsApp media on this device.\n" +
                "Make sure WhatsApp is installed and media files have been received.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WhatsAppScanningState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = "Scanning WhatsApp media\u2026",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WhatsAppEmptyCategoryState(category: WhatsAppCategory) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.padding(vertical = 12.dp))
        Text(
            text = "No ${category.label.lowercase()} found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "WhatsApp ${category.label.lowercase()} will appear here once received",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
