package com.autobill.smartpos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * SmartPos Light Color Scheme
 * Restaurant-optimized colors for tablet devices
 * Colors: Primary - FC8019 (Orange), Secondary - FFEBDB (Cream), Neutral - Black/Gray/White
 */
private val SmartPosLightColorScheme = lightColorScheme(
    // Primary Brand Colors
    primary = PrimaryBrand,                    // FC8019 - Main action color
    onPrimary = White,                         // Text on primary
    primaryContainer = PrimaryLight,           // Light orange backgrounds
    onPrimaryContainer = Black,                // Text on primary container

    // Secondary Colors
    secondary = SecondaryBrand,                // FFEBDB - Cream
    onSecondary = Black,                       // Text on secondary
    secondaryContainer = SecondaryLight,       // Very light cream
    onSecondaryContainer = Black,              // Text on secondary container

    // Tertiary Colors
    tertiary = PrimaryBrand,                   // Same as primary for consistency
    onTertiary = White,
    tertiaryContainer = PrimaryLight,
    onTertiaryContainer = Black,

    // Surface & Background
    surface = SurfacePrimary,                  // Pure white - main surfaces
    onSurface = Black,                         // Black text on surfaces
    surfaceVariant = SurfaceSecondary,         // Light gray - alternate surfaces
    onSurfaceVariant = Gray600,                // Dark gray text

    // Background
    background = SurfacePrimary,               // White background
    onBackground = Black,                      // Black text on background

    // Error States
    error = Error,                             // Red for errors
    onError = White,                           // White text on error
    errorContainer = ComposeColor(0xFFFFDAD6),        // Light red background
    onErrorContainer = Error,                  // Red text on error container

    // Outline & Borders
    outline = ComposeColor(0xFFDDDDDD),                         // Light gray - borders
    outlineVariant = ComposeColor(0xFFEAEAEA),                  // Very light gray - subtle dividers
    
    // Scrim & Overlay
    scrim = ComposeColor(0xFF000000).copy(alpha = 0.54f)          // Dark overlay
)

/**
 * SmartPos Theme
 * Tablet-first restaurant POS system
 * Light theme optimized for daytime restaurant environment
 */
@Composable
fun SmartPosTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = SmartPosLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
