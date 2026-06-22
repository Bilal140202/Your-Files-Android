package com.yourfiles.manager.presentation.ui.pages

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose
import java.io.File

/**
 * APK info viewer — shows package name, version, icon, and Install button.
 * Reads APK metadata via PackageManager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkInfoScreen(
    filePath: String,
) {
    val context = LocalContext.current
    val file = File(filePath)

    var packageName by remember(filePath) { mutableStateOf<String?>(null) }
    var versionName by remember(filePath) { mutableStateOf<String?>(null) }
    var versionCode by remember(filePath) { mutableStateOf<String?>(null) }
    var icon by remember(filePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var minSdk by remember(filePath) { mutableStateOf<String?>(null) }
    var targetSdk by remember(filePath) { mutableStateOf<String?>(null) }
    var error by remember(filePath) { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageArchiveInfo(
                    filePath,
                    PackageManager.PackageInfoFlags.of(
                        PackageManager.GET_ACTIVITIES.toLong() or
                        PackageManager.GET_PERMISSIONS.toLong() or
                        PackageManager.GET_SHARED_LIBRARY_FILES.toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageArchiveInfo(
                    filePath,
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_SHARED_LIBRARY_FILES
                )
            }

            if (packageInfo != null) {
                packageInfo.applicationInfo?.sourceDir = filePath
                packageInfo.applicationInfo?.publicSourceDir = filePath
                packageName = packageInfo.packageName
                versionName = packageInfo.versionName
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toString()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toString()
                }
                minSdk = "API ${packageInfo.applicationInfo?.minSdkVersion ?: "?"}"
                targetSdk = "API ${packageInfo.applicationInfo?.targetSdkVersion ?: "?"}"
                icon = try {
                    val drawable = packageInfo.applicationInfo?.loadIcon(context.packageManager)
                    (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                } catch (_: Exception) { null }
            } else {
                error = "Could not read APK info"
            }
        } catch (e: Exception) {
            error = e.message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file.name, fontSize = 14.sp) },
                navigationIcon = { BackNavigationIconCompose() },
            )
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // App icon
        if (icon != null) {
            Image(
                bitmap = icon!!.asImageBitmap(),
                contentDescription = "App icon",
                modifier = Modifier.size(64.dp),
            )
        } else {
            Text(
                text = "📦",
                fontSize = 64.sp,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Package name
        Text(
            text = packageName ?: "Unknown package",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        // File name
        Text(
            text = file.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Info rows
        InfoRow("Version", "${versionName ?: "?"} (${versionCode ?: "?"})")
        InfoRow("Min SDK", minSdk ?: "?")
        InfoRow("Target SDK", targetSdk ?: "?")
        InfoRow("Size", Formatter.formatShortFileSize(context, file.length()))

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Install button
        Button(
            onClick = {
                try {
                    val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", file
                        )
                    } else {
                        Uri.fromFile(file)
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) { }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
        ) {
            Text("Install")
        }

        // Open with button
        OutlinedButton(
            onClick = {
                try {
                    val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        androidx.core.content.FileProvider.getUriForFile(
                            context, "${context.packageName}.provider", file
                        )
                    } else {
                        Uri.fromFile(file)
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                } catch (_: Exception) { }
            },
            modifier = Modifier.fillMaxWidth(0.7f),
        ) {
            Text("Open with...")
        }
    }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}
