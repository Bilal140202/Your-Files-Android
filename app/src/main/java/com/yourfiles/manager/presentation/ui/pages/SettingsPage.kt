package com.yourfiles.manager.presentation.ui.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yourfiles.manager.R
import com.yourfiles.manager.app.AppThemeManager
import com.yourfiles.manager.app.DarkModeSetting
import com.yourfiles.manager.presentation.ui.components.BackNavigationIconCompose

private const val PRIVACY_POLICY_URL = "https://bilal140202.github.io/Your-Files-Android/privacy-policy.html"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage() {
    val context = LocalContext.current

    // ── Dark mode (Day / Night) ───────────────────────────────────────────
    val isDark by remember { DarkModeSetting.isDarkMode }
    val darkModeSubtitle = if (isDark)
        stringResource(R.string.settings_dark_mode_subtitle_night)
    else
        stringResource(R.string.settings_dark_mode_subtitle_day)

    // ── Theme picker ──────────────────────────────────────────────────────
    val currentThemeIndex by remember { AppThemeManager.currentThemeIndex }
    val themes = remember { AppThemeManager.themes }
    var showThemePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                navigationIcon = { BackNavigationIconCompose() },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            // ── Appearance Section ───────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_appearance))

            // Day / Night toggle
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_dark_mode),
                    subtitle = darkModeSubtitle,
                    checked = isDark,
                    onCheckedChange = { DarkModeSetting.toggle(context) },
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Color theme selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { showThemePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_app_theme),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = themes[currentThemeIndex].name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    // Preview swatch
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape,
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .padding(2.dp)
                                .background(themes[currentThemeIndex].previewColor),
                        )
                    }
                }
            }

            // ── Theme Picker Dialog ───────────────────────────────────
            if (showThemePicker) {
                AlertDialog(
                    onDismissRequest = { showThemePicker = false },
                    title = { Text(stringResource(R.string.settings_app_theme)) },
                    text = {
                        Column {
                            themes.forEachIndexed { index, theme ->
                                val isSelected = index == currentThemeIndex
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            AppThemeManager.selectTheme(context, index)
                                            showThemePicker = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(theme.previewColor),
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = theme.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = theme.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.VerifiedUser,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showThemePicker = false }) {
                            Text("Close")
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── About Section ─────────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_about))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_your_apps_suite),
                    description = stringResource(R.string.settings_about_text),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Privacy Section ───────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_privacy))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.settings_privacy_policy),
                    description = stringResource(R.string.settings_privacy_text),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Version Section ───────────────────────────────────────
            SettingsSectionHeader(title = stringResource(R.string.settings_version))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                SettingsInfoRow(
                    icon = Icons.Outlined.VerifiedUser,
                    title = stringResource(R.string.app_name),
                    description = stringResource(R.string.settings_version_info),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────────
// Private helper composables
// ────────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            modifier = Modifier.padding(end = 16.dp),
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