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
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Settings
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.app.App
import kotlinx.coroutines.launch

private data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String? = null,
    val path: String? = null,
    val isSection: Boolean = false,
)

@Composable
fun ESDrawerContent(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    val navController = App.instance.navController()
    val primaryPath = Environment.getExternalStorageDirectory().absolutePath

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
        DrawerMenuItem("Home", Icons.Outlined.Home, route = Routes.HOME),
        DrawerMenuItem("Internal Storage", Icons.Outlined.SdStorage, path = primaryPath),
        DrawerMenuItem("Downloads", Icons.Outlined.Download, path = "$primaryPath/Download"),
        DrawerMenuItem("Images", Icons.Outlined.Image, path = "$primaryPath/Pictures"),
        DrawerMenuItem("Videos", Icons.Outlined.Movie, path = "$primaryPath/Movies"),
        DrawerMenuItem("Documents", Icons.Outlined.Description, path = "$primaryPath/Documents"),
        DrawerMenuItem("Music", Icons.Outlined.MusicNote, path = "$primaryPath/Music"),
        DrawerMenuItem("APKs", Icons.Outlined.Memory, path = "$primaryPath/Download"),
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem("Cleaner", Icons.Outlined.CleaningServices, route = Routes.FLAT_DUPLICATES_FILE_MANAGER),
        DrawerMenuItem("Storage Analyzer", Icons.Outlined.Analytics, route = Routes.ANALYZER),
        DrawerMenuItem("Image Optimiser", Icons.Outlined.PhotoSizeSelectLarge, route = Routes.OPTIMISE_IMAGES),
        DrawerMenuItem("Folder Organiser", Icons.Outlined.FolderOpen, route = Routes.FOLDER_ORGANISER),
        DrawerMenuItem("", Icons.Outlined.Folder, isSection = true),
        DrawerMenuItem("Settings", Icons.Outlined.Settings, route = Routes.SETTINGS),
    )

    ModalDrawerSheet {
        // App header
        Text(
            text = "Your Files",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "File Manager v1.0",
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
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null, modifier = Modifier.height(24.dp)) },
                        label = { Text(item.label, style = MaterialTheme.typography.bodyMedium) },
                        selected = false,
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
