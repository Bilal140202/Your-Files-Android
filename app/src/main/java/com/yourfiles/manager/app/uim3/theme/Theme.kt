package com.yourfiles.manager.app.uim3.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Build shapes from the theme's corner radius, or use defaults. */
private fun buildShapes(cornerRadius: Int?): Shapes {
    val r = cornerRadius ?: 12
    val small = (r * 0.6).toInt().coerceAtLeast(4)
    val xsmall = (r * 0.3).toInt().coerceAtLeast(2)
    val large = (r * 1.4).toInt()
    val xlarge = (r * 2.0).toInt()
    return Shapes(
        extraSmall = RoundedCornerShape(xsmall.dp),
        small = RoundedCornerShape(small.dp),
        medium = RoundedCornerShape(r.dp),
        large = RoundedCornerShape(large.dp),
        extraLarge = RoundedCornerShape(xlarge.dp),
    )
}

private fun buildLightScheme(tc: ThemeColors) = lightColorScheme(
    primary = tc.primaryLight, onPrimary = tc.onPrimaryLight,
    primaryContainer = tc.primaryContainerLight, onPrimaryContainer = tc.onPrimaryContainerLight,
    secondary = tc.secondaryLight, onSecondary = tc.onSecondaryLight,
    secondaryContainer = tc.secondaryContainerLight, onSecondaryContainer = tc.onSecondaryContainerLight,
    tertiary = tc.tertiaryLight, onTertiary = tc.onTertiaryLight,
    tertiaryContainer = tc.tertiaryContainerLight, onTertiaryContainer = tc.onTertiaryContainerLight,
    error = errorLight, onError = onErrorLight,
    errorContainer = errorContainerLight, onErrorContainer = onErrorContainerLight,
    background = tc.backgroundLight ?: backgroundLight,
    onBackground = onBackgroundLight,
    surface = tc.surfaceLight ?: surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = tc.surfaceVariantLight ?: surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight, outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight, inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = tc.primaryContainerLight,
    surfaceDim = surfaceDimLight, surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = tc.surfaceContainerLowLight ?: surfaceContainerLowLight,
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
    background = tc.backgroundDark ?: backgroundDark,
    onBackground = onBackgroundDark,
    surface = tc.surfaceDark ?: surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = tc.surfaceVariantDark ?: surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark, outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark, inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = tc.primaryDark,
    surfaceDim = surfaceDimDark, surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = tc.surfaceContainerLowDark ?: surfaceContainerLowDark,
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
    val shapes = buildShapes(tc.cornerRadius)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = shapes,
        content = content,
    )
}
