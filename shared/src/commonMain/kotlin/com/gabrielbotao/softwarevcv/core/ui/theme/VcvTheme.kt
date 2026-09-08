package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.gabrielbotao.softwarevcv.core.platform.prefersReducedMotion

/**
 * Assembles the 1C "Corpo" design system: picks the light/dark [androidx.compose.material3.ColorScheme],
 * provides the brand roles ([LocalVcvColors]) and [LocalSpacing], and sets the typography + shapes.
 * Wrap the app (and previews) in this. See [[VCV Design-System]] §7.
 */
@Composable
fun VcvTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) VcvDarkColorScheme else VcvLightColorScheme
    val brandColors = if (darkTheme) VcvDarkBrandColors else VcvLightBrandColors
    CompositionLocalProvider(
        LocalVcvColors provides brandColors,
        LocalSpacing provides Spacing(),
        LocalMotion provides Motion(),
        LocalReducedMotion provides prefersReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VcvTypography,
            shapes = VcvShapes,
            content = content,
        )
    }
}

/** Ergonomic accessor for brand tokens inside composables: `Vcv.colors.wine`, `Vcv.spacing.md`, `Vcv.motion`. */
object Vcv {
    val colors: VcvColors
        @Composable @ReadOnlyComposable get() = LocalVcvColors.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val motion: Motion
        @Composable @ReadOnlyComposable get() = LocalMotion.current

    /** True when the user asked for reduced motion — components snap instead of animating. */
    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}
