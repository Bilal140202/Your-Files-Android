package com.yourfiles.manager.app

import android.net.Uri
import androidx.compose.material3.DrawerState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yourfiles.manager.app.Routes.Companion.APK_INFO
import com.yourfiles.manager.app.Routes.Companion.EXPLORER
import com.yourfiles.manager.app.Routes.Companion.FILE_DETAIL_VIEWER
import com.yourfiles.manager.app.Routes.Companion.FOLDER_ORGANISER
import com.yourfiles.manager.app.Routes.Companion.FLAT_DUPLICATES_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.FLAT_IMAGES_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.FLAT_LARGE_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.FLAT_SCREENSHOTS_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.FLAT_VIDEOS_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.FLAT_WHATSAPP_FILE_MANAGER
import com.yourfiles.manager.app.Routes.Companion.HOME
import com.yourfiles.manager.app.Routes.Companion.MEDIA_STORE_CATEGORY
import com.yourfiles.manager.app.Routes.Companion.OPTIMISE_IMAGES
import com.yourfiles.manager.app.Routes.Companion.SETTINGS
import com.yourfiles.manager.app.Routes.Companion.TRASH
import com.yourfiles.manager.presentation.ui.components.ESDrawerContent
import com.yourfiles.manager.presentation.ui.pages.ESHomeScreen
import com.yourfiles.manager.presentation.ui.pages.FileBrowserScreen
import com.yourfiles.manager.presentation.ui.pages.FlatFileManager
import com.yourfiles.manager.presentation.ui.pages.FlatImagesFileManager
import com.yourfiles.manager.presentation.ui.pages.FlatLargeFilesManager
import com.yourfiles.manager.presentation.ui.pages.FlatScreenshotsFileManager
import com.yourfiles.manager.presentation.ui.pages.FlatVideosFileManager
import com.yourfiles.manager.presentation.ui.pages.ImageOptimiserPage
import com.yourfiles.manager.presentation.ui.pages.SettingsPage
import com.yourfiles.manager.presentation.ui.pages.StorageAnalyzerScreen
import com.yourfiles.manager.presentation.ui.pages.TrashPage
import com.yourfiles.manager.presentation.ui.pages.WhatsAppCleanerPage
import com.yourfiles.manager.presentation.ui.pages.FileDetailViewerCompose
import com.yourfiles.manager.presentation.ui.pages.FolderOrganiserScreen
import com.yourfiles.manager.presentation.ui.pages.ApkInfoScreen
import com.yourfiles.manager.presentation.ui.pages.MediaStoreCategoryScreen
import com.yourfiles.manager.presentation.vm.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun buildAppGraph(
    drawerState: DrawerState,
    scope: CoroutineScope,
): NavGraphBuilder.() -> Unit = {
    composable(HOME) {
        ESHomeScreen(
            onNavigateToExplorer = { path ->
                App.instance.navController().navigate("${EXPLORER}?path=${Uri.encode(path)}")
            },
            onNavigateToRoute = { route ->
                App.instance.navController().navigate(route)
            },
            onOpenDrawer = {
                scope.launch { drawerState.open() }
            },
        )
    }
    composable(
        route = "$EXPLORER?path={path}",
        arguments = listOf(
            navArgument("path") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val path = backStackEntry.arguments?.getString("path")
        FileBrowserScreen(
            initialPath = path,
            onOpenDrawer = {
                scope.launch { drawerState.open() }
            },
        )
    }
    composable(OPTIMISE_IMAGES) {
        ImageOptimiserPage()
    }
    composable(FLAT_DUPLICATES_FILE_MANAGER) {
        FlatFileManager()
    }
    composable(FLAT_VIDEOS_FILE_MANAGER) {
        FlatVideosFileManager()
    }
    composable(FLAT_IMAGES_FILE_MANAGER) {
        FlatImagesFileManager()
    }
    composable(route = FLAT_LARGE_FILE_MANAGER) {
        FlatLargeFilesManager()
    }
    composable(FLAT_SCREENSHOTS_FILE_MANAGER) {
        FlatScreenshotsFileManager()
    }
    composable(FLAT_WHATSAPP_FILE_MANAGER) {
        WhatsAppCleanerPage()
    }
    composable(TRASH) {
        TrashPage()
    }
    composable(Routes.ANALYZER) {
        StorageAnalyzerScreen(
            onNavigateToExplorer = { path ->
                App.instance.navController().navigate("${EXPLORER}?path=${Uri.encode(path)}")
            }
        )
    }
    composable(
        route = "$MEDIA_STORE_CATEGORY/{categoryType}",
        arguments = listOf(
            navArgument("categoryType") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val typeStr = backStackEntry.arguments?.getString("categoryType") ?: "images"
        val categoryType = CategoryType.fromKey(typeStr)
        MediaStoreCategoryScreen(categoryType = categoryType)
    }
    composable(SETTINGS) {
        SettingsPage()
    }
    composable(
        route = "$FOLDER_ORGANISER?path={path}",
        arguments = listOf(
            navArgument("path") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        FolderOrganiserScreen()
    }
    composable(
        route = "$APK_INFO?filePath={filePath}",
        arguments = listOf(
            navArgument("filePath") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val filePath = backStackEntry.arguments?.getString("filePath")
        if (filePath != null) {
            ApkInfoScreen(filePath = Uri.decode(filePath))
        }
    }
    composable(
        "$FILE_DETAIL_VIEWER?url={url}&category={category}&md5={md5}",
        arguments = listOf(
            navArgument("url") { type = NavType.StringType },
            navArgument("category") { type = NavType.StringType },
            navArgument("md5") { type = NavType.StringType; nullable = true; defaultValue = null },
        )
    ) { backStackEntry ->
        val url = backStackEntry.arguments?.getString("url")!!
        val category = backStackEntry.arguments?.getString("category") ?: ""
        val md5 = backStackEntry.arguments?.getString("md5")
        val resolvedUrl = Uri.decode(url)
        FileDetailViewerCompose(resolvedUrl, category = category, md5 = md5)
    }
}


interface Routes {
    companion object {
        const val HOME = "/home"
        const val EXPLORER = "/explorer"
        const val FLAT_DUPLICATES_FILE_MANAGER = "/flat-duplicates-file-manager"
        const val FLAT_IMAGES_FILE_MANAGER = "/flat-images-file-manager"
        const val FLAT_VIDEOS_FILE_MANAGER = "/flat-videos-file-manager"
        const val FLAT_LARGE_FILE_MANAGER = "/flat-large-file-manager"
        const val FLAT_SCREENSHOTS_FILE_MANAGER = "/flat-screenshots-file-manager"
        const val FLAT_WHATSAPP_FILE_MANAGER = "/flat-whatsapp-file-manager"
        const val OPTIMISE_IMAGES = "/optimise-images"
        const val FILE_DETAIL_VIEWER = "/file-detail-viewer"
        const val TRASH = "/trash"
        const val SETTINGS = "/settings"
        const val ANALYZER = "/analyzer"
        const val APK_INFO = "/apk-info"
        const val MEDIA_STORE_CATEGORY = "/media-category"
        const val FOLDER_ORGANISER = "/folder-organiser"
    }
}
