package com.yourfiles.manager.presentation.ui.pages

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yourfiles.manager.R
import com.yourfiles.manager.app.Routes
import java.io.File

private const val PREFS_NAME = "yourfiles_settings"
private const val KEY_CONFIRM_BEFORE_DELETE = "confirm_before_delete"
private const val KEY_SHOW_FILE_EXTENSIONS = "show_file_extensions"
private const val KEY_DARK_MODE = "dark_mode"

/**
 * Settings screen for Your Files v2.0 — modern Material You design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // ── Persisted states ─────────────────────────────────────────────────────
    var confirmBeforeDelete by remember {
        mutableStateOf(prefs.getBoolean(KEY_CONFIRM_BEFORE_DELETE, true))
    }
    var showFileExtensions by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_FILE_EXTENSIONS, false))
    }
    val systemDark = isSystemInDarkTheme()
    var darkModeEnabled by remember {
        mutableStateOf(
            prefs.getBoolean(KEY_DARK_MODE, systemDark)
        )
    }

    // ── Storage helpers ───────────────────────────────────────────────────────
    val cacheDir = context.cacheDir
    val cacheSize = remember { calculateDirSize(cacheDir) }
    val trashDir = remember { File(context.filesDir, "trash") }
    val trashSize = remember { calculateDirSize(trashDir) }

    // ── Dialog states ────────────────────────────────────────────────────────
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ══════════════════════════════════════════════════════════════════
            // 1. BEHAVIOR
            // ══════════════════════════════════════════════════════════════════
            SectionHeader(title = "BEHAVIOR")

            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Confirm before delete",
                    subtitle = "Show a confirmation dialog before deleting files",
                    checked = confirmBeforeDelete,
                    onCheckedChange = { checked ->
                        confirmBeforeDelete = checked
                        prefs.edit().putBoolean(KEY_CONFIRM_BEFORE_DELETE, checked).apply()
                    },
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.Extension,
                    title = "Show file extensions",
                    subtitle = "Display .jpg, .pdf, etc. in file names",
                    checked = showFileExtensions,
                    onCheckedChange = { checked ->
                        showFileExtensions = checked
                        prefs.edit().putBoolean(KEY_SHOW_FILE_EXTENSIONS, checked).apply()
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════════════════════════════════
            // 2. APPEARANCE
            // ══════════════════════════════════════════════════════════════════
            SectionHeader(title = "APPEARANCE")

            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = if (darkModeEnabled) "On" else "Off — follows system default",
                    checked = darkModeEnabled,
                    onCheckedChange = { checked ->
                        darkModeEnabled = checked
                        prefs.edit().putBoolean(KEY_DARK_MODE, checked).apply()
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════════════════════════════════
            // 3. STORAGE
            // ══════════════════════════════════════════════════════════════════
            SectionHeader(title = "STORAGE")

            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Outlined.CleaningServices,
                    title = "Clear cache",
                    description = formatSize(cacheSize),
                    onClick = { showClearCacheDialog = true },
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Outlined.DeleteSweep,
                    title = "Empty trash",
                    description = formatSize(trashSize),
                    onClick = { showEmptyTrashDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════════════════════════════════
            // 4. ABOUT
            // ══════════════════════════════════════════════════════════════════
            SectionHeader(title = "ABOUT")

            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    title = "Your Files",
                    description = "v1.0.0",
                    showChevron = false,
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Outlined.Favorite,
                    title = "Part of Your Apps suite",
                    description = "YourNotes · Your Gallery · Your Files",
                    showChevron = false,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════════════════════════════════
            // 5. LEGAL
            // ══════════════════════════════════════════════════════════════════
            SectionHeader(title = "LEGAL")

            SettingsCard {
                SettingsInfoRow(
                    icon = Icons.Outlined.Shield,
                    title = "Privacy Policy",
                    description = "Data handling & permissions",
                    onClick = { navController.navigate(Routes.PRIVACY_POLICY) },
                )
                SettingsDivider()
                SettingsInfoRow(
                    icon = Icons.Outlined.Description,
                    title = "Licenses",
                    description = "GNU General Public License v3.0",
                    onClick = { showLicensesDialog = true },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ══════════════════════════════════════════════════════════════════
            // 6. FOOTER
            // ══════════════════════════════════════════════════════════════════
            Text(
                text = "Made with \u2764 in India",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
            )
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear cache?") },
            text = { Text("This will free up ${formatSize(cacheSize)} of cached data.") },
            confirmButton = {
                TextButton(onClick = {
                    clearDir(cacheDir)
                    showClearCacheDialog = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("Empty trash?") },
            text = { Text("This will permanently delete ${formatSize(trashSize)} of trashed files.") },
            confirmButton = {
                TextButton(onClick = {
                    clearDir(trashDir)
                    showEmptyTrashDialog = false
                }) { Text("Empty") }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) { Text("Cancel") }
            },
        )
    }

    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Licenses") },
            text = {
                Text(
                    text = "Your Files is free and open-source software released under the " +
                        "GNU General Public License v3.0 (GPL-3.0).\n\n" +
                        "You are free to use, modify, and distribute this software under " +
                        "the terms of the license.\n\n" +
                        "Source code: https://github.com/yourfiles",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) { Text("OK") }
            },
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Private helper composables
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp, top = 4.dp),
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Utility helpers
// ────────────────────────────────────────────────────────────────────────────────

private fun calculateDirSize(dir: File): Long {
    if (!dir.exists()) return 0L
    return dir.walkTopDown()
        .filter { it.isFile }
        .sumOf { it.length() }
}

private fun clearDir(dir: File) {
    if (!dir.exists()) return
    dir.walkTopDown()
        .filter { it.isFile }
        .forEach { it.delete() }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    while (size >= 1024 && unitIndex < units.lastIndex) {
        size /= 1024.0
        unitIndex++
    }
    return String.format("%.1f %s", size, units[unitIndex])
}
