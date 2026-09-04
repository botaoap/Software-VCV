package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import kotlinx.coroutines.delay

/**
 * Slim top utility bar that cycles through short brand/service messages (frete, atendimento, etc.) —
 * the promo bar every reference storefront opens with, kept restrained for the atelier. Global chrome,
 * see [[VCV-19 — site structure & UX redesign]]. Copy is caller-supplied so it stays config-driven.
 */
@Composable
fun PromoBar(messages: List<String>, modifier: Modifier = Modifier) {
    if (messages.isEmpty()) return
    var index by remember(messages) { mutableStateOf(0) }
    LaunchedEffect(messages) {
        while (messages.size > 1) {
            delay(4000)
            index = (index + 1) % messages.size
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = Vcv.spacing.md, vertical = Vcv.spacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = messages[index % messages.size]) { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
