package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Motion tokens — the brand's animation feel in one place, so durations/easings aren't scattered as
 * magic numbers across components (the same discipline `Spacing`/`Breakpoints` enforce). Read via
 * `Vcv.motion.*` inside composables; a non-composable spec lambda (e.g. Nav3 `transitionSpec`) reads the
 * value into a local first. Durations are milliseconds. See [[VCV Design-System]] §7, [[VCV-35 — motion tokens]].
 */
@Immutable
data class Motion(
    /** Quick affordance fades — hover reveals, small state flips. */
    val hoverFadeMillis: Int = 200,
    /** Standard chrome transitions — route crossfade. */
    val routeFadeMillis: Int = 280,
    /** Content entrances — reveal-on-appear. */
    val revealMillis: Int = 450,
    /** Slow, deliberate — hero image crossfade. */
    val crossfadeMillis: Int = 700,
    /** Reveal-on-appear slide-up distance. */
    val revealSlideUp: Dp = 16.dp,
    /** Rest opacity of pointer-only controls (carousel chevrons) before hover. */
    val subtleControlAlpha: Float = 0.12f,
    /** Hero carousel auto-advance interval. */
    val carouselAdvanceMillis: Long = 4500,
    /** Promo marquee scroll speed (device-independent px per second). */
    val marqueePxPerSecond: Float = 60f,
)

/** Provided by [VcvTheme]. */
val LocalMotion = staticCompositionLocalOf { Motion() }

/**
 * Whether to drop non-essential motion (honors `prefers-reduced-motion`). Provided by [VcvTheme] from the
 * platform seam; components read `Vcv.reducedMotion` to snap animations instead of playing them. See [[VCV-33]].
 */
val LocalReducedMotion = staticCompositionLocalOf { false }
