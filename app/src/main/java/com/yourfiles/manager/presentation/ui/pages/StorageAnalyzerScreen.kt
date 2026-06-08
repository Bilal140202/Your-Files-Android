package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.App
import com.yourfiles.manager.presentation.vm.AnalyzerUiState
import com.yourfiles.manager.presentation.vm.StorageAnalyzerVM
import com.yourfiles.manager.presentation.vm.StorageCategory

/** Colors for each storage category, ES 2014 style. */
private val CATEGORY_COLORS = mapOf(
    StorageCategory.IMAGES to Color(0xFFE91E63),
    StorageCategory.VIDEOS to Color(0xFF9C27B0),
    StorageCategory.AUDIO to Color(0xFFFF9800),
    StorageCategory.DOCUMENTS to Color(0xFF2196F3),
    StorageCategory.APK to Color(0xFF4CAF50),
    StorageCategory.OTHER to Color(0xFF607D8B),
)

private val CATEGORY_ICONS = mapOf(
    StorageCategory.IMAGES to Icons.Outlined.Image,
    StorageCategory.VIDEOS to Icons.Outlined.Movie,
    StorageCategory.AUDIO to Icons.Outlined.MusicNote,
    StorageCategory.DOCUMENTS to Icons.Outlined.Description,
    StorageCategory.APK to Icons.Outlined.Memory,
    StorageCategory.OTHER to Icons.Outlined.QuestionMark,
)

/** Category → Android folder path for "open filtered" navigation. */
private val CATEGORY_FOLDERS = mapOf(
    StorageCategory.IMAGES to "Pictures",
    StorageCategory.VIDEOS to "Movies",
    StorageCategory.AUDIO to "Music",
    StorageCategory.DOCUMENTS to "Documents",
    StorageCategory.APK to "Download",
    StorageCategory.OTHER to null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzerScreen(
    onNavigateToExplorer: (String) -> Unit = {},
    viewModel: StorageAnalyzerVM = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Storage Analyzer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = {
                        App.instance.navController().popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    ) { paddingValues ->
        when {
            state.isScanning -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning storage\u2026",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            state.error != null -> {
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
                        TextButton(onClick = { viewModel.analyzeStorage() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // ── Donut Chart Section ────────────────────────────────────────
                    item {
                        DonutChartSection(state)
                    }

                    // ── Category Breakdown ──────────────────────────────────────────
                    item {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    items(state.categories, key = { it.category.name }) { catStat ->
                        CategoryRow(
                            stats = catStat,
                            totalUsed = state.totalUsedBytes,
                            onClick = {
                                val folder = CATEGORY_FOLDERS[catStat.category]
                                if (folder != null) {
                                    onNavigateToExplorer("$primaryPath/$folder")
                                }
                            },
                        )
                    }

                    // ── Top 10 Largest Files ───────────────────────────────────────
                    if (state.topLargestFiles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Top ${state.topLargestFiles.size} Largest Files",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        items(state.topLargestFiles, key = { it.path }) { entry ->
                            LargestFileRow(entry)
                        }
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ===== DONUT CHART SECTION =====

@Composable
private fun DonutChartSection(state: AnalyzerUiState) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The donut
        val totalCategorized = state.categories.sumOf { it.totalSize }
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Canvas draws the arcs
            Canvas(modifier = Modifier.size(180.dp)) {
                val strokeWidth = 32.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val topLeft = Offset(
                    (size.width - radius * 2) / 2,
                    (size.height - radius * 2) / 2,
                )

                var startAngle = -90f // Start from 12 o'clock
                for (catStat in state.categories) {
                    if (catStat.totalSize <= 0) continue
                    val sweep = if (totalCategorized > 0) {
                        (catStat.totalSize.toFloat() / totalCategorized) * 360f
                    } else 0f

                    drawArc(
                        color = CATEGORY_COLORS[catStat.category] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth),
                    )
                    startAngle += sweep
                }
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Formatter.formatShortFileSize(context, state.totalUsedBytes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            state.categories.filter { it.totalSize > 0 }.forEach { catStat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = CircleShape,
                        color = CATEGORY_COLORS[catStat.category] ?: Color.Gray,
                    ) {}
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = catStat.category.label,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ===== CATEGORY ROW =====

@Composable
private fun CategoryRow(
    stats: com.yourfiles.manager.presentation.vm.CategoryStats,
    totalUsed: Long,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val color = CATEGORY_COLORS[stats.category] ?: Color.Gray
    val icon = CATEGORY_ICONS[stats.category] ?: Icons.AutoMirrored.Outlined.InsertDriveFile
    val percentage = if (totalUsed > 0) {
        (stats.totalSize * 100 / totalUsed).toInt()
    } else 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Colored icon circle
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stats.category.label,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Category name + file count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.category.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${stats.fileCount} files",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Size + percentage
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatter.formatShortFileSize(context, stats.totalSize),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
    }
}

// ===== LARGEST FILE ROW =====

@Composable
private fun LargestFileRow(entry: com.yourfiles.manager.presentation.vm.LargestFileEntry) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.path,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = Formatter.formatShortFileSize(context, entry.size),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
    }
}
