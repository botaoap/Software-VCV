package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import kotlinx.coroutines.delay

private val DotSize = 8.dp

/**
 * Auto-advancing, crossfading image carousel — the "models in looks" hero motion the reference stores
 * use. The caller sizes it (e.g. a fixed-height hero band); images fill + crop. Static when there's a
 * single image. Respects the perf budget (crossfade only, no heavy libs). See [[VCV-22]].
 */
@Composable
fun ImageCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    autoAdvanceMillis: Long = 4500,
) {
    if (imageUrls.isEmpty()) return
    var index by remember(imageUrls) { mutableIntStateOf(0) }
    LaunchedEffect(imageUrls) {
        while (imageUrls.size > 1) {
            delay(autoAdvanceMillis)
            index = (index + 1) % imageUrls.size
        }
    }
    Box(modifier) {
        Crossfade(targetState = index, animationSpec = tween(700), label = "carousel") { i ->
            RemoteImage(
                url = imageUrls[i % imageUrls.size],
                contentDescription = null,
                aspectRatio = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(Vcv.spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.xs),
            ) {
                val active = MaterialTheme.colorScheme.onPrimary
                val inactive = active.copy(alpha = 0.4f)
                imageUrls.indices.forEach { i ->
                    Dot(color = if (i == index) active else inactive)
                }
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(DotSize).clip(CircleShape).background(color))
}
