package com.yourfiles.manager.app.uim3.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.yourfiles.manager.R

// =============================================================================
// Typography System — "Your Files" v2.0
// =============================================================================
// Google Font: Outfit — a modern geometric sans-serif with clean, wide letterforms
// and excellent readability at all sizes. Chosen for its distinctive personality
// that sets "Your Files" apart from generic Android file managers.
//
// Provider: Google Play Services downloadable fonts
// Certificates: GMS font provider certificates (required for Downloadable Fonts)
//
// Loaded weights:
//   - Normal  (400)  → body text, captions, subtle labels
//   - Medium  (500)  → category titles, chip text, medium-emphasis UI
//   - SemiBold(600)  → sub-section headers, card titles, button text
//   - Bold    (700)  → hero text, screen titles, section headers, stats
// =============================================================================

/** Google Fonts downloadable-font provider backed by Google Play Services. */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

/** Outfit font definition resolved at runtime via Downloadable Fonts. */
private val OutfitFont = GoogleFont("Outfit")

/**
 * Outfit font family with four weights covering the full M3 type scale.
 *
 * | Weight    | Value | Primary role                          |
 * |-----------|-------|---------------------------------------|
 * | Normal    |  400  | Body text, captions, tertiary labels  |
 * | Medium    |  500  | Category titles, chip text             |
 * | SemiBold  |  600  | Card titles, buttons, sub-sections     |
 * | Bold      |  700  | Hero text, screen titles, stats        |
 */
val OutfitFontFamily = FontFamily(
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = OutfitFont, fontProvider = provider, weight = FontWeight.Bold),
)

// =============================================================================
// Complete Material 3 Typography Scale
// =============================================================================
// All 15 M3 type slots are defined below with Outfit-specific sizing and
// letter-spacing tuned for a file-manager UI. Each slot includes a comment
// mapping it to the concrete UI surface where it appears in "Your Files".
// =============================================================================

val AppTypography = Typography(

    // ---------------------------------------------------------------------------
    // DISPLAY — Top-level hero elements, storage stats, onboarding screens
    // ---------------------------------------------------------------------------

    /** 57 sp Bold — "Welcome" hero text on the onboarding / splash screen. */
    displayLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),

    /** 45 sp Bold — Large storage size display such as "64 GB". */
    displayMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),

    /** 36 sp Bold — Prominent stats like "4.7 GB freeable" on the dashboard. */
    displaySmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // ---------------------------------------------------------------------------
    // HEADLINE — Screen-level titles and major section dividers
    // ---------------------------------------------------------------------------

    /** 32 sp Bold — Primary screen titles (e.g. "Storage Overview", "Smart Clean"). */
    headlineLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    /** 28 sp Bold — Section headers like "Categories", "Quick Actions". */
    headlineMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    /** 24 sp SemiBold — Sub-section headers (e.g. "Recent Files", "Large Files"). */
    headlineSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // ---------------------------------------------------------------------------
    // TITLE — Card headers, list item names, category labels
    // ---------------------------------------------------------------------------

    /** 22 sp SemiBold — Card titles (e.g. "Internal Storage", "Duplicate Finder"). */
    titleLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    /** 18 sp Medium — Category item titles (e.g. "Images", "Videos", "Documents"). */
    titleMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    /** 14 sp Medium — List item titles and file names in browse/search results. */
    titleSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // ---------------------------------------------------------------------------
    // BODY — Descriptive text, file metadata, secondary information
    // ---------------------------------------------------------------------------

    /** 16 sp Normal — Primary descriptions (e.g. "Scanning your storage…"). */
    bodyLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /** 14 sp Normal — Secondary descriptions (e.g. file size, date, path info). */
    bodyMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    /** 12 sp Normal — Tertiary labels (e.g. "Last modified", "23 files"). */
    bodySmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // ---------------------------------------------------------------------------
    // LABEL — Buttons, chips, badges, and small interactive text
    // ---------------------------------------------------------------------------

    /** 16 sp SemiBold — Button text (e.g. "Clean Now", "Select All", "Back"). */
    labelLarge = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    /** 14 sp Medium — Chip / filter text (e.g. "Images", "Large", "This week"). */
    labelMedium = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    /** 12 sp Normal — Caption text and micro-labels (e.g. "Free", "Used", icons). */
    labelSmall = TextStyle(
        fontFamily = OutfitFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
)
