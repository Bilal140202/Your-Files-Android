package com.yourfiles.manager.app

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.yourfiles.manager.app.uim3.theme.ItemLayoutStyle
import com.yourfiles.manager.app.uim3.theme.ThemeColors

/**
 * Manages user-selectable visual themes.
 *
 * Each [AppThemeOption] defines a COMPLETE visual template — chromatic palette,
 * neutral surface overrides, shape roundness, card style, header treatment,
 * AND layout parameters that change the actual UI structure (item height,
 * icon sizes, card style, dividers, spacing, etc.).
 *
 * This is NOT just a color swap — each theme creates a genuinely distinct
 * app experience inspired by real Android file managers.
 */
object AppThemeManager {

    private const val PREFS_NAME = "yourfiles_settings"
    private const val KEY_THEME = "app_theme"

    data class AppThemeOption(
        val name: String,
        val description: String,
        val previewColor: Color,
        val colors: ThemeColors,
    )

    val themes: List<AppThemeOption> = listOf(

        // ═══════════════════════════════════════════════════════════════════
        // 1. ES Explorer Classic
        // Blue header, compact rows, dividers, white surface, small icons
        // Professional file manager look — like classic ES File Explorer
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "ES Explorer",
            description = "Classic compact rows with blue header & dividers",
            previewColor = Color(0xFF1976D2),
            colors = ThemeColors(
                primaryLight = Color(0xFF1976D2), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFBBDEFB), onPrimaryContainerLight = Color(0xFF0D47A1),
                secondaryLight = Color(0xFF26A69A), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFB2DFDB), onSecondaryContainerLight = Color(0xFF004D40),
                tertiaryLight = Color(0xFFFF8F00), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFE082), onTertiaryContainerLight = Color(0xFF5D4037),
                primaryDark = Color(0xFF90CAF9), onPrimaryDark = Color(0xFF0D47A1),
                primaryContainerDark = Color(0xFF1565C0), onPrimaryContainerDark = Color(0xFFBBDEFB),
                secondaryDark = Color(0xFF80CBC4), onSecondaryDark = Color(0xFF004D40),
                secondaryContainerDark = Color(0xFF00897B), onSecondaryContainerDark = Color(0xFFB2DFDB),
                tertiaryDark = Color(0xFFFFC107), onTertiaryDark = Color(0xFF5D4037),
                tertiaryContainerDark = Color(0xFFFF8F00), onTertiaryContainerDark = Color(0xFFFFE082),
                backgroundLight = Color(0xFFF5F5F5),
                surfaceLight = Color(0xFFFFFFFF),
                surfaceContainerLowLight = Color(0xFFFFFFFF),
                backgroundDark = Color(0xFF0D1B2A),
                surfaceDark = Color(0xFF1B2838),
                surfaceContainerLowDark = Color(0xFF1B2838),
                // Layout: compact ES Explorer style
                cornerRadius = 8,
                cardElevation = 1f,
                coloredHeader = true,
                itemLayoutStyle = ItemLayoutStyle.LIST_ROW,
                listItemHeight = 48,
                fileIconSize = 22,
                thumbnailSize = 36,
                folderGridIconSize = 40,
                showDividers = true,
                showDateInList = true,
                showBreadcrumb = true,
                itemCornerRadius = 0,
                listHorizontalPadding = 0,
                gridSpacing = 0,
                folderIconColor = Color(0xFFFF9800),
                highlightItems = false,
                showSizeInList = true,
            ),
        ),

        // ═══════════════════════════════════════════════════════════════════
        // 2. Google Files
        // Each file in its own rounded card, no dividers, soft shadows,
        // extra large rounded corners, spacious, pastel accents
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "Google Files",
            description = "Card-style items, soft rounded, minimal",
            previewColor = Color(0xFF4285F4),
            colors = ThemeColors(
                primaryLight = Color(0xFF4285F4), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFD7E8FD), onPrimaryContainerLight = Color(0xFF0D47A1),
                secondaryLight = Color(0xFF34A853), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFD4EDDA), onSecondaryContainerLight = Color(0xFF1B5E20),
                tertiaryLight = Color(0xFFFBBC05), onTertiaryLight = Color(0xFF3E2723),
                tertiaryContainerLight = Color(0xFFFFF3CD), onTertiaryContainerLight = Color(0xFF795548),
                primaryDark = Color(0xFF8AB4F8), onPrimaryDark = Color(0xFF0D47A1),
                primaryContainerDark = Color(0xFF1A73E8), onPrimaryContainerDark = Color(0xFFD7E8FD),
                secondaryDark = Color(0xFF81C995), onSecondaryDark = Color(0xFF1B5E20),
                secondaryContainerDark = Color(0xFF2E7D32), onSecondaryContainerDark = Color(0xFFD4EDDA),
                tertiaryDark = Color(0xFFFDD663), onTertiaryDark = Color(0xFF3E2723),
                tertiaryContainerDark = Color(0xFFF9A825), onTertiaryContainerDark = Color(0xFFFFF3CD),
                backgroundLight = Color(0xFFF8F9FA),
                surfaceLight = Color(0xFFFFFFFF),
                surfaceVariantLight = Color(0xFFF1F3F4),
                surfaceContainerLowLight = Color(0xFFFFFFFF),
                backgroundDark = Color(0xFF1A1A2E),
                surfaceDark = Color(0xFF16213E),
                surfaceContainerLowDark = Color(0xFF16213E),
                // Layout: Google Files card style
                cornerRadius = 16,
                cardElevation = 0f,
                coloredHeader = false,
                itemLayoutStyle = ItemLayoutStyle.CARD,
                listItemHeight = 64,
                fileIconSize = 28,
                thumbnailSize = 44,
                folderGridIconSize = 52,
                showDividers = false,
                showDateInList = false,
                showBreadcrumb = false,
                itemCornerRadius = 12,
                listHorizontalPadding = 12,
                gridSpacing = 8,
                folderIconColor = Color(0xFF4285F4),
                highlightItems = true,
                showSizeInList = true,
            ),
        ),

        // ═══════════════════════════════════════════════════════════════════
        // 3. MiXplorer Dark
        // Ultra-compact, no dividers, tiny icons, dark navy, power-user
        // Maximum information density, minimal chrome
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "MiXplorer",
            description = "Ultra-compact dark navy, max info density",
            previewColor = Color(0xFF0F3460),
            colors = ThemeColors(
                primaryLight = Color(0xFF0F3460), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFD6E4FF), onPrimaryContainerLight = Color(0xFF001D36),
                secondaryLight = Color(0xFF536DFE), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFDDE1FF), onSecondaryContainerLight = Color(0xFF0A1F5C),
                tertiaryLight = Color(0xFF00BFA5), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFA7FFEB), onTertiaryContainerLight = Color(0xFF004D40),
                primaryDark = Color(0xFF82B1FF), onPrimaryDark = Color(0xFF003258),
                primaryContainerDark = Color(0xFF0F3460), onPrimaryContainerDark = Color(0xFFD6E4FF),
                secondaryDark = Color(0xFFB3C0FF), onSecondaryDark = Color(0xFF0A1F5C),
                secondaryContainerDark = Color(0xFF3B52CC), onSecondaryContainerDark = Color(0xFFDDE1FF),
                tertiaryDark = Color(0xFF64FFDA), onTertiaryDark = Color(0xFF004D40),
                tertiaryContainerDark = Color(0xFF00897B), onTertiaryContainerDark = Color(0xFFA7FFEB),
                backgroundLight = Color(0xFFECEFF1),
                surfaceLight = Color(0xFFFFFFFF),
                surfaceVariantLight = Color(0xFFCFD8DC),
                surfaceContainerLowLight = Color(0xFFF5F5F5),
                backgroundDark = Color(0xFF0A1929),
                surfaceDark = Color(0xFF0D2137),
                surfaceVariantDark = Color(0xFF132F4C),
                surfaceContainerLowDark = Color(0xFF0D2137),
                // Layout: MiXplorer ultra-compact
                cornerRadius = 4,
                cardElevation = 0f,
                coloredHeader = false,
                itemLayoutStyle = ItemLayoutStyle.COMPACT,
                listItemHeight = 40,
                fileIconSize = 18,
                thumbnailSize = 32,
                folderGridIconSize = 36,
                showDividers = false,
                showDateInList = true,
                showBreadcrumb = true,
                itemCornerRadius = 0,
                listHorizontalPadding = 4,
                gridSpacing = 2,
                folderIconColor = Color(0xFF82B1FF),
                highlightItems = false,
                showSizeInList = true,
            ),
        ),

        // ═══════════════════════════════════════════════════════════════════
        // 4. Material You
        // Expressive rounded shapes, tonal surfaces, large touch targets,
        // pastel palette — pure Google Material Design 3
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "Material You",
            description = "Expressive M3 pastels, large rounded shapes",
            previewColor = Color(0xFF6750A4),
            colors = ThemeColors(
                primaryLight = Color(0xFF6750A4), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFEADDFF), onPrimaryContainerLight = Color(0xFF21005D),
                secondaryLight = Color(0xFF625B71), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFE8DEF8), onSecondaryContainerLight = Color(0xFF1D192B),
                tertiaryLight = Color(0xFF7D5260), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFD8E4), onTertiaryContainerLight = Color(0xFF31111D),
                primaryDark = Color(0xFFD0BCFF), onPrimaryDark = Color(0xFF381E72),
                primaryContainerDark = Color(0xFF4F378B), onPrimaryContainerDark = Color(0xFFEADDFF),
                secondaryDark = Color(0xFFCCC2DC), onSecondaryDark = Color(0xFF332D41),
                secondaryContainerDark = Color(0xFF4A4458), onSecondaryContainerDark = Color(0xFFE8DEF8),
                tertiaryDark = Color(0xFFEFB8C8), onTertiaryDark = Color(0xFF492532),
                tertiaryContainerDark = Color(0xFF633B48), onTertiaryContainerDark = Color(0xFFFFD8E4),
                backgroundLight = Color(0xFFFFFBFE),
                surfaceLight = Color(0xFFFFFBFE),
                surfaceVariantLight = Color(0xFFE7E0EC),
                surfaceContainerLowLight = Color(0xFFF7F2FA),
                backgroundDark = Color(0xFF1C1B1F),
                surfaceDark = Color(0xFF2B2930),
                surfaceVariantDark = Color(0xFF49454F),
                surfaceContainerLowDark = Color(0xFF2B2930),
                // Layout: Material You spacious
                cornerRadius = 28,
                cardElevation = 0f,
                coloredHeader = false,
                itemLayoutStyle = ItemLayoutStyle.LIST_ROW,
                listItemHeight = 64,
                fileIconSize = 28,
                thumbnailSize = 44,
                folderGridIconSize = 52,
                showDividers = false,
                showDateInList = true,
                showBreadcrumb = true,
                itemCornerRadius = 16,
                listHorizontalPadding = 8,
                gridSpacing = 4,
                folderIconColor = Color(0xFFD0BCFF),
                highlightItems = true,
                showSizeInList = true,
            ),
        ),

        // ═══════════════════════════════════════════════════════════════════
        // 5. Amaze File Manager
        // Vibrant colors, card-based items with elevation, colorful accents,
        // open-source community feel — like Amaze File Manager
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "Amaze",
            description = "Vibrant cards with colorful accents & elevation",
            previewColor = Color(0xFF00897B),
            colors = ThemeColors(
                primaryLight = Color(0xFF00897B), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFB2DFDB), onPrimaryContainerLight = Color(0xFF004D40),
                secondaryLight = Color(0xFF5C6BC0), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFC5CAE9), onSecondaryContainerLight = Color(0xFF283593),
                tertiaryLight = Color(0xFFFF7043), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFCCBC), onTertiaryContainerLight = Color(0xFFBF360C),
                primaryDark = Color(0xFF80CBC4), onPrimaryDark = Color(0xFF004D40),
                primaryContainerDark = Color(0xFF00695C), onPrimaryContainerDark = Color(0xFFB2DFDB),
                secondaryDark = Color(0xFF9FA8DA), onSecondaryDark = Color(0xFF283593),
                secondaryContainerDark = Color(0xFF3949AB), onSecondaryContainerDark = Color(0xFFC5CAE9),
                tertiaryDark = Color(0xFFFFAB91), onTertiaryDark = Color(0xFFBF360C),
                tertiaryContainerDark = Color(0xFFF4511E), onTertiaryContainerDark = Color(0xFFFFCCBC),
                backgroundLight = Color(0xFFFAFAFA),
                surfaceLight = Color(0xFFFFFFFF),
                surfaceContainerLowLight = Color(0xFFFFFFFF),
                backgroundDark = Color(0xFF121212),
                surfaceDark = Color(0xFF1E1E1E),
                surfaceContainerLowDark = Color(0xFF252525),
                // Layout: Amaze card style
                cornerRadius = 12,
                cardElevation = 2f,
                coloredHeader = true,
                itemLayoutStyle = ItemLayoutStyle.CARD,
                listItemHeight = 60,
                fileIconSize = 26,
                thumbnailSize = 42,
                folderGridIconSize = 48,
                showDividers = false,
                showDateInList = true,
                showBreadcrumb = true,
                itemCornerRadius = 8,
                listHorizontalPadding = 8,
                gridSpacing = 6,
                folderIconColor = Color(0xFFFF9800),
                highlightItems = true,
                showSizeInList = true,
            ),
        ),

        // ═══════════════════════════════════════════════════════════════════
        // 6. One UI (Samsung)
        // Samsung-inspired large touch targets, clean cards, refined blue,
        // generous spacing — like Samsung's native file manager
        // ═══════════════════════════════════════════════════════════════════
        AppThemeOption(
            name = "One UI",
            description = "Samsung-style large touch targets, clean cards",
            previewColor = Color(0xFF1259C3),
            colors = ThemeColors(
                primaryLight = Color(0xFF1259C3), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFD4E3FF), onPrimaryContainerLight = Color(0xFF001B3D),
                secondaryLight = Color(0xFF565F71), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFDAE2F9), onSecondaryContainerLight = Color(0xFF131C2B),
                tertiaryLight = Color(0xFF6E5673), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFF7D8FA), onTertiaryContainerLight = Color(0xFF27162F),
                primaryDark = Color(0xFFA8C8FF), onPrimaryDark = Color(0xFF003062),
                primaryContainerDark = Color(0xFF004A8C), onPrimaryContainerDark = Color(0xFFD4E3FF),
                secondaryDark = Color(0xFFBEC6DC), onSecondaryDark = Color(0xFF283141),
                secondaryContainerDark = Color(0xFF3E4759), onSecondaryContainerDark = Color(0xFFDAE2F9),
                tertiaryDark = Color(0xFFD9BDE0), onTertiaryDark = Color(0xFF3F2A45),
                tertiaryContainerDark = Color(0xFF55405C), onTertiaryContainerDark = Color(0xFFF7D8FA),
                backgroundLight = Color(0xFFF9F9FF),
                surfaceLight = Color(0xFFFFFFFF),
                surfaceVariantLight = Color(0xFFE0E2EC),
                surfaceContainerLowLight = Color(0xFFF5F5FA),
                backgroundDark = Color(0xFF1A1C1E),
                surfaceDark = Color(0xFF2C2E31),
                surfaceVariantDark = Color(0xFF44474E),
                surfaceContainerLowDark = Color(0xFF2C2E31),
                // Layout: One UI spacious
                cornerRadius = 20,
                cardElevation = 0f,
                coloredHeader = false,
                itemLayoutStyle = ItemLayoutStyle.LIST_ROW,
                listItemHeight = 68,
                fileIconSize = 30,
                thumbnailSize = 48,
                folderGridIconSize = 56,
                showDividers = false,
                showDateInList = true,
                showBreadcrumb = false,
                itemCornerRadius = 16,
                listHorizontalPadding = 12,
                gridSpacing = 4,
                folderIconColor = Color(0xFF1259C3),
                highlightItems = true,
                showSizeInList = true,
            ),
        ),
    )

    /** Currently selected theme index. Read in Composable for recomposition. */
    val currentThemeIndex = mutableStateOf(0)

    /** Shorthand to get the current theme's colors. */
    val currentThemeColors = mutableStateOf(themes[0].colors)

    fun load(context: Context) {
        val idx = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, 0)
            .coerceIn(0, themes.size - 1)
        currentThemeIndex.value = idx
        currentThemeColors.value = themes[idx].colors
    }

    fun selectTheme(context: Context, index: Int) {
        val idx = index.coerceIn(0, themes.size - 1)
        currentThemeIndex.value = idx
        currentThemeColors.value = themes[idx].colors
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME, idx)
            .apply()
    }
}