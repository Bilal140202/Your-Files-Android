package com.yourfiles.manager.presentation.ui.components.common.thumbnail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourfiles.manager.app.uim3.theme.AppColors

@Composable
fun OtherFileThumbnailCompose(
    modifier: Modifier = Modifier,
    filePath: String? = null
) {

    val extension = remember(filePath) {
        filePath?.substringAfterLast('.', "") ?: ""
    }
    if (extension.isNotBlank()) {
        FileTypeIcon(extension = extension, modifier = modifier)
    } else {
        // Fallback for when we don't have an extension
        Icon(
            imageVector = Icons.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
@Composable
fun FileTypeIcon(extension: String, modifier: Modifier = Modifier) {
    val backgroundColor = fileTypeColor(extension)
    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = extension.uppercase(),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

fun fileTypeColor(extension: String): Color {
    return when (extension.lowercase()) {
        "pdf" -> AppColors.Error
        "doc", "docx" -> AppColors.CategoryDocuments
        "xls", "xlsx" -> AppColors.CategoryApk
        "ppt", "pptx" -> AppColors.Warning
        "mp3", "wav" -> AppColors.CategoryVideos
        "mp4", "mkv" -> AppColors.Info
        "apk" -> AppColors.CategoryApk
        "zip", "rar" -> AppColors.CategoryArchives
        else -> AppColors.CategoryOther
    }
}