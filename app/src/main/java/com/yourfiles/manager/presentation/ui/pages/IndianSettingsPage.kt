package com.yourfiles.manager.presentation.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yourfiles.manager.app.IndianRoutes

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

/** Saffron – top stripe of the Indian flag. */
private val Saffron = Color(0xFFFF9933)

/** Indian green – bottom stripe of the Indian flag. */
private val IndianGreen = Color(0xFF138808)

/** Preference file name for the Indian edition settings. */
private const val PREFS_NAME = "yourfiles_indian_settings"

private const val KEY_CONFIRM_BEFORE_DELETE = "confirm_before_delete"
private const val KEY_SHOW_HIDDEN_FILES = "show_hidden_files"

// ---------------------------------------------------------------------------
// Main settings screen
// ---------------------------------------------------------------------------

/**
 * Settings / "Me" screen for the Indian edition of Your Files.
 *
 * Provides sections for Storage, Preferences, About (with a "Made in India"
 * badge), and Support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndianSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    // ── Persisted toggle states ────────────────────────────────────────────
    var confirmBeforeDelete by remember {
        mutableStateOf(prefs.getBoolean(KEY_CONFIRM_BEFORE_DELETE, true))
    }
    var showHiddenFiles by remember {
        mutableStateOf(prefs.getBoolean(KEY_SHOW_HIDDEN_FILES, false))
    }
    val systemDarkMode = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Me") },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ════════════════════════════════════════════════════════════════
            // 1. STORAGE
            // ════════════════════════════════════════════════════════════════
            SettingsSectionHeader(title = "Storage")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.SdStorage,
                    title = "Storage Used",
                    description = "View storage breakdown",
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsNavRow(
                    icon = Icons.Outlined.DeleteOutline,
                    title = "Trash",
                    onClick = { /* Navigate to trash page */ },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.Shield,
                    title = "Clear Cache",
                    description = "Remove app cache data",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════════════════
            // 2. PREFERENCES
            // ════════════════════════════════════════════════════════════════
            SettingsSectionHeader(title = "Preferences")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Visibility,
                    title = "Confirm before delete",
                    subtitle = "Show a dialog before deleting files",
                    checked = confirmBeforeDelete,
                    onCheckedChange = { checked ->
                        confirmBeforeDelete = checked
                        prefs.edit().putBoolean(KEY_CONFIRM_BEFORE_DELETE, checked).apply()
                    },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Visibility,
                    title = "Show hidden files",
                    subtitle = "Display files and folders starting with a dot",
                    checked = showHiddenFiles,
                    onCheckedChange = { checked ->
                        showHiddenFiles = checked
                        prefs.edit().putBoolean(KEY_SHOW_HIDDEN_FILES, checked).apply()
                    },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Dark Mode",
                    subtitle = when {
                        systemDarkMode -> "Currently active (following system)"
                        else -> "Currently inactive (following system)"
                    },
                    checked = systemDarkMode,
                    onCheckedChange = { /* Follows system – read only */ },
                    enabled = false,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════════════════
            // 3. ABOUT
            // ════════════════════════════════════════════════════════════════
            SettingsSectionHeader(title = "About")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    title = "Your Files v2.0",
                    description = "Indian Edition",
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Made in India badge card ───────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Saffron,
                                    Color.White,
                                    IndianGreen,
                                ),
                                startY = 0f,
                                endY = Float.MAX_VALUE,
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "\uD83C\uDDEE\uD83C\uDDF3 Made in India",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Built with pride for India",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF444444),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.Shield,
                    title = "Open Source \u00B7 GPL-3.0",
                    description = "Part of Your Apps Suite",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ════════════════════════════════════════════════════════════════
            // 4. SUPPORT
            // ════════════════════════════════════════════════════════════════
            SettingsSectionHeader(title = "Support")

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsNavRow(
                    icon = Icons.Outlined.Star,
                    title = "Rate on Play Store",
                    onClick = { /* Open Play Store */ },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsNavRow(
                    icon = Icons.Outlined.Share,
                    title = "Share App",
                    onClick = { /* Share intent */ },
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsNavRow(
                    icon = Icons.Outlined.Policy,
                    title = "Privacy Policy",
                    onClick = { /* Open privacy policy */ },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable helper composables
// ---------------------------------------------------------------------------

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
    )
}

/**
 * A row with an icon, title, subtitle, and a toggle switch.
 */
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

/**
 * A row with an icon, title, and description (no action widget).
 */
@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A row with an icon, title, and a chevron – tappable to navigate or trigger an action.
 */
@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 16.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}
