package com.yourfiles.manager.app

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavOptions
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.room.Room
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import com.yourfiles.manager.data.db.AppDatabase
import com.yourfiles.manager.utils.SavedMemoryTracker
import com.yourfiles.manager.utils.TrashManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


class App : Application() {

    private var navController: NavHostController? = null

    lateinit var imageLoader: ImageLoader

    lateinit var db: AppDatabase

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun navController(): NavHostController {
        return navController ?: throw IllegalStateException("NavController not initialized")
    }

    fun initNavController(navController: NavHostController) {
        this.navController = navController
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initDB()
        initLibraries()
        SavedMemoryTracker.initialize()
        applicationScope.launch {
            TrashManager.cleanupOldTrash(applicationContext)
        }
    }

    private fun initDB() {
        db = Room.databaseBuilder(
            instance.applicationContext, AppDatabase::class.java, "yourfiles-database"
        ).fallbackToDestructiveMigration(true)
            .build()
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

    com.yourfiles.manager.app.uim3.theme.AppTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                com.yourfiles.manager.presentation.ui.components.ESDrawerContent(drawerState)
            },
        ) {
            NavHost(
                modifier = modifier,
                navController = App.instance.navController(),
                startDestination = startDestination,
                // ZERO transitions — prevent ghost overlay and flash on back navigation
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                builder = buildAppGraph(drawerState, scope)
            )
        }
    }
}
