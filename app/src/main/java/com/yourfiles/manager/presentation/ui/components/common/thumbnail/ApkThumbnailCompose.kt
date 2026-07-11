package com.yourfiles.manager.presentation.ui.components.common.thumbnail

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import com.yourfiles.manager.R

/**
 * APK icon thumbnail — loads the actual app icon from the APK package.
 * Falls back to a generic file icon if the APK cannot be parsed.
 *
 * Uses the App's shared ImageLoader via rememberAsyncImagePainter for consistency.
 */
@Composable
fun ApkThumbnailCompose(
    apkPath: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val icon = remember(apkPath) {
        try {
            val pkgInfo = pm.getPackageArchiveInfo(apkPath, 0)
            pkgInfo?.applicationInfo?.apply {
                sourceDir = apkPath
                publicSourceDir = apkPath
            }
            pkgInfo?.applicationInfo?.loadIcon(pm)
        } catch (_: Exception) {
            null
        }
    }

    if (icon != null) {
        Image(
            bitmap = icon.toBitmap().asImageBitmap(),
            contentDescription = "APK Icon",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
    } else {
        // Fallback to generic file icon
        Image(
            painter = painterResource(id = R.drawable.ic_file),
            contentDescription = "APK fallback",
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.None,
        )
    }
}