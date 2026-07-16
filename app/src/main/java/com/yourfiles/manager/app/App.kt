package com.yourfiles.manager.app

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.yourfiles.manager.data.db.AppDatabase
import com.yourfiles.manager.utils.SavedMemoryTracker
import com.yourfiles.manager.utils.TrashManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope


class App : Application() {

    lateinit var imageLoader: ImageLoader
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDB()
        initLibraries()
        SavedMemoryTracker.initialize()
        DarkModeSetting.load(applicationContext)
        AppThemeManager.load(applicationContext)
        CoroutineScope(Dispatchers.IO).launch {
            TrashManager.cleanupOldTrash(applicationContext)
        }
    }

    private fun initDB() {
        db = Room.databaseBuilder(
            instance.applicationContext, AppDatabase::class.java, "yourfiles-database"
        ).fallbackToDestructiveMigration(true).build()
    }

    private fun initLibraries() {
        imageLoader = ImageLoader.Builder(instance).memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED).components {
                add(VideoFrameDecoder.Factory())
            }.crossfade(true).build()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}


@ExperimentalFoundationApi
@Composable
fun YourFilesApp(
    modifier: Modifier = Modifier,
    startDestination: String = Routes.HOME,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // ── Dark mode ─────────────────────────────────────────────────────────
    val isDark by remember { DarkModeSetting.isDarkMode }

    CompositionLocalProvider(LocalNavController provides navController) {
        com.yourfiles.manager.app.uim3.theme.AppTheme(
            darkTheme = isDark,
            themeColors = AppThemeManager.currentThemeColors.value,
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    com.yourfiles.manager.presentation.ui.components.ESDrawerContent(
                        drawerState,
                        currentRoute = navController.currentDestination?.route,
                    )
                },
            ) {
                NavHost(
                    modifier = modifier,
                    navController = navController,
                    startDestination = startDestination,
                    enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) + slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(200)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(300)) },
                    popExitTransition = { fadeOut(animationSpec = tween(200)) + slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = tween(200)) },
                    builder = buildAppGraph(drawerState, scope, navController)
                )
            }
        }
    }
}