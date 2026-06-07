package com.yourfiles.manager.app

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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


class App : Application() {

    private lateinit var navController: NavHostController

    lateinit var imageLoader: ImageLoader

    lateinit var db: AppDatabase


    fun navController(): NavHostController {
        return instance.navController
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
        // Clean up old trash on app start (fire and forget)
        CoroutineScope(Dispatchers.IO).launch {
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


/**
 * Root composable for the app.
 *
 * Wraps everything in the M3 theme, then delegates entirely to
 * [IndianFileRouter] which owns the Scaffold, bottom navigation bar,
 * and NavHost for the Indian edition.
 */
@ExperimentalFoundationApi
@Composable
fun YourFilesApp(
    modifier: Modifier = Modifier,
) {
    com.yourfiles.manager.app.uim3.theme.AppTheme {
        IndianFileRouter(modifier = modifier)
    }
}
