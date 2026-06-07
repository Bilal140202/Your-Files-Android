package com.yourfiles.manager.presentation.ui.pages

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourfiles.manager.app.IndianRoutes
import java.io.File

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

/** Saffron colour used throughout the Indian edition for accent / indicators. */
private val Saffron = Color(0xFFFF9933)

/** Indian green used for secondary accents. */
private val IndianGreen = Color(0xFF138808)

// ---------------------------------------------------------------------------
// Junk-file category model
// ---------------------------------------------------------------------------

private data class JunkCategory(
    val label: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color,
    val paths: List<String>,
    val isCountBased: Boolean = false,
)

private val JUNK_CATEGORIES = listOf(
    JunkCategory(
        label = "Cache",
        description = "App cache & temporary files",
        icon = Icons.Outlined.FolderSpecial,
        iconTint = Saffron,
        paths = listOf(
            "/storage/emulated/0/Android/data",
        ),
    ),
    JunkCategory(
        label = "Thumbnails",
        description = "Image & video thumbnail caches",
        icon = Icons.Outlined.Image,
        iconTint = Color(0xFF7B61FF),
        paths = listOf(
            "/storage/emulated/0/Pictures/.thumbnails",
            "/storage/emulated/0/DCIM/.thumbnails",
            "/storage/emulated/0/DCIM/.Thumbnails",
        ),
    ),
    JunkCategory(
        label = "WhatsApp Status",
        description = "Viewed status media",
        icon = Icons.Outlined.BrokenImage,
        iconTint = Color(0xFF25D366),
        paths = listOf(
            "/storage/emulated/0/WhatsApp/Media/.Statuses",
        ),
    ),
    JunkCategory(
        label = "APK Installers",
        description = "Downloaded APK files",
        icon = Icons.Outlined.Android,
        iconTint = Color(0xFF3DDC84),
        paths = listOf(
            "/storage/emulated/0/Download",
        ),
    ),
    JunkCategory(
        label = "Empty Folders",
        description = "Folders with no contents",
        icon = Icons.Outlined.FolderOff,
        iconTint = Color(0xFF90A4AE),
        paths = emptyList(),
        isCountBased = true,
    ),
)

// ---------------------------------------------------------------------------
// Main screen composable
// ---------------------------------------------------------------------------

