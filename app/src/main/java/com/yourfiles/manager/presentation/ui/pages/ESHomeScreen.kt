package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.text.format.Formatter
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.yourfiles.manager.R
import com.yourfiles.manager.app.AppThemeManager
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.uim3.theme.CATEGORY_COLORS
import com.yourfiles.manager.app.uim3.theme.CATEGORY_ICONS
import com.yourfiles.manager.presentation.vm.CategoryType
import com.yourfiles.manager.presentation.vm.StorageCategory

// ═══════════════════════════════════════════════════════════════════════════
// Data classes
// ═══════════════════════════════════════════════════════════════════════════

/** Storage volume info for home screen cards. */
private data class VolumeInfo(
    val name: String,
    val path: String,
    val totalBytes: Long,
    val freeBytes: Long,
    val usedPercent: Int,
    val isRemovable: Boolean,
)

private data class CategoryItem(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val categoryType: CategoryType,
)

private data class ToolItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

// ═══════════════════════════════════════════════════════════════════════════
// Storage detection
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Detect all mounted storage volumes using StorageManager API.
 * Returns a list of [VolumeInfo] — primary first, then removable.
 */
private fun getVolumeInfos(context: android.content.Context): List<VolumeInfo> {
    val volumes = mutableListOf<VolumeInfo>()
    val storageManager = context.getSystemService<StorageManager>() ?: return volumes

    val storageVolumes = storageManager.storageVolumes
    for (volume in storageVolumes) {
        if (volume.state != Environment.MEDIA_MOUNTED &&
            volume.state != Environment.MEDIA_MOUNTED_READ_ONLY
        ) continue

        val dir = volume.directory
        if (dir == null) {
            Log.w("ESHome", "Volume '${volume.getDescription(context)}' has null directory, skipping")
            continue
        }

        val path = dir.absolutePath
        val stat = try { StatFs(path) } catch (e: Exception) { continue }
        val total = stat.totalBytes
        val free = stat.availableBytes
        val used = total - free
        val pct = if (total > 0) (used * 100 / total).toInt() else 0

        volumes.add(
            VolumeInfo(
                name = if (volume.isPrimary)
                    context.getString(R.string.home_internal_storage)
                else
                    context.getString(R.string.home_sd_card),
                path = path,
                totalBytes = total,
                freeBytes = free,
                usedPercent = pct,
                isRemovable = !volume.isPrimary,
            )
        )
    }

    return volumes
}

