package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

private const val CardHoverZoom = 1.04f

/**
 * Product tile: cover photo (fixed aspect ratio, no layout shift) + name + price, with an optional
 * marketing badge. On pointer devices the photo **zooms subtly on hover** (clipped to the tile) — the
 * signature storefront micro-interaction (VCV-32); touch devices just see the static tile.
 * Domain-agnostic — pages map their `Product` onto these params. §9.
 */
@Composable
fun ProductCard(
    name: String,
    priceCents: Long,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val zoom by animateFloatAsState(
        targetValue = if (hovered) CardHoverZoom else 1f,
        animationSpec = tween(Vcv.motion.hoverFadeMillis),
        label = "card-zoom",
    )
    Column(modifier = modifier.hoverable(interaction).clickable(onClick = onClick)) {
        Box(Modifier.fillMaxWidth().clipToBounds()) {
            RemoteImage(
                url = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = zoom; scaleY = zoom },
            )
            if (badgeText != null) {
                VcvBadge(
                    text = badgeText,
                    color = badgeColor,
                    modifier = Modifier.align(Alignment.TopStart).padding(Vcv.spacing.sm),
                )
            }
        }
        Spacer(Modifier.height(Vcv.spacing.sm))
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
        )
        Spacer(Modifier.height(Vcv.spacing.xs))
        PriceText(
            amountCents = priceCents,
            style = MaterialTheme.typography.bodyLarge,
            color = Vcv.colors.wine,
        )
    }
}
