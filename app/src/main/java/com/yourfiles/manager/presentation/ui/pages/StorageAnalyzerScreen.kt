package com.yourfiles.manager.presentation.ui.pages

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.text.format.Formatter
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.yourfiles.manager.app.App
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.presentation.vm.AnalyzerPhase
import com.yourfiles.manager.presentation.vm.AnalyzerUiState
import com.yourfiles.manager.presentation.vm.ScanOption
import com.yourfiles.manager.presentation.vm.StorageAnalyzerVM
import com.yourfiles.manager.presentation.vm.StorageCategory
import kotlin.math.roundToInt

// ===== Colors & Icons =====

private val CAT_COLORS = mapOf(
    StorageCategory.IMAGES to Color(0xFFE91E63),
    StorageCategory.VIDEOS to Color(0xFF9C27B0),
    StorageCategory.AUDIO to Color(0xFFFF9800),
    StorageCategory.DOCUMENTS to Color(0xFF2196F3),
    StorageCategory.APK to Color(0xFF4CAF50),
    StorageCategory.ARCHIVES to Color(0xFF795548),
    StorageCategory.OTHER to Color(0xFF607D8B),
)

private val CAT_ICONS = mapOf(
    StorageCategory.IMAGES to Icons.Outlined.Image,
    StorageCategory.VIDEOS to Icons.Outlined.Movie,
    StorageCategory.AUDIO to Icons.Outlined.MusicNote,
    StorageCategory.DOCUMENTS to Icons.Outlined.Description,
    StorageCategory.APK to Icons.Outlined.Memory,
    StorageCategory.ARCHIVES to Icons.Outlined.Archive,
    StorageCategory.OTHER to Icons.Outlined.QuestionMark,
)

