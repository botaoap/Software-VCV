package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale — editorial layouts want generous whitespace. Screens read `LocalSpacing.current.*`
 * (or `Vcv.spacing.*`); **no magic `dp` in feature Composables**. See [[VCV Design-System]] §7.
 */
@Immutable
data class Spacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 40.dp,
    val xxl: Dp = 64.dp,
)

/** Provided by [VcvTheme]. */
val LocalSpacing = staticCompositionLocalOf { Spacing() }
