package com.yourfiles.manager.presentation.ui.pages

import android.text.format.Formatter
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
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.R
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import com.yourfiles.manager.presentation.ui.components.common.EmptyStateView
import com.yourfiles.manager.utils.TrashManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Trash page showing files that have been previously deleted (moved to trash).
 *
 * Users can:
 * - View all trashed files with their original names, paths, and sizes.
 * - Restore individual files back to their original locations.
 * - Permanently empty the entire trash.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashPage() {
    val context = LocalContext.current

    // ── State ──────────────────────────────────────────────────────────────────
    var trashFiles by remember { mutableStateOf(emptyList<File>()) }
    var isLoading by remember { mutableStateOf(true) }
    var isEmptyingTrash by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf<File?>(null) }
    var isRestoring by remember { mutableStateOf(false) }
    var fileToRestore by remember { mutableStateOf<File?>(null) }

    // Load trash files on first composition and after any mutation
    fun reloadTrash() {
        isLoading = true
        trashFiles = TrashManager.getTrashFiles(context)
            .sortedByDescending { it.lastModified() }
        isLoading = false
    }

    LaunchedEffect(Unit) { reloadTrash() }

    // ── Empty trash handler ───────────────────────────────────────────────────
    val emptyTrash: () -> Unit = {
        isEmptyingTrash = true
        showEmptyTrashDialog = false
    }

    LaunchedEffect(isEmptyingTrash) {
        if (isEmptyingTrash) {
            withContext(Dispatchers.IO) {
                TrashManager.emptyTrash(context)
            }
            withContext(Dispatchers.Main) {
                isEmptyingTrash = false
                reloadTrash()
            }
        }
    }

    // ── Restore handler ────────────────────────────────────────────────────────
    val restoreFile: (File) -> Unit = { file ->
        showRestoreDialog = file
    }

    val confirmRestore: () -> Unit = {
        val file = showRestoreDialog
        if (file != null) {
            isRestoring = true
            showRestoreDialog = null
            fileToRestore = file
        }
    }

    LaunchedEffect(fileToRestore) {
        val file = fileToRestore ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            TrashManager.restoreFile(file)
        }
        withContext(Dispatchers.Main) {
            isRestoring = false
            fileToRestore = null
            reloadTrash()
        }
    }

    // ── Total trash size ──────────────────────────────────────────────────────
    val totalSize = remember(trashFiles) {
        trashFiles.sumOf { it.length() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.trash_title)
                    )
                },
                navigationIcon = { BackNavigationIconCompose() },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                // ── Loading ────────────────────────────────────────────────────
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                // ── Empty trash ──────────────────────────────────────────────
                trashFiles.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Outlined.DeleteOutline,
                        title = stringResource(R.string.trash_empty),
                        subtitle = stringResource(R.string.trash_empty_description),
                    )
                }

                // ── Trash has files ────────────────────────────────────────────
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Summary bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${context.resources.getQuantityString(R.plurals.trash_item_count, trashFiles.size, trashFiles.size)} \u00B7 ${
                                    Formatter.formatFileSize(context, totalSize)
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FilledTonalButton(
                                onClick = { showEmptyTrashDialog = true },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteForever,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.trash_empty_trash))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // File list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = trashFiles,
                                key = { it.absolutePath }
                            ) { file ->
                                TrashFileItem(
                                    file = file,
                                    onRestore = { restoreFile(file) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Empty Trash confirmation dialog ──────────────────────────────────────
    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text(stringResource(R.string.trash_empty_trash)) },
            text = {
                Text(
                    stringResource(R.string.trash_empty_trash_confirmation, trashFiles.size)
                )
            },
            confirmButton = {
                TextButton(onClick = emptyTrash) {
                    Text(stringResource(R.string.trash_empty_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // ── Restore confirmation dialog ───────────────────────────────────────────
    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text(stringResource(R.string.trash_restore_file)) },
            text = {
                Text(
                    stringResource(R.string.trash_restore_confirmation, showRestoreDialog?.name ?: "file")
                )
            },
            confirmButton = {
                TextButton(onClick = confirmRestore) {
                    Text(stringResource(R.string.trash_restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    // ── Restoring progress dialog ─────────────────────────────────────────────
    if (isRestoring) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.trash_restoring)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.trash_moving_back))
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = { },
        )
    }

    // ── Emptying progress dialog ──────────────────────────────────────────────
    if (isEmptyingTrash) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.trash_emptying)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.trash_permanently_deleting))
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = { },
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Private helper composables
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrashFileItem(
    file: File,
    onRestore: () -> Unit,
) {
    val context = LocalContext.current

    // Extract a display name: strip the timestamp prefix that was added by TrashManager.
    // TrashManager uses: "<timestamp_ms>_<originalFileName>"
    val displayName = file.name.substringAfter("_", file.name)
    val fileSize = Formatter.formatFileSize(context, file.length())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // File icon
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .padding(6.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$fileSize",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Restore button
            OutlinedButton(onClick = onRestore) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.trash_restore))
            }
        }
    }
}
