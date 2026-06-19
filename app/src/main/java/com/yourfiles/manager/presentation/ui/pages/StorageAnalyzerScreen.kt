package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.text.format.Formatter
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.App
import com.yourfiles.manager.presentation.vm.AnalyzerUiState
import com.yourfiles.manager.presentation.vm.CategoryStats
import com.yourfiles.manager.presentation.vm.CleanupSuggestion
import com.yourfiles.manager.presentation.vm.LargestFileEntry
import com.yourfiles.manager.presentation.vm.RecentFileEntry
import com.yourfiles.manager.presentation.vm.StorageAnalyzerVM
import com.yourfiles.manager.presentation.vm.StorageCategory
import java.io.File
import kotlin.math.roundToInt

/** Colors for each storage category, ES 2014 style. */
private val CATEGORY_COLORS = mapOf(
    StorageCategory.IMAGES to Color(0xFFE91E63),
    StorageCategory.VIDEOS to Color(0xFF9C27B0),
    StorageCategory.AUDIO to Color(0xFFFF9800),
    StorageCategory.DOCUMENTS to Color(0xFF2196F3),
    StorageCategory.APK to Color(0xFF4CAF50),
    StorageCategory.ARCHIVES to Color(0xFF795548),
    StorageCategory.OTHER to Color(0xFF607D8B),
)

private val CATEGORY_ICONS = mapOf(
    StorageCategory.IMAGES to Icons.Outlined.Image,
    StorageCategory.VIDEOS to Icons.Outlined.Movie,
    StorageCategory.AUDIO to Icons.Outlined.MusicNote,
    StorageCategory.DOCUMENTS to Icons.Outlined.Description,
    StorageCategory.APK to Icons.Outlined.Memory,
    StorageCategory.ARCHIVES to Icons.Outlined.Archive,
    StorageCategory.OTHER to Icons.Outlined.QuestionMark,
)

/** Category → Android folder path for "open filtered" navigation. */
private val CATEGORY_FOLDERS = mapOf(
    StorageCategory.IMAGES to "Pictures",
    StorageCategory.VIDEOS to "Movies",
    StorageCategory.AUDIO to "Music",
    StorageCategory.DOCUMENTS to "Documents",
    StorageCategory.APK to "Download",
    StorageCategory.ARCHIVES to "Download",
    StorageCategory.OTHER to null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAnalyzerScreen(
    onNavigateToExplorer: (String) -> Unit = {},
    viewModel: StorageAnalyzerVM = viewModel(),
) {
    val state by viewModel.state.collectAsState()
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
                            progress = { state.scanProgress / 100f },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Scanning storage\u2026",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (state.scanProgress > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val progressText = "${state.scanProgress}% \u00B7 ${String.format("%,d", state.scannedFileCount)} files"
                            Text(
                                text = progressText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 1: Overview Cards
                    // ═══════════════════════════════════════════════════════════════
                    item {
                        OverviewCardsSection(state)
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 2: Category Breakdown (Donut + Category List)
                    // ═══════════════════════════════════════════════════════════════
                    item {
                        DonutChartSection(state)
                    }

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
                            totalUsed = state.categories.sumOf { it.totalSize },
                            onClick = {
                                val folder = CATEGORY_FOLDERS[catStat.category]
                                if (folder != null) {
                                    onNavigateToExplorer("$primaryPath/$folder")
                                }
                            },
                        )
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 3: Top 20 Largest Files
                    // ═══════════════════════════════════════════════════════════════
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
                            LargestFileRow(
                                entry = entry,
                                onClick = {
                                    // Navigate to parent folder
                                    val parent = File(entry.path).parent
                                    if (parent != null) {
                                        onNavigateToExplorer(parent)
                                    }
                                },
                            )
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 4: Recently Added Files
                    // ═══════════════════════════════════════════════════════════════
                    if (state.recentFiles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Recently Added",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        items(state.recentFiles, key = { it.path }) { entry ->
                            RecentFileRow(
                                entry = entry,
                                onClick = {
                                    val parent = File(entry.path).parent
                                    if (parent != null) {
                                        onNavigateToExplorer(parent)
                                    }
                                },
                            )
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 5: Cleanup Suggestions
                    // ═══════════════════════════════════════════════════════════════
                    if (state.cleanupSuggestions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cleanup Suggestions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        items(state.cleanupSuggestions, key = { it.title }) { suggestion ->
                            CleanupSuggestionCard(
                                suggestion = suggestion,
                                onClick = {
                                    if (suggestion.targetPath.isNotEmpty()) {
                                        onNavigateToExplorer(suggestion.targetPath)
                                    }
                                },
                            )
                        }
                    }

                    // ═══════════════════════════════════════════════════════════════
                    // STAGE 6: Rescan Button
                    // ═══════════════════════════════════════════════════════════════
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                onClick = { viewModel.analyzeStorage() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Rescan Storage")
                            }
                        }
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 1: Overview Cards
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun OverviewCardsSection(state: AnalyzerUiState) {
    val context = LocalContext.current
    val usageFraction = if (state.totalCapacityBytes > 0) {
        state.totalUsedBytes.toFloat() / state.totalCapacityBytes.toFloat()
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Three stat cards in a Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Total Storage
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = Formatter.formatShortFileSize(context, state.totalCapacityBytes),
                color = MaterialTheme.colorScheme.primary,
            )

            // Used (with progress bar)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StatCard(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Used",
                    value = Formatter.formatShortFileSize(context, state.totalUsedBytes),
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Thin usage progress bar
                LinearProgressIndicator(
                    progress = { usageFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = when {
                        usageFraction > 0.9f -> MaterialTheme.colorScheme.error
                        usageFraction > 0.75f -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(usageFraction * 100).roundToInt()}% used",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                )
            }

            // Available
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Available",
                value = Formatter.formatShortFileSize(context, state.availableBytes),
                color = Color(0xFF4CAF50),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Scan summary
        Text(
            text = "Scanned ${String.format("%,d", state.scannedFileCount)} files",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f),
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 2: Donut Chart Section
// ═══════════════════════════════════════════════════════════════════════════

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

            // Center text — show categorized total (actual scanned data)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Formatter.formatShortFileSize(context, totalCategorized),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Scanned",
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

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 2: Category Row
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CategoryRow(
    stats: CategoryStats,
    totalUsed: Long,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val color = CATEGORY_COLORS[stats.category] ?: Color.Gray
    val icon = CATEGORY_ICONS[stats.category] ?: Icons.AutoMirrored.Outlined.InsertDriveFile
    val percentage = if (totalUsed > 0) {
        ((stats.totalSize.toDouble() / totalUsed) * 100).roundToInt()
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

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 3: Largest File Row
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun LargestFileRow(
    entry: LargestFileEntry,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 4: Recent File Row
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun RecentFileRow(
    entry: RecentFileEntry,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                tint = MaterialTheme.colorScheme.primary,
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
                    text = getRelativeDate(entry.lastModified),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = Formatter.formatShortFileSize(context, entry.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
    }
}

/**
 * Convert a timestamp to a human-readable relative date string.
 */
private fun getRelativeDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp
    val diffDays = (diffMs / (24 * 60 * 60 * 1000L)).toInt()

    return when {
        diffDays == 0 -> "Today"
        diffDays == 1 -> "Yesterday"
        diffDays < 7 -> "$diffDays days ago"
        else -> {
            // Fallback to simple date
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// STAGE 5: Cleanup Suggestion Card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CleanupSuggestionCard(
    suggestion: CleanupSuggestion,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = suggestion.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron indicator
            Text(
                text = "\u203A", // single right-pointing angle quotation mark
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}