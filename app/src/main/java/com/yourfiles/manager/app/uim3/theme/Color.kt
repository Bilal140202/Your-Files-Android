package com.yourfiles.manager.app.uim3.theme

import androidx.compose.ui.graphics.Color

/**
 * Layout style enum — determines how file items are rendered.
 */
enum class ItemLayoutStyle {
    /** Standard Material list rows with icon + text + meta */
    LIST_ROW,
    /** Each item in its own card (like Google Files) */
    CARD,
    /** Compact rows with no dividers, minimal padding */
    COMPACT,
}

/**
 * Holds the complete per-theme visual tokens — chromatic palette, neutral
 * overrides, shape rounding, card style, header treatment, AND layout
 * parameters that change the actual UI structure (not just colors).
 */
data class ThemeColors(
    // ── Chromatic tokens ───────────────────────────────────────────────
    val primaryLight: Color, val onPrimaryLight: Color,
    val primaryContainerLight: Color, val onPrimaryContainerLight: Color,
    val secondaryLight: Color, val onSecondaryLight: Color,
    val secondaryContainerLight: Color, val onSecondaryContainerLight: Color,
    val tertiaryLight: Color, val onTertiaryLight: Color,
    val tertiaryContainerLight: Color, val onTertiaryContainerLight: Color,
    val primaryDark: Color, val onPrimaryDark: Color,
    val primaryContainerDark: Color, val onPrimaryContainerDark: Color,
    val secondaryDark: Color, val onSecondaryDark: Color,
    val secondaryContainerDark: Color, val onSecondaryContainerDark: Color,
    val tertiaryDark: Color, val onTertiaryDark: Color,
    val tertiaryContainerDark: Color, val onTertiaryContainerDark: Color,
    // ── Per-theme neutral overrides (null = use defaults) ─────────────
    val backgroundLight: Color? = null,
    val surfaceLight: Color? = null,
    val surfaceVariantLight: Color? = null,
    val surfaceContainerLowLight: Color? = null,
    val backgroundDark: Color? = null,
    val surfaceDark: Color? = null,
    val surfaceVariantDark: Color? = null,
    val surfaceContainerLowDark: Color? = null,
    // ── Shape rounding (dp) — null = use AppShapes defaults ────────────
    val cornerRadius: Int? = null,
    // ── Card style hint ────────────────────────────────────────────────
    val cardElevation: Float = 0f,
    val outlinedCards: Boolean = false,
    // ── Header bar style ───────────────────────────────────────────────
    val coloredHeader: Boolean = true,

    // ═══════════════════════════════════════════════════════════════════
    // LAYOUT PARAMETERS — These change the actual UI structure, not just
    // colors. Each theme template sets these to create a distinct look.
    // ═══════════════════════════════════════════════════════════════════

    /** How file items are rendered: list row, card, or compact */
    val itemLayoutStyle: ItemLayoutStyle = ItemLayoutStyle.LIST_ROW,

    /** List item height in dp (for LIST_ROW and COMPACT) */
    val listItemHeight: Int = 56,

    /** File/folder icon size in dp */
    val fileIconSize: Int = 24,

    /** Thumbnail size in dp (for images/videos in list) */
    val thumbnailSize: Int = 40,

    /** Folder icon size in dp (grid mode) */
    val folderGridIconSize: Int = 48,

    /** Whether to show dividers between list items */
    val showDividers: Boolean = true,

    /** Whether to show date in list items */
    val showDateInList: Boolean = true,

    /** Whether to show breadcrumb path in explorer header */
    val showBreadcrumb: Boolean = true,

    /** Corner radius for card-style items in dp */
    val itemCornerRadius: Int = 8,

    /** Horizontal padding for the file list in dp */
    val listHorizontalPadding: Int = 0,

    /** Vertical spacing between items in grid mode in dp */
    val gridSpacing: Int = 0,

    /** Custom folder icon color (null = use default orange) */
    val folderIconColor: Color? = null,

    /** Whether items have a subtle background highlight on hover/press */
    val highlightItems: Boolean = false,

    /** Whether to show file size in the list items */
    val showSizeInList: Boolean = true,
)

// ── Default Light neutral tokens ────────────────────────────────────────
val backgroundLight = Color(0xFFFAFAFA)
val onBackgroundLight = Color(0xFF212121)
val surfaceLight = Color(0xFFFFFFFF)
val onSurfaceLight = Color(0xFF212121)
val surfaceVariantLight = Color(0xFFF5F5F5)
val onSurfaceVariantLight = Color(0xFF757575)
val outlineLight = Color(0xFFBDBDBD)
val outlineVariantLight = Color(0xFFE0E0E0)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF212121)
val inverseOnSurfaceLight = Color(0xFFF5F5F5)
val surfaceDimLight = Color(0xFFE0E0E0)
val surfaceBrightLight = Color(0xFFFFFFFF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF5F5F5)
val surfaceContainerLight = Color(0xFFF0F0F0)
val surfaceContainerHighLight = Color(0xFFEEEEEE)
val surfaceContainerHighestLight = Color(0xFFE8E8E8)

// ── Default Dark neutral tokens ─────────────────────────────────────────
val backgroundDark = Color(0xFF121212)
val onBackgroundDark = Color(0xFFE0E0E0)
val surfaceDark = Color(0xFF1E1E1E)
val onSurfaceDark = Color(0xFFE0E0E0)
val surfaceVariantDark = Color(0xFF2C2C2C)
val onSurfaceVariantDark = Color(0xFFBDBDBD)
val outlineDark = Color(0xFF757575)
val outlineVariantDark = Color(0xFF424242)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE0E0E0)
val inverseOnSurfaceDark = Color(0xFF212121)
val surfaceDimDark = Color(0xFF121212)
val surfaceBrightDark = Color(0xFF2C2C2C)
val surfaceContainerLowestDark = Color(0xFF0D0D0D)
val surfaceContainerLowDark = Color(0xFF1A1A1A)
val surfaceContainerDark = Color(0xFF1E1E1E)
val surfaceContainerHighDark = Color(0xFF282828)
val surfaceContainerHighestDark = Color(0xFF333333)

// ── Error tokens (constant) ─────────────────────────────────────────────
val errorLight = Color(0xFFD32F2F)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFCDD2)
val onErrorContainerLight = Color(0xFFB71C1C)

val errorDark = Color(0xFFEF5350)
val onErrorDark = Color(0xFFB71C1C)
val errorContainerDark = Color(0xFFD32F2F)
val onErrorContainerDark = Color(0xFFFFCDD2)

// ── Semantic & Category Colors ──────────────────────────────────────────
object AppColors {
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
    val Error = Color(0xFFE53935)

    val DarkNavy = Color(0xFF0D1D2E)
    val DarkSurface = Color(0xFF152238)
    val DarkCard = Color(0xFF1A2D47)

    val CategoryImages = Color(0xFFE91E63)
    val CategoryVideos = Color(0xFF9C27B0)
    val CategoryAudio = Color(0xFFFF9800)
    val CategoryDocuments = Color(0xFF2196F3)
    val CategoryApk = Color(0xFF4CAF50)
    val CategoryArchives = Color(0xFF795548)
    val CategoryOther = Color(0xFF607D8B)
    val FolderColor = Color(0xFFFF9800)
}