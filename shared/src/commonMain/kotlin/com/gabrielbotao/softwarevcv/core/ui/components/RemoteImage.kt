package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * The one image primitive. Reserves its space via [aspectRatio] over a placeholder background, so a card
 * **never reflows** when the photo arrives (no layout shift). Portrait `3:4` by default (product shots).
 *
 * VCV-3 ships the layout + Coil `AsyncImage`; the platform network `ImageLoader` is configured in VCV-5
 * (with the Ktor HTTP engine). Until then remote URLs show the placeholder — callers don't change.
 * See [[VCV Design-System]] §10.
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 3f / 4f,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
            )
        }
    }
}
