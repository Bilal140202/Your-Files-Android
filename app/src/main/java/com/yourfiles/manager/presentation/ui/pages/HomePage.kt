package com.yourfiles.manager.presentation.ui.pages

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourfiles.manager.app.Routes
import com.yourfiles.manager.presentation.WorkerUIState
import com.yourfiles.manager.presentation.vm.HomeVM
import com.yourfiles.manager.presentation.vm.StorageUiState
import com.yourfiles.manager.utils.StorageInfo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.text.format.Formatter
import androidx.compose.foundation.Canvas

// ─────────────────────────────────────────────────────────────────────────────
// Root composable — same signature as before so Router.kt needs no changes
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeComposable(vm: HomeVM = viewModel()) {
    val context = LocalContext.current
    val navController = remember {
        (context.applicationContext as com.yourfiles.manager.app.App).navController()
    }
    val storageState by vm.uiState.collectAsState()
    val scanStatus by vm.scanUIStatus.collectAsState()
    val totalSavedBytes by vm.totalSavedBytes.collectAsState()

    val duplicatesCount by vm.duplicatesCount.collectAsState()
    val largeFilesCount by vm.largeFilesCount.collectAsState()
    val screenshotsCount by vm.screenshotsCount.collectAsState()
    val duplicateSizeBytes by vm.duplicateSizeBytes.collectAsState()
    val largeSizeBytes by vm.largeSizeBytes.collectAsState()
    val screenshotsSizeBytes by vm.screenshotsSizeBytes.collectAsState()

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val dateString = dateFormatter.format(java.util.Date())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp)
        ) {
            when (scanStatus) {
                is WorkerUIState.InProgress -> {
                    // Scanning overlay
                    val progressState = scanStatus as WorkerUIState.InProgress
                    ScanningOverlay(
                        progress = progressState.progress,
                        message = progressState.message,
                        duplicatesCount = duplicatesCount,
                        largeFilesCount = largeFilesCount,
                        screenshotsCount = screenshotsCount,
                        storageState = storageState,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Main dashboard
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // ── Greeting section ─────────────────────────────────
                        Spacer(modifier = Modifier.height(16.dp))

                        // Saved memory badge top-right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = greeting,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (totalSavedBytes > 0L) {
                                SavedMemoryPill(savedBytes = totalSavedBytes)
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ── Storage Ring ─────────────────────────────────────
                        when (storageState) {
                            is StorageUiState.Success -> {
                                val info = (storageState as StorageUiState.Success).info
                                StorageRingComposable(
                                    storageInfo = info,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                )
                            }
                            is StorageUiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                            is StorageUiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.ErrorOutline,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Could not read storage info",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── 2x2 Tool Cards Grid ────────────────────────────
                        val isScanComplete = scanStatus is WorkerUIState.Success
                            || scanStatus is WorkerUIState.Failed
                            || scanStatus is WorkerUIState.Cancelled

                        Text(
                            text = "Cleaning Tools",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ToolCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.ContentCopy,
                                    label = "Duplicates",
                                    iconBackgroundColor = Color(0xFF6750A4),
                                    count = if (isScanComplete) duplicatesCount else 0,
                                    sizeBytes = if (isScanComplete) duplicateSizeBytes else 0L,
                                    onClick = {
                                        if (isScanComplete) {
                                            navController.navigate(Routes.FLAT_DUPLICATES_FILE_MANAGER)
                                        }
                                    },
                                    enabled = isScanComplete
                                )
                                ToolCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.FolderSpecial,
                                    label = "Large Files",
                                    iconBackgroundColor = Color(0xFF00897B),
                                    count = if (isScanComplete) largeFilesCount else 0,
                                    sizeBytes = if (isScanComplete) largeSizeBytes else 0L,
                                    onClick = {
                                        if (isScanComplete) {
                                            navController.navigate(Routes.FLAT_LARGE_FILE_MANAGER)
                                        }
                                    },
                                    enabled = isScanComplete
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ToolCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.PhotoLibrary,
                                    label = "Screenshots",
                                    iconBackgroundColor = Color(0xFFFF8F00),
                                    count = if (isScanComplete) screenshotsCount else 0,
                                    sizeBytes = if (isScanComplete) screenshotsSizeBytes else 0L,
                                    onClick = {
                                        if (isScanComplete) {
                                            navController.navigate(Routes.FLAT_SCREENSHOTS_FILE_MANAGER)
                                        }
                                    },
                                    enabled = isScanComplete
                                )
                                ToolCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Outlined.Chat,
                                    label = "WhatsApp",
                                    iconBackgroundColor = Color(0xFF25D366),
                                    count = null,
                                    sizeBytes = null,
                                    subtitle = "Clean media",
                                    onClick = {
                                        if (isScanComplete) {
                                            navController.navigate(Routes.FLAT_WHATSAPP_FILE_MANAGER)
                                        }
                                    },
                                    enabled = isScanComplete
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // ── Bottom: Scan button or status ───────────────────
                        when (scanStatus) {
                            is WorkerUIState.Initial -> {
                                ScanButton(onClick = { vm.restartScan() })
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            is WorkerUIState.Failed,
                            is WorkerUIState.Cancelled -> {
                                ScanButton(label = "Scan Again", onClick = { vm.restartScan() })
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            is WorkerUIState.Success -> {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated circular storage ring
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StorageRingComposable(
    storageInfo: StorageInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val usedFraction = if (storageInfo.totalSpaceGB > 0f) {
        (storageInfo.usedSpaceGB / storageInfo.totalSpaceGB).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedFraction by animateFloatAsState(
        targetValue = usedFraction,
        animationSpec = tween(durationMillis = 1200, easing = { t ->
            // ease-out cubic
            1f - (1f - t) * (1f - t) * (1f - t)
        }),
        label = "storageRing"
    )

    val primaryColor = Color(0xFF6750A4)
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val ringThickness = 12.dp
    val gapAngle = 16f // degrees gap at the top

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.Center)
        ) {
            val diameter = size.minDimension
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)
            val thicknessPx = ringThickness.toPx()
            val startAngle = -90f + gapAngle / 2f
            val sweepBackground = 360f - gapAngle
            val sweepForeground = animatedFraction * sweepBackground

            // Background arc (surfaceVariant)
            drawArc(
                brush = Brush.linearGradient(
                    colors = listOf(
                        surfaceVariantColor.copy(alpha = 0.6f),
                        surfaceVariantColor.copy(alpha = 0.3f)
                    ),
                    start = topLeft,
                    end = Offset(topLeft.x + diameter, topLeft.y + diameter)
                ),
                startAngle = startAngle,
                sweepAngle = sweepBackground,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = thicknessPx, cap = StrokeCap.Round)
            )

            // Foreground arc (primary, animated)
            if (animatedFraction > 0.001f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.9f),
                            primaryColor
                        ),
                        start = topLeft,
                        end = Offset(topLeft.x + diameter, topLeft.y + diameter)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepForeground,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = thicknessPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "${storageInfo.freeSpaceGB.formatToOneDecimal()} GB",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "free of ${storageInfo.totalSpaceGB.formatToOneDecimal()} GB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${Formatter.formatFileSize(context, (storageInfo.usedSpaceGB * 1024 * 1024 * 1024).toLong())} used",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tool card in the 2x2 grid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    iconBackgroundColor: Color,
    count: Int?,
    sizeBytes: Long?,
    subtitle: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val alpha = if (enabled) 1f else 0.5f

    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Icon at top-left
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = iconBackgroundColor.copy(alpha = 0.15f * alpha)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp),
                    tint = iconBackgroundColor.copy(alpha = alpha)
                )
            }

            // Bottom-left: label + count or subtitle
            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f)
                    )
                } else if (count != null) {
                    Text(
                        text = "$count items",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha * 0.7f)
                    )
                }
            }

            // Bottom-right: size
            if (sizeBytes != null && sizeBytes > 0L) {
                Text(
                    text = Formatter.formatFileSize(context, sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = iconBackgroundColor.copy(alpha = alpha * 0.85f),
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pill-shaped scan button
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScanButton(
    label: String = "Scan Storage",
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Saved memory pill badge
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SavedMemoryPill(savedBytes: Long) {
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${Formatter.formatFileSize(context, savedBytes)} freed",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scanning overlay with progress bar + live stats
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ScanningOverlay(
    progress: Int,
    message: String,
    duplicatesCount: Int,
    largeFilesCount: Int,
    screenshotsCount: Int,
    storageState: StorageUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing circle indicator
        val infiniteTransition = rememberInfiniteTransition(label = "scanPulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp * pulseScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {}
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$progress%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // "Found so far" stats
        Text(
            text = "Found so far",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ScanStatItem(
                icon = Icons.Outlined.ContentCopy,
                label = "Duplicates",
                count = duplicatesCount,
                color = Color(0xFF6750A4)
            )
            ScanStatItem(
                icon = Icons.Outlined.FolderSpecial,
                label = "Large",
                count = largeFilesCount,
                color = Color(0xFF00897B)
            )
            ScanStatItem(
                icon = Icons.Outlined.PhotoLibrary,
                label = "Screenshots",
                count = screenshotsCount,
                color = Color(0xFFFF8F00)
            )
        }
    }
}

@Composable
private fun ScanStatItem(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun Float.formatToOneDecimal(): String {
    return if (this >= 10f) {
        "%.0f".format(this)
    } else {
        "%.1f".format(this)
    }
}
