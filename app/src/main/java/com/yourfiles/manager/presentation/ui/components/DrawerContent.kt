package com.yourfiles.manager.presentation.ui.components

import android.os.Environment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
import androidx.compose.ui.res.stringResource
import com.yourfiles.manager.R
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.LocalNavController
import kotlinx.coroutines.launch

private data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String? = null,
    val path: String? = null,
    val isSection: Boolean = false,
)

@Composable
fun ESDrawerContent(drawerState: DrawerState, currentRoute: String? = null) {
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath

    // Resolve the actual explorer path when on an explorer route
    val currentExplorerPath = if (currentRoute?.startsWith(Routes.EXPLORER) == true) {
        navController.currentBackStackEntry?.arguments?.getString("path")
    } else {
        null
    }

    fun navigate(routeOrPath: String, isRoute: Boolean) {
        scope.launch { drawerState.close() }
        if (isRoute) {
            navController.navigate(routeOrPath) {
                navController.graph.startDestinationRoute?.let { route ->
                    popUpTo(route) { saveState = true }
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate("${Routes.EXPLORER}?path=${android.net.Uri.encode(routeOrPath)}")
        }
    }

    val menuItems = listOf(
        DrawerMenuItem(stringResource(R.string.drawer_home), Icons.Outlined.Home, route = Routes.HOME),
        DrawerMenuItem(stringResource(R.string.home_internal_storage), Icons.Outlined.SdStorage, path = primaryPath),
        DrawerMenuItem(stringResource(R.string.drawer_downloads), Icons.Outlined.Download, path = "$primaryPath/Download"),
        DrawerMenuItem(stringResource(R.string.category_images), Icons.Outlined.Image, route = "${Routes.MEDIA_STORE_CATEGORY}/images"),
        DrawerMenuItem(stringResource(R.string.category_videos), Icons.Outlined.Movie, route = "${Routes.MEDIA_STORE_CATEGORY}/videos"),
        DrawerMenuItem(stringResource(R.string.category_documents), Icons.Outlined.Description, route = "${Routes.MEDIA_STORE_CATEGORY}/documents"),
        DrawerMenuItem(stringResource(R.string.category_music), Icons.Outlined.MusicNote, route = "${Routes.MEDIA_STORE_CATEGORY}/audio"),
        DrawerMenuItem(stringResource(R.string.category_apks), Icons.Outlined.Memory, route = "${Routes.MEDIA_STORE_CATEGORY}/apk"),
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem(stringResource(R.string.home_tool_cleaner), Icons.Outlined.CleaningServices, route = Routes.FLAT_DUPLICATES_FILE_MANAGER),
        DrawerMenuItem(stringResource(R.string.drawer_storage_analyzer), Icons.Outlined.Analytics, route = Routes.ANALYZER),
        DrawerMenuItem(stringResource(R.string.drawer_image_optimiser), Icons.Outlined.PhotoSizeSelectLarge, route = Routes.OPTIMISE_IMAGES),
        DrawerMenuItem(stringResource(R.string.home_tool_recycle_bin), Icons.Outlined.DeleteOutline, route = Routes.TRASH),
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem(stringResource(R.string.drawer_settings), Icons.Outlined.Settings, route = Routes.SETTINGS),
    )

    ModalDrawerSheet {
        // App header
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.drawer_file_manager_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))

        // Menu items in a scrollable column with weight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {
            var lastWasSection = false
            menuItems.forEach { item ->
                if (item.isSection) {
                    if (!lastWasSection) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp))
                    }
                    lastWasSection = true
                } else {
                    lastWasSection = false
                    val selected = when {
                        item.route != null -> {
                            // Handle parameterised routes like /media-category/{categoryType}
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
                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.height(24.dp)) },
                        label = { Text(item.label, style = MaterialTheme.typography.bodyMedium) },
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
    }
}