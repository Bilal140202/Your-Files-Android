package com.yourfiles.manager.app

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.yourfiles.manager.app.uim3.theme.ThemeColors

/**
 * Manages user-selectable color themes.
 *
 * Each [AppThemeOption] defines a unique visual identity with light and dark
 * color palettes. The selected theme is persisted in SharedPreferences and
 * exposed as a Compose [mutableStateOf] for reactive recomposition.
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
        AppThemeOption(
            name = "Ocean Blue",
            description = "Classic blue — clean and professional",
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
            ),
        ),
        AppThemeOption(
            name = "Midnight Purple",
            description = "Deep purple with elegant accent tones",
            previewColor = Color(0xFF7C4DFF),
            colors = ThemeColors(
                primaryLight = Color(0xFF6200EA), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFE8DDFF), onPrimaryContainerLight = Color(0xFF4A148C),
                secondaryLight = Color(0xFF00897B), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFB2DFDB), onSecondaryContainerLight = Color(0xFF004D40),
                tertiaryLight = Color(0xFFFF6D00), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFE0B2), onTertiaryContainerLight = Color(0xFFBF360C),
                primaryDark = Color(0xFFD1C4E9), onPrimaryDark = Color(0xFF311B92),
                primaryContainerDark = Color(0xFF7C4DFF), onPrimaryContainerDark = Color(0xFFE8DDFF),
                secondaryDark = Color(0xFF80CBC4), onSecondaryDark = Color(0xFF004D40),
                secondaryContainerDark = Color(0xFF00897B), onSecondaryContainerDark = Color(0xFFB2DFDB),
                tertiaryDark = Color(0xFFFFB74D), onTertiaryDark = Color(0xFFBF360C),
                tertiaryContainerDark = Color(0xFFFF6D00), onTertiaryContainerDark = Color(0xFFFFE0B2),
            ),
        ),
        AppThemeOption(
            name = "Forest Green",
            description = "Natural green tones for a calm feel",
            previewColor = Color(0xFF2E7D32),
            colors = ThemeColors(
                primaryLight = Color(0xFF2E7D32), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFC8E6C9), onPrimaryContainerLight = Color(0xFF1B5E20),
                secondaryLight = Color(0xFF00838F), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFB2EBF2), onSecondaryContainerLight = Color(0xFF006064),
                tertiaryLight = Color(0xFFFF8F00), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFE082), onTertiaryContainerLight = Color(0xFF5D4037),
                primaryDark = Color(0xFFA5D6A7), onPrimaryDark = Color(0xFF1B5E20),
                primaryContainerDark = Color(0xFF388E3C), onPrimaryContainerDark = Color(0xFFC8E6C9),
                secondaryDark = Color(0xFF80DEEA), onSecondaryDark = Color(0xFF006064),
                secondaryContainerDark = Color(0xFF00838F), onSecondaryContainerDark = Color(0xFFB2EBF2),
                tertiaryDark = Color(0xFFFFC107), onTertiaryDark = Color(0xFF5D4037),
                tertiaryContainerDark = Color(0xFFFF8F00), onTertiaryContainerDark = Color(0xFFFFE082),
            ),
        ),
        AppThemeOption(
            name = "Sunset Orange",
            description = "Warm orange palette with vibrant energy",
            previewColor = Color(0xFFE65100),
            colors = ThemeColors(
                primaryLight = Color(0xFFE65100), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFFFE0B2), onPrimaryContainerLight = Color(0xFFBF360C),
                secondaryLight = Color(0xFF00695C), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFB2DFDB), onSecondaryContainerLight = Color(0xFF004D40),
                tertiaryLight = Color(0xFF1565C0), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFBBDEFB), onTertiaryContainerLight = Color(0xFF0D47A1),
                primaryDark = Color(0xFFFFB74D), onPrimaryDark = Color(0xFFBF360C),
                primaryContainerDark = Color(0xFFF57C00), onPrimaryContainerDark = Color(0xFFFFE0B2),
                secondaryDark = Color(0xFF80CBC4), onSecondaryDark = Color(0xFF004D40),
                secondaryContainerDark = Color(0xFF00897B), onSecondaryContainerDark = Color(0xFFB2DFDB),
                tertiaryDark = Color(0xFF90CAF9), onTertiaryDark = Color(0xFF0D47A1),
                tertiaryContainerDark = Color(0xFF1565C0), onTertiaryContainerDark = Color(0xFFBBDEFB),
            ),
        ),
        AppThemeOption(
            name = "Rose Pink",
            description = "Soft rose with modern blush accents",
            previewColor = Color(0xFFC2185B),
            colors = ThemeColors(
                primaryLight = Color(0xFFC2185B), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFFCE4EC), onPrimaryContainerLight = Color(0xFF880E4F),
                secondaryLight = Color(0xFF5C6BC0), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFC5CAE9), onSecondaryContainerLight = Color(0xFF283593),
                tertiaryLight = Color(0xFFFF6F00), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFFFE0B2), onTertiaryContainerLight = Color(0xFFE65100),
                primaryDark = Color(0xFFF48FB1), onPrimaryDark = Color(0xFF880E4F),
                primaryContainerDark = Color(0xFFE91E63), onPrimaryContainerDark = Color(0xFFFCE4EC),
                secondaryDark = Color(0xFF9FA8DA), onSecondaryDark = Color(0xFF283593),
                secondaryContainerDark = Color(0xFF5C6BC0), onSecondaryContainerDark = Color(0xFFC5CAE9),
                tertiaryDark = Color(0xFFFFB74D), onTertiaryDark = Color(0xFFE65100),
                tertiaryContainerDark = Color(0xFFFF6F00), onTertiaryContainerDark = Color(0xFFFFE0B2),
            ),
        ),
        AppThemeOption(
            name = "Slate Grey",
            description = "Minimal neutral grey for a subtle look",
            previewColor = Color(0xFF455A64),
            colors = ThemeColors(
                primaryLight = Color(0xFF455A64), onPrimaryLight = Color(0xFFFFFFFF),
                primaryContainerLight = Color(0xFFCFD8DC), onPrimaryContainerLight = Color(0xFF263238),
                secondaryLight = Color(0xFF546E7A), onSecondaryLight = Color(0xFFFFFFFF),
                secondaryContainerLight = Color(0xFFB0BEC5), onSecondaryContainerLight = Color(0xFF37474F),
                tertiaryLight = Color(0xFF78909C), onTertiaryLight = Color(0xFFFFFFFF),
                tertiaryContainerLight = Color(0xFFECEFF1), onTertiaryContainerLight = Color(0xFF455A64),
                primaryDark = Color(0xFFB0BEC5), onPrimaryDark = Color(0xFF263238),
                primaryContainerDark = Color(0xFF546E7A), onPrimaryContainerDark = Color(0xFFCFD8DC),
                secondaryDark = Color(0xFF90A4AE), onSecondaryDark = Color(0xFF37474F),
                secondaryContainerDark = Color(0xFF607D8B), onSecondaryContainerDark = Color(0xFFB0BEC5),
                tertiaryDark = Color(0xFFB0BEC5), onTertiaryDark = Color(0xFF455A64),
                tertiaryContainerDark = Color(0xFF78909C), onTertiaryContainerDark = Color(0xFFECEFF1),
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