// ===== Main Screen =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzerScreen(
    viewModel: StorageAnalyzerVM = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val navController = remember { App.instance.navController() }

    var showPermDialog by remember { mutableStateOf(false) }
    val scanOptions = remember { mutableStateListOf(*ScanOption.entries.toTypedArray()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Storage Analyzer", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.phase == AnalyzerPhase.RESULTS) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Outlined.Refresh, "Refresh")
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
        when (state.phase) {
            AnalyzerPhase.HOME -> {
                if (state.error != null) {
                    // Error state
                    Box(
                        Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                } else {
                    // Home: checkboxes + ANALYZE button
                    HomePhaseContent(
                        scanOptions = scanOptions,
                        onAnalyze = {
                            if (!Environment.isExternalStorageManager()) {
                                showPermDialog = true
                            } else {
                                viewModel.startAnalysis()
                            }
                        },
                        modifier = Modifier.padding(padding),
                    )
                }
            }

            AnalyzerPhase.SCANNING -> {
                ScanningContent(state, Modifier.padding(padding))
            }

            AnalyzerPhase.RESULTS -> {
                ResultsDashboard(
                    state = state,
                    onCategoryClick = { category ->
                        navController.navigate("${Routes.ANALYZER_CATEGORY}?category=${category.name}")
                    },
                    onAllFilesClick = {
                        navController.navigate(Routes.ANALYZER_FOLDERS)
                    },
                    onToolClick = { route ->
                        navController.navigate(route)
                    },
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }

    // Permission dialog
    if (showPermDialog) {
        AlertDialog(
            onDismissRequest = { showPermDialog = false },
            title = { Text("Permission Required") },
            text = {
                Text(
                    "Due to Android 13+ system restrictions, " +
                        "full storage access is needed to analyze all files. " +
                        "Please grant All Files Access permission."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermDialog = false
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    )
                }) { Text("Grant Permission") }
            },
            dismissButton = {
                TextButton(onClick = { showPermDialog = false }) { Text("Cancel") }
            },
        )
    }
}

// ===== HOME PHASE =====

@Composable
private fun HomePhaseContent(
    scanOptions: List<ScanOption>,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Scanning icon + description
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Analyze Storage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Select what to analyze and tap the button below",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Checkboxes
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                scanOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = null, // always checked (cosmetic)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        // ANALYZE button
        item {
            Button(
                onClick = onAnalyze,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("ANALYZE", modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        // Storage summary at bottom
        item {
            val ctx = LocalContext.current
            val statFs = remember {
                runCatching { StatFs(Environment.getExternalStorageDirectory().absolutePath) }.getOrNull()
            }
            if (statFs != null) {
                val total = statFs.totalBytes
                val used = total - statFs.availableBytes
                val free = statFs.availableBytes
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        StorageStatItem(ctx, "Total", total)
                        StorageStatItem(ctx, "Used", used)
                        StorageStatItem(ctx, "Free", free)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun StorageStatItem(ctx: android.content.Context, label: String, bytes: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            Formatter.formatShortFileSize(ctx, bytes),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
        )
    }
}

// ===== SCANNING PHASE =====

@Composable
private fun ScanningContent(state: AnalyzerUiState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary,
                progress = { state.scanProgress / 100f },
            )
            Spacer(Modifier.height(16.dp))
            Text("Scanning storage...", style = MaterialTheme.typography.bodyMedium)
            if (state.scanProgress > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.scanProgress}% \u00B7 ${String.format("%,d", state.scannedCount)} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ===== RESULTS DASHBOARD =====

@Composable
private fun ResultsDashboard(
    state: AnalyzerUiState,
    onCategoryClick: (StorageCategory) -> Unit,
    onAllFilesClick: () -> Unit,
    onToolClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val categorizedTotal = state.categories.sumOf { it.totalSize }
    val freeBytes = state.totalCapacity - state.usedBytes

    LazyColumn(modifier = modifier.fillMaxSize()) {
        // Storage summary bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StorageStatItem(ctx, "Total", state.totalCapacity)
                    StorageStatItem(ctx, "Used", state.usedBytes)
                    StorageStatItem(ctx, "Free", freeBytes)
                }
            }
        }

        // Category cards — 2-column grid
        item {
            Text(
                "Categories",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        item {
            val nonEmptyCats = state.categories.filter { it.totalSize > 0 || it.fileCount > 0 }
            // Use a simple Row of 2 columns
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                nonEmptyCats.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { catStat ->
                            CategoryCard(
                                stats = catStat,
                                onClick = { onCategoryClick(catStat.category) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        // Pad if odd number
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // All Files section
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(onClick = onAllFilesClick),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Outlined.Folder, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "All Files",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "${state.folderSizes.size} top folders",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        Formatter.formatShortFileSize(ctx, state.usedBytes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Outlined.KeyboardArrowRight, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Tool sections
        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                "Tools",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        item {
            ToolRow(
                icon = Icons.Outlined.HourglassEmpty,
                label = "Recently Created",
                color = Color(0xFF009688),
                onClick = { onToolClick(Routes.ANALYZER_RECENT) },
            )
        }
        item {
            ToolRow(
                icon = Icons.Outlined.DeleteSweep,
                label = "Junk Files",
                color = Color(0xFFF44336),
                onClick = { onToolClick(Routes.ANALYZER_JUNK) },
            )
        }
        item {
            ToolRow(
                icon = Icons.Outlined.CreateNewFolder,
                label = "Duplicate Files",
                color = Color(0xFFFF9800),
                onClick = { onToolClick(Routes.ANALYZER_DUPLICATES) },
            )
        }
        item {
            ToolRow(
                icon = Icons.Outlined.AutoFixHigh,
                label = "Large Files",
                color = Color(0xFF9C27B0),
                onClick = { onToolClick(Routes.ANALYZER_LARGE) },
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ===== CATEGORY CARD =====

@Composable
private fun CategoryCard(
    stats: com.yourfiles.manager.presentation.vm.CategoryStats,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ctx = LocalContext.current
    val color = CAT_COLORS[stats.category] ?: Color.Gray
    val icon = CAT_ICONS[stats.category] ?: Icons.Outlined.QuestionMark
    val total = stats.totalSize

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            // Icon + size
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    Formatter.formatShortFileSize(ctx, total),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                stats.category.label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
            )
            Text(
                "${String.format("%,d", stats.fileCount)} files",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF999999),
            )
        }
    }
}

// ===== TOOL ROW =====

@Composable
private fun ToolRow(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 66.dp))
    }
}