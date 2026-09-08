package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import kotlinx.coroutines.delay

private val DotSize = 8.dp
private val ChevronSize = 40.dp
private val ChevronGlyph = 14.dp
private const val SwipeThresholdPx = 48f

/**
 * Image carousel with auto-advance **and** manual control: horizontal **swipe/drag**, **prev/next
 * chevrons** (drawn as shapes so they're perfectly centered), and tappable **dots**. The chevrons rest
 * nearly invisible and fade in on hover — so they don't clutter both sides of every image, but reveal as
 * buttons when the pointer is over them. Any manual change re-arms the auto-advance timer. Caller sizes
 * it; images fill + crop; static for a single image. See [[VCV-22]].
 */
@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    autoAdvanceMillis: Long? = null,
) {
    if (imageUrls.isEmpty()) return
    val reduced = Vcv.reducedMotion
    val advanceMillis = autoAdvanceMillis ?: Vcv.motion.carouselAdvanceMillis
    val count = imageUrls.size
    var index by remember(imageUrls) { mutableIntStateOf(0) }
    fun go(delta: Int) { index = (index + delta + count) % count }

    // Auto-advance only when motion is welcome; manual controls (swipe/chevrons/dots) always work.
    LaunchedEffect(imageUrls, index, reduced) {
        if (count > 1 && !reduced) {
            delay(advanceMillis)
            go(1)
        }
    }

    val swipe = if (count > 1) {
        Modifier.pointerInput(imageUrls) {
            var total = 0f
            detectHorizontalDragGestures(
                onDragStart = { total = 0f },
                onDragEnd = {
                    if (total > SwipeThresholdPx) go(-1)
                    else if (total < -SwipeThresholdPx) go(1)
                },
            ) { _, dragAmount -> total += dragAmount }
        }
    } else Modifier

    Box(modifier.then(swipe)) {
        Crossfade(
            targetState = index,
            animationSpec = tween(if (reduced) 0 else Vcv.motion.crossfadeMillis),
            label = "carousel",
        ) { i ->
            RemoteImage(
                url = imageUrls[i % count],
                contentDescription = contentDescription,
                aspectRatio = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (count > 1) {
            Chevron(pointingLeft = true, modifier = Modifier.align(Alignment.CenterStart).padding(Vcv.spacing.sm)) { go(-1) }
            Chevron(pointingLeft = false, modifier = Modifier.align(Alignment.CenterEnd).padding(Vcv.spacing.sm)) { go(1) }
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(Vcv.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.xs),
            ) {
                val active = MaterialTheme.colorScheme.onPrimary
                val inactive = active.copy(alpha = 0.4f)
                imageUrls.indices.forEach { i ->
                    Dot(color = if (i == index) active else inactive, onClick = { index = i })
                }
            }
        }
    }
}

@Composable
private fun Chevron(pointingLeft: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val alpha by animateFloatAsState(
        targetValue = if (hovered) 1f else Vcv.motion.subtleControlAlpha,
        animationSpec = tween(Vcv.motion.hoverFadeMillis),
        label = "chevron-alpha",
    )
    val glyphColor = MaterialTheme.colorScheme.onSurface
    val bg = MaterialTheme.colorScheme.surface
    Box(
        modifier = modifier
            .size(ChevronSize)
            .alpha(alpha)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.6f))
            .hoverable(interaction)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(ChevronGlyph)) {
            val w = size.width
            val h = size.height
            val stroke = 2.dp.toPx()
            val near = if (pointingLeft) w * 0.38f else w * 0.62f
            val far = if (pointingLeft) w * 0.62f else w * 0.38f
            drawLine(glyphColor, Offset(far, h * 0.22f), Offset(near, h * 0.5f), stroke, StrokeCap.Round)
            drawLine(glyphColor, Offset(near, h * 0.5f), Offset(far, h * 0.78f), stroke, StrokeCap.Round)
        }
    }
}

@Composable
private fun Dot(color: Color, onClick: () -> Unit) {
    Box(Modifier.size(DotSize).clip(CircleShape).background(color).clickable(onClick = onClick))
}
