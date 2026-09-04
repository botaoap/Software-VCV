package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Floating "Fale no WhatsApp" pill — the persistent chat CTA every reference storefront has, kept
 * on-brand (wine pill, no third-party green so it stays inside the token system). Position it with
 * [modifier] (e.g. `Modifier.align(BottomEnd).padding(...)`). Global chrome — see
 * [[VCV-19 — site structure & UX redesign]].
 */
@Composable
fun FloatingWhatsApp(url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = "Fale no WhatsApp",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.primary)
            .clickable { uriHandler.openUri(url) }
            .padding(horizontal = Vcv.spacing.lg, vertical = Vcv.spacing.sm),
    )
}
