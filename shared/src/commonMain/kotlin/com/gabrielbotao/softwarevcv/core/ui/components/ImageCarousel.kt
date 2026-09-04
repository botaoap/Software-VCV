package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import kotlinx.coroutines.delay

private val DotSize = 8.dp
private val ChevronSize = 36.dp
private const val SwipeThresholdPx = 48f

/**
 * Image carousel with auto-advance **and** manual control: horizontal **swipe/drag**, tappable **prev/next
 * chevrons**, and tappable **dots**. Any manual change restarts the auto-advance timer. The caller sizes
 * it (e.g. a fixed-height hero band); images fill + crop. Static when there's a single image. See [[VCV-22]].
 */
@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    autoAdvanceMillis: Long = 4500,
) {
    if (imageUrls.isEmpty()) return
    val count = imageUrls.size
    var index by remember(imageUrls) { mutableIntStateOf(0) }
    fun go(delta: Int) { index = (index + delta + count) % count }

    // Re-armed on every change (manual or auto), so a user interaction resets the countdown.
    LaunchedEffect(imageUrls, index) {
        if (count > 1) {
            delay(autoAdvanceMillis)
            go(1)
        }
    }

    val swipe = if (count > 1) {
        Modifier.pointerInput(imageUrls) {
            var total = 0f
            detectHorizontalDragGestures(
                onDragStart = { total = 0f },
                onDragEnd = {
                    if (total > SwipeThresholdPx) go(-1)          // dragged right → previous
                    else if (total < -SwipeThresholdPx) go(1)     // dragged left → next
                },
            ) { _, dragAmount -> total += dragAmount }
        }
    } else Modifier

    Box(modifier.then(swipe)) {
        Crossfade(targetState = index, animationSpec = tween(700), label = "carousel") { i ->
            RemoteImage(
                url = imageUrls[i % count],
                contentDescription = null,
                aspectRatio = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (count > 1) {
            Chevron("‹", Modifier.align(Alignment.CenterStart).padding(Vcv.spacing.sm)) { go(-1) }
            Chevron("›", Modifier.align(Alignment.CenterEnd).padding(Vcv.spacing.sm)) { go(1) }
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
private fun Chevron(glyph: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(ChevronSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
    }
}

@Composable
private fun Dot(color: Color, onClick: () -> Unit) {
    Box(Modifier.size(DotSize).clip(CircleShape).background(color).clickable(onClick = onClick))
}