/**
 * Safai (Cleaning) home screen for the Indian edition.
 *
 * Presents three tabs – **Duplicates**, **Junk Files**, and **Large Files** –
 * allowing users to quickly scan and clean storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafaiHomeScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Duplicates", "Junk Files", "Large Files")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Safai",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Clean your storage smartly",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Tab row ────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Saffron,
                    height = 3.dp,
                )
            },
            divider = {},
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Tab content ───────────────────────────────────────────────────
        when (selectedTab) {
            0 -> DuplicatesTabContent(
                onNavigateToDuplicates = {
                    navController.navigate(IndianRoutes.SAFAI_DUPLICATES)
                },
            )
            1 -> JunkTabContent()
            2 -> LargeFilesTabContent()
        }
    }
}

// ---------------------------------------------------------------------------
// Tab 1 – Duplicates
// ---------------------------------------------------------------------------

@Composable
private fun DuplicatesTabContent(onNavigateToDuplicates: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Decorative icon
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.BrokenImage,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Find Duplicate Files",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scan your storage for duplicate photos, videos, and documents.\n"
                        + "Free up space by removing redundant copies.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToDuplicates,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text(
                        text = "Scan for Duplicates",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Tab 2 – Junk Files
// ---------------------------------------------------------------------------

@Composable
private fun JunkTabContent() {
    var categorySizes by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var emptyFolderCount by remember { mutableStateOf(0) }
    var isScanning by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }
    var isCleaning by remember { mutableStateOf(false) }

    // Scan for junk when tab first loads
    LaunchedEffect(Unit) {
        isScanning = true
        val sizes = mutableMapOf<String, Long>()
        var emptyCount = 0

        for (category in JUNK_CATEGORIES) {
            if (category.isCountBased) {
                // Count empty folders in common locations
                val scanPaths = listOf(
                    "/storage/emulated/0/Download",
                    "/storage/emulated/0/Pictures",
                    "/storage/emulated/0/DCIM",
                    "/storage/emulated/0/Documents",
                )
                for (path in scanPaths) {
                    emptyCount += countEmptyFolders(File(path))
                }
            } else {
                var totalSize = 0L
                for (path in category.paths) {
                    val dir = File(path)
                    if (dir.exists()) {
                        if (category.label == "APK Installers") {
                            // Only count APK files in Download
                            dir.listFiles()?.filter { it.isFile && it.extension.equals("apk", ignoreCase = true) }
                                ?.sumOf { it.length() }
                                ?.let { totalSize += it }
                        } else {
                            totalSize += dir.walkTopDown()
                                .filter { it.isFile }
                                .sumOf { it.length() }
                        }
                    }
                }
                sizes[category.label] = totalSize
            }
        }

        categorySizes = sizes
        emptyFolderCount = emptyCount
        isScanning = false
        hasScanned = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        if (isScanning) {
            // ── Scanning state ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LinearProgressIndicator(
                    color = Saffron,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(0.5f).clip(RoundedCornerShape(4.dp)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Scanning for junk files\u2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // ── Category cards ────────────────────────────────────────────
            val totalJunkSize = categorySizes.values.sum()
            if (totalJunkSize > 0 || emptyFolderCount > 0) {
                // Summary header
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Saffron.copy(alpha = 0.1f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SdStorage,
                            contentDescription = null,
                            tint = Saffron,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${formatFileSize(totalJunkSize)} can be freed",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Saffron,
                            )
                            if (emptyFolderCount > 0) {
                                Text(
                                    text = "+ $emptyFolderCount empty folder${if (emptyFolderCount != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            for (category in JUNK_CATEGORIES) {
                JunkCategoryCard(
                    category = category,
                    size = categorySizes[category.label] ?: 0L,
                    count = if (category.isCountBased) emptyFolderCount else null,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ── Clean All button ─────────────────────────────────────────
            if (hasScanned && (totalJunkSize > 0 || emptyFolderCount > 0)) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { isCleaning = true },
                    enabled = !isCleaning,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isCleaning) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCleaning) "Cleaning\u2026" else "Clean All Junk",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (hasScanned && totalJunkSize == 0L && emptyFolderCount == 0) {
                // ── All clean state ─────────────────────────────────────────
                Spacer(modifier = Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = null,
                        tint = IndianGreen,
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "All clean! \uD83C\uDFE5",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No junk files found on your device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun JunkCategoryCard(
    category: JunkCategory,
    size: Long,
    count: Int? = null,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(category.iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Label & description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Size or count
            Text(
                text = if (count != null) {
                    "$count folder${if (count != 1) "s" else ""}"
                } else {
                    formatFileSize(size)
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (size > 0 || (count ?: 0) > 0) Saffron
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tab 3 – Large Files
// ---------------------------------------------------------------------------

@Composable
private fun LargeFilesTabContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SdStorage,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Find Large Files",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Discover the biggest files hogging your storage.\n"
                        + "Review and remove the ones you no longer need.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* Navigate to large files screen */ },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text(
                        text = "Scan for Large Files",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Utilities
// ---------------------------------------------------------------------------

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var value = bytes
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return "$value ${units[unitIndex]}"
}

private fun countEmptyFolders(dir: File): Int {
    if (!dir.exists() || !dir.isDirectory) return 0
    var count = 0
    dir.listFiles()?.forEach { child ->
        if (child.isDirectory) {
            if (isEmptyDirectory(child)) count++
            count += countEmptyFolders(child)
        }
    }
    return count
}

private fun isEmptyDirectory(dir: File): Boolean {
    val children = dir.listFiles() ?: return true
    return children.isEmpty()
}
