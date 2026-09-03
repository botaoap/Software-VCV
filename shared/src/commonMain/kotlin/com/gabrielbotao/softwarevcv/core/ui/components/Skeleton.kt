package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** A gently pulsing placeholder block for loading skeletons (matches the image placeholder tone). §7. */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha",
    )
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha), MaterialTheme.shapes.small))
}

/** Product-card-shaped skeleton (cover + name + price lines) — same footprint as [ProductCard]. */
@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm)) {
        SkeletonBox(Modifier.fillMaxWidth().aspectRatio(3f / 4f))
        SkeletonBox(Modifier.fillMaxWidth(0.7f).height(16.dp))
        SkeletonBox(Modifier.fillMaxWidth(0.4f).height(14.dp))
    }
    Spacer(Modifier.height(Vcv.spacing.xs))
}
