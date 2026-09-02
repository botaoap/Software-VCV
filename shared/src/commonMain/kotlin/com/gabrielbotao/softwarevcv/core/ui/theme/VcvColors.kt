package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * LAYER 2b — brand roles beyond the Material set (wine/rose/nude/cream/ink + marketing badges), provided
 * via [LocalVcvColors] so light/dark can switch without touching screens. Screens read
 * `LocalVcvColors.current.*` (or `Vcv.colors.*`). See [[VCV Design-System]] §5.
 */
@Immutable
data class VcvColors(
    val wine: Color,
    val rose: Color,
    val nude: Color,
    val cream: Color,
    val ink: Color,
    val muted: Color,
    val badgeBestSeller: Color,
    val badgeLastUnits: Color,
)

internal val VcvLightBrandColors = VcvColors(
    wine = BrandPalette.Bordo700,
    rose = BrandPalette.Rose300,
    nude = BrandPalette.Nude100,
    cream = BrandPalette.Cream50,
    ink = BrandPalette.Ink900,
    muted = BrandPalette.Ink500,
    badgeBestSeller = BrandPalette.Bordo700,
    badgeLastUnits = BrandPalette.Error500,
)

internal val VcvDarkBrandColors = VcvColors(
    wine = BrandPalette.Rose300,
    rose = BrandPalette.Rose100,
    nude = BrandPalette.InkSurfaceVariant,
    cream = BrandPalette.InkBg,
    ink = BrandPalette.Cream50,
    muted = BrandPalette.MutedOnDark,
    badgeBestSeller = BrandPalette.Rose300,
    badgeLastUnits = BrandPalette.Error500,
)

/** Provided by [VcvTheme]; reading it outside the theme is a programming error. */
val LocalVcvColors = staticCompositionLocalOf<VcvColors> {
    error("LocalVcvColors not provided — wrap content in VcvTheme { }")
}
