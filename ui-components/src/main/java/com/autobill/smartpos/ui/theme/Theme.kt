package com.autobill.smartpos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * SmartPos Light Color Scheme
 * Restaurant-optimized colors for tablet devices
 * Colors: Primary - FC8019 (Orange), Secondary - FFEBDB (Cream), Neutral - Black/Gray/White
 */
private val SmartPosLightColorScheme = lightColorScheme(
    primary = PrimaryBrand,
    onPrimary = White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Black,
    secondary = SecondaryBrand,
    onSecondary = Black,
    secondaryContainer = SecondaryLight,
    onSecondaryContainer = Black,
    tertiary = PrimaryBrand,
    onTertiary = White,
    tertiaryContainer = PrimaryLight,
    onTertiaryContainer = Black,
    surface = SurfacePrimary,
    onSurface = Black,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = Gray600,
    surfaceContainerLow = ComposeColor(0xFFF8F4F1),
    background = SurfacePrimary,
    onBackground = Black,
    error = Error,
    onError = White,
    errorContainer = ComposeColor(0xFFFFDAD6),
    onErrorContainer = Error,
    outline = ComposeColor(0xFFDDDDDD),
    outlineVariant = ComposeColor(0xFFEAEAEA),
    scrim = ComposeColor(0xFF000000).copy(alpha = 0.54f),
)

/**
 * SmartPos Dark Color Scheme
 * Warm dark palette — comfortable for dimly-lit restaurant environments.
 * The brand orange is kept vivid as the primary accent.
 */
private val SmartPosDarkColorScheme = darkColorScheme(
    primary = PrimaryBrand,
    onPrimary = Black,
    primaryContainer = ComposeColor(0xFF7A3200),
    onPrimaryContainer = ComposeColor(0xFFFFDCBE),
    secondary = ComposeColor(0xFF5C3D2A),
    onSecondary = ComposeColor(0xFFFFDCBE),
    secondaryContainer = ComposeColor(0xFF3D2210),
    onSecondaryContainer = ComposeColor(0xFFFFDCBE),
    tertiary = PrimaryBrand,
    onTertiary = Black,
    tertiaryContainer = ComposeColor(0xFF7A3200),
    onTertiaryContainer = ComposeColor(0xFFFFDCBE),
    surface = ComposeColor(0xFF1E1A18),
    onSurface = ComposeColor(0xFFF0DDD5),
    surfaceVariant = ComposeColor(0xFF2C2420),
    onSurfaceVariant = ComposeColor(0xFFD5C0B5),
    surfaceContainerLow = ComposeColor(0xFF251E1B),
    background = ComposeColor(0xFF1A1210),
    onBackground = ComposeColor(0xFFF0DDD5),
    error = ComposeColor(0xFFFF6B6B),
    onError = Black,
    errorContainer = ComposeColor(0xFF8B0000),
    onErrorContainer = ComposeColor(0xFFFFDAD6),
    outline = ComposeColor(0xFF9D8A7D),
    outlineVariant = ComposeColor(0xFF4D3830),
    scrim = ComposeColor(0xFF000000).copy(alpha = 0.54f),
)

/**
 * SmartPos Theme
 * Tablet-first restaurant POS system.
 *
 * [darkTheme] — pass `true` to use the warm dark palette.
 *               Defaults to the system preference when not explicitly set.
 */
@Composable
fun SmartPosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) SmartPosDarkColorScheme else SmartPosLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}


