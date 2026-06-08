package com.yourfiles.manager.presentation.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yourfiles.manager.BuildConfig
import com.yourfiles.manager.R
import java.io.File

@Composable
fun ImageViewer(imagePath: String) {
    val context = LocalContext.current
    val file = File(imagePath)
    val imageUri = rememberSafeUri(context, file)
    val errorPainter: Painter = painterResource(id = R.drawable.ic_file)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            imageLoader = com.yourfiles.manager.app.App.instance.imageLoader,
            placeholder = errorPainter,
            error = errorPainter,
        )
    }
}

/**
 * Safely create a FileProvider URI, falling back to file path string if FileProvider fails.
 */
@Composable
private fun rememberSafeUri(context: android.content.Context, file: File): Any {
    return remember(file.absolutePath) {
        try {
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.provider",
                    file,
                )
            } else {
                file.absolutePath
            }
        } catch (e: Exception) {
            file.absolutePath
        }
    }
}
