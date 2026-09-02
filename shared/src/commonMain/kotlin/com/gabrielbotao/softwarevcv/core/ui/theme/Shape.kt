package com.gabrielbotao.softwarevcv.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shapes for 1C "Corpo": mostly **square / low-radius** — fashion editorial reads sharp, not rounded
 * (product imagery stays square-cornered). See [[VCV Design-System]] §7.
 */
val VcvShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)
