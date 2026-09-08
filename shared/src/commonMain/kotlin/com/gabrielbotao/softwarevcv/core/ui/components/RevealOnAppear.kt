package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Fades + slides its [content] up when it first appears. In a lazy list/grid, items compose as they
 * scroll into view, so this reads as a scroll-reveal; on static pages it's a gentle entrance. Cheap
 * (two value animations), and a no-op after the first pass. See [[VCV-22]].
 */
@Composable
fun RevealOnAppear(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    if (Vcv.reducedMotion) {
        Box(modifier) { content() }
        return
    }
    val motion = Vcv.motion
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = motion.revealMillis, delayMillis = delayMillis),
        label = "reveal-alpha",
    )
    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else motion.revealSlideUp,
        animationSpec = tween(durationMillis = motion.revealMillis, delayMillis = delayMillis),
        label = "reveal-offset",
    )
    Box(modifier.offset(y = offsetY).alpha(alpha)) {
        content()
    }
}
