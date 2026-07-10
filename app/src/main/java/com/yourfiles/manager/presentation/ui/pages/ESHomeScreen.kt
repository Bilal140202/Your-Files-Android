package com.yourfiles.manager.presentation.ui.pages

import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.text.format.Formatter
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import com.yourfiles.manager.R
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.presentation.vm.CategoryType
import com.yourfiles.manager.app.uim3.theme.CATEGORY_COLORS
import com.yourfiles.manager.app.uim3.theme.CATEGORY_ICONS
import com.yourfiles.manager.presentation.vm.StorageCategory
import java.io.File

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
            // Android 11+ without MANAGE_EXTERNAL_STORAGE: directory may be null for SD.
            // Try to guess path from volume description or UUID.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESHomeScreen(
    onNavigateToExplorer: (path: String) -> Unit,
    onNavigateToRoute: (route: String) -> Unit,
    onOpenDrawer: () -> Unit,
) {
    val context = LocalContext.current
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath

    val categories = listOf(
        CategoryItem(stringResource(R.string.category_images), CATEGORY_ICONS[StorageCategory.IMAGES]!!, CATEGORY_COLORS[StorageCategory.IMAGES]!!, CategoryType.IMAGES),
        CategoryItem(stringResource(R.string.category_videos), CATEGORY_ICONS[StorageCategory.VIDEOS]!!, CATEGORY_COLORS[StorageCategory.VIDEOS]!!, CategoryType.VIDEOS),
        CategoryItem(stringResource(R.string.category_documents), CATEGORY_ICONS[StorageCategory.DOCUMENTS]!!, CATEGORY_COLORS[StorageCategory.DOCUMENTS]!!, CategoryType.DOCUMENTS),
        CategoryItem(stringResource(R.string.category_music), CATEGORY_ICONS[StorageCategory.AUDIO]!!, CATEGORY_COLORS[StorageCategory.AUDIO]!!, CategoryType.AUDIO),
        CategoryItem(stringResource(R.string.category_apks), CATEGORY_ICONS[StorageCategory.APK]!!, CATEGORY_COLORS[StorageCategory.APK]!!, CategoryType.APK),
    )

    val tools = listOf(
        ToolItem(stringResource(R.string.home_tool_cleaner), Icons.Outlined.CleaningServices, Routes.FLAT_DUPLICATES_FILE_MANAGER),
        ToolItem(stringResource(R.string.home_tool_analyzer), Icons.Outlined.Analytics, Routes.ANALYZER),
        ToolItem(stringResource(R.string.home_tool_optimise), Icons.Outlined.PhotoSizeSelectLarge, Routes.OPTIMISE_IMAGES),
        ToolItem(stringResource(R.string.home_tool_recycle_bin), Icons.Outlined.DeleteOutline, Routes.TRASH),
    )

    // Detect all storage volumes
    val volumes = remember { getVolumeInfos(context) }
    val hasSdCard = volumes.count { it.isRemovable } > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.Transparent,
                    ) {
                        androidx.compose.material3.IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = stringResource(R.string.cd_menu),
                            )
                        }
                    }
                },
                actions = {
                    Surface(color = Color.Transparent) {
                        androidx.compose.material3.IconButton(onClick = {
                            onNavigateToExplorer(primaryPath)
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.cd_search),
                            )
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ═══════════════════════════════════════════════════════════════
            // §1 — STORAGE CARDS (side-by-side: Internal + SD, or full-width Internal)
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Internal Storage card
                val internalVol = volumes.firstOrNull { !it.isRemovable }
                if (internalVol != null) {
                    StorageCard(
                        volume = internalVol,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { onNavigateToExplorer(internalVol.path) },
                    )
                }

                // SD Card (only if mounted)
                val sdVol = volumes.firstOrNull { it.isRemovable }
                if (sdVol != null) {
                    StorageCard(
                        volume = sdVol,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50),
                        onClick = { onNavigateToExplorer(sdVol.path) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ═══════════════════════════════════════════════════════════════
            // §2 — CATEGORIES (compact row, 32dp icons)
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                categories.forEach { cat ->
                    Column(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onNavigateToRoute("${Routes.MEDIA_STORE_CATEGORY}/${cat.categoryType.key}")
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.label,
                            tint = cat.color,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════════════════════════════════════════════════════════
            // §3 — TOOLS GRID (4 cols, 8dp spacing, compact)
            // ═══════════════════════════════════════════════════════════════
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tools.forEach { tool ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToRoute(tool.route) },
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ════════════════════════════════════════════════════════════════════════
// Storage Card composable — used for both Internal and SD
// ════════════════════════════════════════════════════════════════════════

@Composable
private fun StorageCard(
    volume: VolumeInfo,
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val usedBytes = volume.totalBytes - volume.freeBytes

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Title row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.SdStorage,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = volume.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                shape = RoundedCornerShape(3.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(volume.usedPercent / 100f)
                        .height(6.dp),
                    shape = RoundedCornerShape(3.dp),
                    color = color,
                ) {}
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Used / Free
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = Formatter.formatShortFileSize(context, usedBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
                Text(
                    text = "${volume.usedPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = stringResource(R.string.home_free_of, Formatter.formatShortFileSize(context, volume.freeBytes), Formatter.formatShortFileSize(context, volume.totalBytes)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
            )
        }
    }
}