// ═══════════════════════════════════════════════════════════════════════════
// Main Home Screen — ES Explorer style
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESHomeScreen(
    onNavigateToExplorer: (path: String) -> Unit,
    onNavigateToRoute: (route: String) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath
    val themeColors = AppThemeManager.currentThemeColors.value
    val useColoredHeader = themeColors.coloredHeader

    val categories = listOf(
        CategoryItem(stringResource(R.string.category_images), CATEGORY_ICONS[StorageCategory.IMAGES]!!, CATEGORY_COLORS[StorageCategory.IMAGES]!!, CategoryType.IMAGES),
        CategoryItem(stringResource(R.string.category_videos), CATEGORY_ICONS[StorageCategory.VIDEOS]!!, CATEGORY_COLORS[StorageCategory.VIDEOS]!!, CategoryType.VIDEOS),
        CategoryItem(stringResource(R.string.category_documents), CATEGORY_ICONS[StorageCategory.DOCUMENTS]!!, CATEGORY_COLORS[StorageCategory.DOCUMENTS]!!, CategoryType.DOCUMENTS),
        CategoryItem(stringResource(R.string.category_music), CATEGORY_ICONS[StorageCategory.AUDIO]!!, CATEGORY_COLORS[StorageCategory.AUDIO]!!, CategoryType.AUDIO),
        CategoryItem(stringResource(R.string.category_apks), CATEGORY_ICONS[StorageCategory.APK]!!, CATEGORY_COLORS[StorageCategory.APK]!!, CategoryType.APK),
    )

    // ES Explorer-style tools (only real, functional tools)
    val tools = listOf(
        ToolItem(stringResource(R.string.home_tool_cleaner), Icons.Outlined.CleaningServices, Routes.FLAT_DUPLICATES_FILE_MANAGER),
        ToolItem(stringResource(R.string.home_tool_analyzer), Icons.Outlined.Analytics, Routes.ANALYZER),
        ToolItem(stringResource(R.string.home_tool_optimise), Icons.Outlined.PhotoSizeSelectLarge, Routes.OPTIMISE_IMAGES),
        ToolItem(stringResource(R.string.home_tool_recycle_bin), Icons.Outlined.DeleteOutline, Routes.TRASH),
    )

    // Detect all storage volumes
    val volumes = remember { getVolumeInfos(context) }
    val internalVol = volumes.firstOrNull { !it.isRemovable }
    val sdVol = volumes.firstOrNull { it.isRemovable }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // ES Explorer style: embedded search bar in header
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = if (useColoredHeader)
                            Color.White.copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onNavigateToExplorer(primaryPath) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = null,
                                tint = if (useColoredHeader)
                                    Color.White.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.drawer_home),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (useColoredHeader)
                                    Color.White.copy(alpha = 0.9f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.cd_menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToExplorer(primaryPath) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.cd_search),
                        )
                    }
                },
                colors = if (useColoredHeader) {
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    )
                } else {
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ═══════════════════════════════════════════════════════════════
            // §1 — STORAGE CARDS with circular progress (ES Explorer style)
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (internalVol != null) {
                    StorageCardCircular(
                        volume = internalVol,
                        modifier = Modifier.weight(1f),
                        progressColor = if (internalVol.usedPercent > 85)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateToExplorer(internalVol.path) },
                    )
                }
                if (sdVol != null) {
                    StorageCardCircular(
                        volume = sdVol,
                        modifier = Modifier.weight(1f),
                        progressColor = MaterialTheme.colorScheme.secondary,
                        onClick = { onNavigateToExplorer(sdVol.path) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ═══════════════════════════════════════════════════════════════
            // §2 — CATEGORIES (ES Explorer: colored icon buttons in a row)
            // ═══════════════════════════════════════════════════════════════
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = themeColors.cardElevation.dp
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    categories.forEach { cat ->
                        Column(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onNavigateToRoute("${Routes.MEDIA_STORE_CATEGORY}/${cat.categoryType.key}")
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            // Colored circle background for icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = cat.color.copy(alpha = 0.15f),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.label,
                                    tint = cat.color,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════════════════════════════════════════════════════════
            // §3 — TOOLS GRID (ES Explorer: 2 rows × 5 cols, white cards)
            // ═══════════════════════════════════════════════════════════════
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = themeColors.cardElevation.dp
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    tools.forEach { tool ->
                        ToolIcon(
                            label = tool.label,
                            icon = tool.icon,
                            onClick = { onNavigateToRoute(tool.route) },
                            tintColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Storage Card with CIRCULAR progress (ES Explorer style)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StorageCardCircular(
    volume: VolumeInfo,
    modifier: Modifier = Modifier,
    progressColor: Color,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val usedBytes = volume.totalBytes - volume.freeBytes

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppThemeManager.currentThemeColors.value.cardElevation.dp
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Circular progress indicator
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier.size(64.dp),
                ) {
                    val strokeWidth = 6.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Background circle
                    drawCircle(
                        color = progressColor.copy(alpha = 0.15f),
                        radius = diameter / 2,
                        center = topLeft + Offset(diameter / 2, diameter / 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                    // Progress arc
                    val sweep = (volume.usedPercent / 100f) * 360f
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    )
                }
                // Percentage text in center
                Text(
                    text = "${volume.usedPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Storage name
            Text(
                text = volume.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Used / Total
            Text(
                text = stringResource(
                    R.string.home_free_of,
                    Formatter.formatShortFileSize(context, usedBytes),
                    Formatter.formatShortFileSize(context, volume.totalBytes)
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Tool Icon (ES Explorer: icon + label, compact)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun ToolIcon(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tintColor: Color,
) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
