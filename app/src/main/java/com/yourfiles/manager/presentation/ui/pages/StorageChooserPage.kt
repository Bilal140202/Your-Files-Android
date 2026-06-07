package com.yourfiles.manager.presentation.ui.pages

import android.os.StatFs
import android.text.format.Formatter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.yourfiles.manager.presentation.vm.HomeVM
import com.yourfiles.manager.presentation.vm.StorageUiState
import com.yourfiles.manager.utils.StorageHelper
import com.yourfiles.manager.utils.StorageInfo

// ─── Indian Tricolor Constants ──────────────────────────────────────────────

private val Saffron = Color(0xFFFF9933)
private val IndiaGreen = Color(0xFF138808)
private val IndianWhite = Color(0xFFFFFFFF)
private val PhoneStoragePath = "/storage/emulated/0"

/**
 * Root composable for the Storage Chooser screen.
 * Replaces the old HomePage (cleaner/scanner) — the app now opens here so
 * users can pick Phone Storage or SD Card before browsing files.
 */
@Composable
fun StorageChooserScreen(
    navController: NavController,
    viewModel: HomeVM = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val storageHelper = remember { StorageHelper() }

    // Phone storage info comes from the VM (already fetched on init)
    var phoneInfo by remember { mutableStateOf<StorageInfo?>(null) }
    var phoneLoading by remember { mutableStateOf(true) }

    // SD card detection & stats
    var sdCardPath by remember { mutableStateOf<String?>(null) }
    var sdInfo by remember { mutableStateOf<StorageInfo?>(null) }
    var sdLoading by remember { mutableStateOf(true) }

    // Detect storage volumes once
    LaunchedEffect(Unit) {
        val paths = storageHelper.getStoragePaths(context)
        // First path is always primary (phone), second (if exists) is SD card
        if (paths.isNotEmpty()) {
            phoneInfo = getStorageInfoForPath(paths.first())
            phoneLoading = false
        }
        // Look for an SD card (non-primary removable volume)
        val sdPath = paths.firstOrNull { it != paths.first() }
        sdCardPath = sdPath
        if (sdPath != null) {
            sdInfo = getStorageInfoForPath(sdPath)
        }
        sdLoading = false
    }

    // Keep phone info in sync with VM state when it loads
    LaunchedEffect(uiState) {
        when (uiState) {
            is StorageUiState.Success -> {
                val vmInfo = (uiState as StorageUiState.Success).info
                // Only override if our local fetch hasn't completed yet
                if (phoneInfo == null) {
                    phoneInfo = vmInfo
                    phoneLoading = false
                }
            }
            is StorageUiState.Error -> {
                if (phoneInfo == null) phoneLoading = false
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Title Section ───────────────────────────────────────────────
        Text(
            text = "Your Files",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // "Made in India" badge in saffron
        Card(
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = Saffron.copy(alpha = 0.15f),
            ),
            modifier = Modifier,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(text = "\uD83C\uDDEE\uD83C\uDDE3", fontSize = 14.sp)
                Text(
                    text = "Made in India",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Saffron,
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // ── Storage Cards ───────────────────────────────────────────────

        // Card 1 — Phone Storage
        StorageCard(
            title = "Phone Storage",
            icon = Icons.Outlined.PhoneAndroid,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onContainerColor = MaterialTheme.colorScheme.onPrimaryContainer,
            storageInfo = phoneInfo,
            isLoading = phoneLoading,
            onClick = {
                navController.navigate("file_browser?path=$PhoneStoragePath")
            },
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card 2 — SD Card
        if (sdCardPath != null) {
            StorageCard(
                title = "SD Card",
                icon = Icons.Outlined.SdStorage,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                storageInfo = sdInfo,
                isLoading = sdLoading,
                onClick = {
                    navController.navigate("file_browser?path=$sdCardPath")
                },
            )
        } else if (!sdLoading) {
            // SD card absent — show disabled card
            StorageCardAbsent(
                title = "SD Card",
                icon = Icons.Outlined.SdStorage,
            )
        } else {
            // Still detecting
            StorageCard(
                title = "SD Card",
                icon = Icons.Outlined.SdStorage,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                storageInfo = null,
                isLoading = true,
                onClick = { },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── Bottom hint ────────────────────────────────────────────────
        Text(
            text = "Tap storage to browse files",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── Storage Card (with stats) ─────────────────────────────────────────────

@Composable
private fun StorageCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onContainerColor: Color,
    storageInfo: StorageInfo?,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(20.dp)

    // Animate press feedback
    var pressed by remember { mutableStateOf(false) }
    val animatedColor by animateColorAsState(
        targetValue = if (pressed) containerColor.copy(alpha = 0.85f) else containerColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_color",
    )

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f))
            .clip(shape)
            .clickable {
                pressed = true
                onClick()
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            // Icon + Title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = onContainerColor,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainerColor,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                // Loading indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = onContainerColor.copy(alpha = 0.5f),
                        strokeWidth = 3.dp,
                    )
                }
            } else if (storageInfo != null) {
                // Storage stats
                val usedBytes = (storageInfo.usedSpaceGB * 1024 * 1024 * 1024).toLong()
                val totalBytes = (storageInfo.totalSpaceGB * 1024 * 1024 * 1024).toLong()
                val freeBytes = (storageInfo.freeSpaceGB * 1024 * 1024 * 1024).toLong()

                val usedFormatted = Formatter.formatFileSize(context, usedBytes)
                val totalFormatted = Formatter.formatFileSize(context, totalBytes)
                val freeFormatted = Formatter.formatFileSize(context, freeBytes)

                // "XX GB used of YY GB"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$usedFormatted used",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = onContainerColor,
                    )
                    Text(
                        text = "of $totalFormatted",
                        fontSize = 14.sp,
                        color = onContainerColor.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Free space label
                Text(
                    text = "$freeFormatted free",
                    fontSize = 13.sp,
                    color = onContainerColor.copy(alpha = 0.6f),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tricolor progress bar
                val fraction = if (storageInfo.totalSpaceGB > 0f) {
                    storageInfo.usedSpaceGB / storageInfo.totalSpaceGB
                } else 0f

                TricolorProgressBar(
                    fraction = fraction.coerceIn(0f, 1f),
                )
            } else {
                Text(
                    text = "Unable to read storage info",
                    fontSize = 14.sp,
                    color = onContainerColor.copy(alpha = 0.6f),
                )
            }
        }
    }
}

// ─── Absent SD Card ──────────────────────────────────────────────────────────

@Composable
private fun StorageCardAbsent(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val shape = RoundedCornerShape(20.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "No SD card inserted",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Insert an SD card to see its storage",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Greyed-out empty progress bar
            TricolorProgressBar(fraction = 0f, dimmed = true)
        }
    }
}

// ─── Tricolor Progress Bar ──────────────────────────────────────────────────

/**
 * A custom progress bar styled with Indian tricolor:
 *   Saffron (#FF9933) | White (#FFFFFF) | Green (#138808)
 *
 * The bar fills proportionally based on [fraction] (0..1 = used/total).
 * When [dimmed] is true, the bar is rendered in a muted grey.
 */
@Composable
private fun TricolorProgressBar(
    fraction: Float,
    dimmed: Boolean = false,
) {
    val barHeight = 8.dp
    val cornerRadius = 4.dp
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .clip(shape)
            .background(
                color = if (dimmed) {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = shape,
            ),
    ) {
        if (fraction > 0f) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(barHeight)
                    .clip(shape),
            ) {
                // Saffron section — always present if any space used
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (dimmed) Saffron.copy(alpha = 0.3f) else Saffron,
                        ),
                )

                // White middle section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (dimmed) IndianWhite.copy(alpha = 0.3f) else IndianWhite,
                        ),
                )

                // Green section
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            color = if (dimmed) IndiaGreen.copy(alpha = 0.3f) else IndiaGreen,
                        ),
                )
            }
        }
    }
}

// ─── Utility: Per-path storage info ──────────────────────────────────────────

/**
 * Calculates [StorageInfo] for a single mount path using StatFs.
 * Falls back to zeroes on error.
 */
private fun getStorageInfoForPath(path: String): StorageInfo {
    return try {
        val stat = StatFs(path)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = totalBytes - freeBytes

        val gb = 1024f * 1024 * 1024
        StorageInfo(
            totalSpaceGB = totalBytes / gb,
            usedSpaceGB = usedBytes / gb,
            freeSpaceGB = freeBytes / gb,
        )
    } catch (_: Exception) {
        StorageInfo(totalSpaceGB = 0f, usedSpaceGB = 0f, freeSpaceGB = 0f)
    }
}
