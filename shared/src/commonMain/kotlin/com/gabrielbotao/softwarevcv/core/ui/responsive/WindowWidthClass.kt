package com.gabrielbotao.softwarevcv.core.ui.responsive

import androidx.compose.ui.unit.Dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Breakpoints

/**
 * Responsive width buckets, derived from the actual space a composable is given (measure, don't assume).
 * Use inside a `BoxWithConstraints`: `Breakpoints`/this helper are the single source of the thresholds —
 * never scatter magic widths. See [[VCV Design-System]] §8.
 */
enum class WindowWidthClass {
    /** phone — single column */
    COMPACT,

    /** tablet — two columns */
    MEDIUM,

    /** desktop — three/four columns */
    EXPANDED;

    companion object {
        /** Classify a measured [maxWidth] (e.g. `BoxWithConstraints { widthClassOf(maxWidth) }`). */
        fun of(maxWidth: Dp): WindowWidthClass = when {
            maxWidth < Breakpoints.CompactMax -> COMPACT
            maxWidth < Breakpoints.MediumMax -> MEDIUM
            else -> EXPANDED
        }
    }
}

/** Columns for a product grid at this width class: 1 (compact) → 2 (medium) → 4 (expanded). */
fun WindowWidthClass.productGridColumns(): Int = when (this) {
    WindowWidthClass.COMPACT -> 1
    WindowWidthClass.MEDIUM -> 2
    WindowWidthClass.EXPANDED -> 4
}
