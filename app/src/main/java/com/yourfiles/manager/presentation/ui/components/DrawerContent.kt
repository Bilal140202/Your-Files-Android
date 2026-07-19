package com.yourfiles.manager.presentation.ui.components

import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Settings
import com.yourfiles.manager.R
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.LocalNavController
import com.yourfiles.manager.app.AppThemeManager
import kotlinx.coroutines.launch

private data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String? = null,
    val path: String? = null,
    val isSection: Boolean = false,
    val sectionLabel: String? = null,
)

@Composable
fun ESDrawerContent(drawerState: DrawerState, currentRoute: String? = null) {
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val context = LocalContext.current
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath
    val themeColors = AppThemeManager.currentThemeColors.value
    val useColoredHeader = themeColors.coloredHeader

    // Resolve the actual explorer path when on an explorer route
    val currentExplorerPath = if (currentRoute?.startsWith(Routes.EXPLORER) == true) {
        navController.currentBackStackEntry?.arguments?.getString("path")
    } else {
        null
    }

    // Storage stats for bottom bar
    val storageInfo = remember {
        try {
            val stat = StatFs(primaryPath)
            val total = stat.totalBytes
            val free = stat.availableBytes
            val used = total - free
            val pct = if (total > 0) (used * 100 / total).toFloat() / 100f else 0f
            Triple(used, total, pct)
        } catch (_: Exception) {
            Triple(0L, 0L, 0f)
        }
    }

    fun navigate(routeOrPath: String, isRoute: Boolean) {
        scope.launch { drawerState.close() }
        navController.navigate(
            if (isRoute) routeOrPath
            else "${Routes.EXPLORER}?path=${android.net.Uri.encode(routeOrPath)}"
        ) {
            popUpTo(Routes.HOME) { inclusive = false }
            launchSingleTop = true
        }
    }

    val menuItems = listOf(
        // Browse section
        DrawerMenuItem(stringResource(R.string.drawer_home), Icons.Outlined.Home, route = Routes.HOME, sectionLabel = "BROWSE"),
        DrawerMenuItem(stringResource(R.string.home_internal_storage), Icons.Outlined.SdStorage, path = primaryPath),
        DrawerMenuItem(stringResource(R.string.drawer_downloads), Icons.Outlined.Download, path = "$primaryPath/Download"),
        // Categories section
        DrawerMenuItem(stringResource(R.string.category_images), Icons.Outlined.Image, route = "${Routes.MEDIA_STORE_CATEGORY}/images", sectionLabel = "CATEGORIES"),
        DrawerMenuItem(stringResource(R.string.category_videos), Icons.Outlined.Movie, route = "${Routes.MEDIA_STORE_CATEGORY}/videos"),
        DrawerMenuItem(stringResource(R.string.category_documents), Icons.Outlined.Description, route = "${Routes.MEDIA_STORE_CATEGORY}/documents"),
        DrawerMenuItem(stringResource(R.string.category_music), Icons.Outlined.MusicNote, route = "${Routes.MEDIA_STORE_CATEGORY}/audio"),
        DrawerMenuItem(stringResource(R.string.category_apks), Icons.Outlined.Memory, route = "${Routes.MEDIA_STORE_CATEGORY}/apk"),
        // Tools section
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem(stringResource(R.string.home_tool_cleaner), Icons.Outlined.CleaningServices, route = Routes.FLAT_DUPLICATES_FILE_MANAGER, sectionLabel = "TOOLS"),
        DrawerMenuItem(stringResource(R.string.drawer_storage_analyzer), Icons.Outlined.Analytics, route = Routes.ANALYZER),
        DrawerMenuItem(stringResource(R.string.drawer_image_optimiser), Icons.Outlined.PhotoSizeSelectLarge, route = Routes.OPTIMISE_IMAGES),
        DrawerMenuItem(stringResource(R.string.home_tool_recycle_bin), Icons.Outlined.DeleteOutline, route = Routes.TRASH),
        // Settings
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem(stringResource(R.string.drawer_settings), Icons.Outlined.Settings, route = Routes.SETTINGS, sectionLabel = "GENERAL"),
    )

    ModalDrawerSheet {
        // ── Colored header with app branding ────────────────────────
        Surface(
            color = if (useColoredHeader)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (useColoredHeader) Color.White else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.drawer_file_manager_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (useColoredHeader)
                        Color.White.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── Scrollable menu items ──────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f),
        ) {
            var lastSection: String? = null
            menuItems.forEach { item ->
                if (item.isSection) {
                    Divider(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    lastSection = null
                } else {
                    // Section header label
                    if (item.sectionLabel != null && item.sectionLabel != lastSection) {
                        Text(
                            text = item.sectionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 2.dp),
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                        )
                        lastSection = item.sectionLabel
                    }

                    val selected = when {
                        item.route != null -> {
                            if (item.route.startsWith("${Routes.MEDIA_STORE_CATEGORY}/")) {
                                currentRoute?.startsWith("${Routes.MEDIA_STORE_CATEGORY}/") == true &&
                                    currentRoute == item.route
                            } else {
                                currentRoute == item.route
                            }
                        }
                        item.path != null -> {
                            currentRoute?.startsWith(Routes.EXPLORER) == true &&
                                currentExplorerPath == item.path
                        }
                        else -> false
                    }
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.height(22.dp)) },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 14.sp,
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (item.route != null) navigate(item.route, true)
                            else if (item.path != null) navigate(item.path, false)
                        },
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                        ),
                    )
                }
            }
        }

        // ── Storage usage bar at bottom (ES Explorer style) ──────
        Divider(
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_internal_storage),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
                Text(
                    text = "${Formatter.formatShortFileSize(context, storageInfo.first)} / ${Formatter.formatShortFileSize(context, storageInfo.second)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = storageInfo.third.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = if (storageInfo.third > 0.85f)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
        }
    }
}