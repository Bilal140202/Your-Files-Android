package com.yourfiles.manager.app.uim3.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private fun buildLightScheme(tc: ThemeColors) = lightColorScheme(
    primary = tc.primaryLight, onPrimary = tc.onPrimaryLight,
    primaryContainer = tc.primaryContainerLight, onPrimaryContainer = tc.onPrimaryContainerLight,
    secondary = tc.secondaryLight, onSecondary = tc.onSecondaryLight,
    secondaryContainer = tc.secondaryContainerLight, onSecondaryContainer = tc.onSecondaryContainerLight,
    tertiary = tc.tertiaryLight, onTertiary = tc.onTertiaryLight,
    tertiaryContainer = tc.tertiaryContainerLight, onTertiaryContainer = tc.onTertiaryContainerLight,
    error = errorLight, onError = onErrorLight,
    errorContainer = errorContainerLight, onErrorContainer = onErrorContainerLight,
    background = backgroundLight, onBackground = onBackgroundLight,
    surface = surfaceLight, onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight, onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight, outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight, inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = tc.primaryContainerLight,
    surfaceDim = surfaceDimLight, surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight, surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight, surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private fun buildDarkScheme(tc: ThemeColors) = darkColorScheme(
    primary = tc.primaryDark, onPrimary = tc.onPrimaryDark,
    primaryContainer = tc.primaryContainerDark, onPrimaryContainer = tc.onPrimaryContainerDark,
    secondary = tc.secondaryDark, onSecondary = tc.onSecondaryDark,
    secondaryContainer = tc.secondaryContainerDark, onSecondaryContainer = tc.onSecondaryContainerDark,
    tertiary = tc.tertiaryDark, onTertiary = tc.onTertiaryDark,
    tertiaryContainer = tc.tertiaryContainerDark, onTertiaryContainer = tc.onTertiaryContainerDark,
    error = errorDark, onError = onErrorDark,
    errorContainer = errorContainerDark, onErrorContainer = onErrorContainerDark,
    background = AppColors.DarkNavy, onBackground = onBackgroundDark,
    surface = AppColors.DarkSurface, onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark, onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark, outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark, inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = tc.primaryDark,
    surfaceDim = surfaceDimDark, surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark, surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark, surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = false,
    themeColors: ThemeColors? = null,
    content: @Composable () -> Unit,
) {
    val tc = themeColors ?: com.yourfiles.manager.app.AppThemeManager.themes[0].colors
    val colorScheme = if (darkTheme) buildDarkScheme(tc) else buildLightScheme(tc)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}