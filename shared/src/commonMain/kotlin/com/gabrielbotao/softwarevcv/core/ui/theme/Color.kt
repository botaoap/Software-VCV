package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

/**
 * LAYER 2a — Material 3 role mapping (primitives → semantic roles). Screens read `MaterialTheme.colorScheme.*`,
 * so changing a primitive in [BrandPalette] re-colors every role that references it.
 */
internal val VcvLightColorScheme = lightColorScheme(
    primary = BrandPalette.Bordo700,
    onPrimary = BrandPalette.Cream50,
    primaryContainer = BrandPalette.Bordo900,
    onPrimaryContainer = BrandPalette.Cream50,
    secondary = BrandPalette.Rose300,
    onSecondary = BrandPalette.Ink900,
    tertiary = BrandPalette.Rose100,
    onTertiary = BrandPalette.Ink900,
    background = BrandPalette.Cream50,
    onBackground = BrandPalette.Ink900,
    surface = BrandPalette.Cream50,
    onSurface = BrandPalette.Ink900,
    surfaceVariant = BrandPalette.Nude100,
    onSurfaceVariant = BrandPalette.Ink500,
    outline = BrandPalette.Ink500,
    error = BrandPalette.Error500,
    onError = BrandPalette.White,
)

internal val VcvDarkColorScheme = darkColorScheme(
    primary = BrandPalette.Rose300,
    onPrimary = BrandPalette.Ink900,
    primaryContainer = BrandPalette.Bordo900,
    onPrimaryContainer = BrandPalette.Cream50,
    secondary = BrandPalette.Rose100,
    onSecondary = BrandPalette.Ink900,
    tertiary = BrandPalette.Rose300,
    onTertiary = BrandPalette.Ink900,
    background = BrandPalette.InkBg,
    onBackground = BrandPalette.Cream50,
    surface = BrandPalette.InkSurface,
    onSurface = BrandPalette.Cream50,
    surfaceVariant = BrandPalette.InkSurfaceVariant,
    onSurfaceVariant = BrandPalette.MutedOnDark,
    outline = BrandPalette.MutedOnDark,
    error = BrandPalette.Error500,
    onError = BrandPalette.White,
)
