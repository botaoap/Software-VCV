package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import kotlin.math.roundToInt

/**
 * Full-bleed promo **marquee**: the brand/service phrases scroll continuously from the end to the start
 * of the screen in one seamless loop — the classic storefront ticker (see [[VCV-19 — site structure & UX
 * redesign]]). Copy is caller-supplied so it stays config-driven.
 *
 * Seamless loop: one [MarqueeSequence] is measured, then rendered three times back-to-back and the whole
 * row is translated left by exactly one sequence width before snapping back — because the copies are
 * identical, the snap is invisible. Three copies guarantee the strip stays filled on wide viewports. The
 * bar clips its bounds so nothing spills. See [[VCV-22]].
 */
@Composable
fun PromoBar(messages: List<String>, modifier: Modifier = Modifier) {
    if (messages.isEmpty()) return
    val pxPerSecond = Vcv.motion.marqueePxPerSecond
    val offsetX = remember { Animatable(0f) }
    var seqWidth by remember { mutableIntStateOf(0) }

    LaunchedEffect(seqWidth, messages, pxPerSecond) {
        if (seqWidth > 0) {
            val durationMillis = (seqWidth / pxPerSecond * 1000f).roundToInt()
            offsetX.snapTo(0f)
            while (true) {
                offsetX.animateTo(
                    targetValue = -seqWidth.toFloat(),
                    animationSpec = tween(durationMillis, easing = LinearEasing),
                )
                offsetX.snapTo(0f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .clipToBounds()
            .padding(vertical = Vcv.spacing.xs),
    ) {
        Row(Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }) {
            MarqueeSequence(messages, Modifier.onSizeChanged { seqWidth = it.width })
            MarqueeSequence(messages)
            MarqueeSequence(messages)
        }
    }
}

/** One pass of the phrases, each followed by a uniform gap so the seam between copies matches. */
@Composable
private fun MarqueeSequence(messages: List<String>, modifier: Modifier = Modifier) {
    Row(modifier) {
        messages.forEach { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.width(Vcv.spacing.xl))
        }
    }
}
