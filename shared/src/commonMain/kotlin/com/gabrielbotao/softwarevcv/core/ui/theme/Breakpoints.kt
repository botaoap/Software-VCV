package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Responsive width thresholds, kept in one place (never scattered as magic widths). Consumed by the
 * `core/ui/responsive/WindowWidthClass` helper (VCV-3) and by screens' `BoxWithConstraints` branches.
 * See [[VCV Design-System]] §8.
 */
object Breakpoints {
    /** `< 600.dp` = compact (phone): single column. */
    val CompactMax = 600.dp

    /** `600..1024.dp` = medium (tablet): two columns. */
    val MediumMax = 1024.dp

    /** Cap measure/text columns on ultra-wide so lines don't run edge-to-edge. */
    val ContentMax = 1360.dp
}
