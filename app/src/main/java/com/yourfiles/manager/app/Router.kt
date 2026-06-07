package com.yourfiles.manager.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yourfiles.manager.presentation.ui.pages.FileDetailViewerCompose
import com.yourfiles.manager.presentation.ui.pages.IndianSettingsScreen
import com.yourfiles.manager.presentation.ui.pages.SafaiHomeScreen
import com.yourfiles.manager.presentation.ui.pages.StorageChooserScreen
import com.yourfiles.manager.presentation.ui.pages.FlatFileManager
import java.net.URLEncoder

// ══════════════════════════════════════════════════════════════════════════════
// Route constants
// ══════════════════════════════════════════════════════════════════════════════
object IndianRoutes {
    const val STORAGE_CHOOSER = "storage_chooser"
    const val FILE_BROWSER = "file_browser?path={path}"
    const val FILE_DETAIL_VIEWER = "file_detail?url={url}&category={category}&md5={md5}"
    const val SAFAI_HOME = "safai_home"
    const val SAFAI_DUPLICATES = "safai_duplicates"
    const val APPS_MANAGER = "apps_manager"
    const val TOOLS_HOME = "tools_home"
    const val SETTINGS = "settings"
}

// Legacy routes kept for gradual migration
object Routes {
    const val HOME = IndianRoutes.STORAGE_CHOOSER
    const val FLAT_DUPLICATES_FILE_MANAGER = IndianRoutes.SAFAI_DUPLICATES
    const val FLAT_IMAGES_FILE_MANAGER = "flat_images"
    const val FLAT_VIDEOS_FILE_MANAGER = "flat_videos"
    const val FLAT_LARGE_FILE_MANAGER = "flat_large"
    const val FLAT_SCREENSHOTS_FILE_MANAGER = "flat_screenshots"
    const val FLAT_WHATSAPP_FILE_MANAGER = "whatsapp_cleaner"
    const val FILE_DETAIL_VIEWER = IndianRoutes.FILE_DETAIL_VIEWER
    const val TRASH = "trash"
    const val SETTINGS = IndianRoutes.SETTINGS
    const val OPTIMISE_IMAGES = "optimise_images"
}

// ══════════════════════════════════════════════════════════════════════════════
// Bottom navigation data
// ══════════════════════════════════════════════════════════════════════════════
private data class BottomNavTab(
    val label: String,
    val homeRoute: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
)

private val BOTTOM_NAV_TABS = listOf(
    BottomNavTab("Files", IndianRoutes.STORAGE_CHOOSER, Icons.Outlined.Folder, Icons.Filled.Folder),
    BottomNavTab("Clean", IndianRoutes.SAFAI_HOME, Icons.Outlined.CleaningServices, Icons.Filled.CleaningServices),
    BottomNavTab("Apps", IndianRoutes.APPS_MANAGER, Icons.Outlined.Apps, Icons.Filled.Apps),
    BottomNavTab("Tools", IndianRoutes.TOOLS_HOME, Icons.Outlined.Build, Icons.Filled.Build),
    BottomNavTab("Me", IndianRoutes.SETTINGS, Icons.Outlined.Person, Icons.Filled.Person),
)

private fun resolveTabIndex(route: String?): Int {
    if (route == null) return 0
    for ((i, tab) in BOTTOM_NAV_TABS.withIndex()) {
        if (route.startsWith(tab.homeRoute.split("?")[0])) return i
    }
    return 0
}

// ══════════════════════════════════════════════════════════════════════════════
// Main router composable
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndianFileRouter(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val resolvedIndex = resolveTabIndex(currentRoute)

    LaunchedEffect(resolvedIndex) {
        selectedTabIndex = resolvedIndex
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                BOTTOM_NAV_TABS.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (selectedTabIndex != index) {
                                selectedTabIndex = index
                            }
                            navController.navigate(tab.homeRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = IndianRoutes.STORAGE_CHOOSER,
            modifier = Modifier.padding(innerPadding),
        ) {
            // ---- FILES tab ----
            composable(route = IndianRoutes.STORAGE_CHOOSER) {
                StorageChooserScreen(navController = navController)
            }

            composable(
                route = "file_browser?path={path}",
                arguments = listOf(
                    navArgument("path") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { entry ->
                val path = entry.arguments?.getString("path")
                // Use the existing FlatFileManager as a file browser view
                // For now show path info
                FileBrowserStubScreen(path = path ?: "/")
            }

            composable(
                route = "file_detail?url={url}&category={category}&md5={md5}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                    navArgument("category") { type = NavType.StringType },
                    navArgument("md5") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) { entry ->
                val url = entry.arguments?.getString("url") ?: ""
                val category = entry.arguments?.getString("category") ?: ""
                val md5 = entry.arguments?.getString("md5")
                FileDetailViewerCompose(
                    filePath = url,
                    category = category,
                    md5 = md5,
                )
            }

            // ---- CLEAN (Safai) tab ----
            composable(route = IndianRoutes.SAFAI_HOME) {
                SafaiHomeScreen(navController = navController)
            }

            composable(route = IndianRoutes.SAFAI_DUPLICATES) {
                FlatFileManager()
            }

            // ---- APPS tab ----
            composable(route = IndianRoutes.APPS_MANAGER) {
                ComingSoonScreen("Apps Manager", "View, backup, and uninstall your apps")
            }

            // ---- TOOLS tab ----
            composable(route = IndianRoutes.TOOLS_HOME) {
                ToolsHomeScreen()
            }

            // ---- ME (Settings) tab ----
            composable(route = IndianRoutes.SETTINGS) {
                IndianSettingsScreen(navController = navController)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Stub composables
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FileBrowserStubScreen(path: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("File Browser", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Path: $path", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ComingSoonScreen(title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ToolsHomeScreen() {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column {
            Text("Tools", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            listOf(
                "FTP Server" to "Host files over WiFi",
                "Text Editor" to "Edit text files",
                "Root Explorer" to "Browse with root access",
                "Bulk Rename" to "Rename multiple files",
                "Image Viewer" to "View photos & videos",
            ).forEach { (name, desc) ->
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        Text(desc, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
