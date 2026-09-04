package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Site footer: brand line + location + optional contact/social links. A **real footer**, not navigation —
 * the top bar is the single primary nav (VCV-17). It flows at the end of each page's scroll content (not
 * pinned). Pure/presentational: [AppFooter] supplies the contact from the shell. See [[VCV Screens-and-UX]] §0.
 */
@Composable
fun VcvFooter(
    modifier: Modifier = Modifier,
    instagramUrl: String? = null,
    whatsappUrl: String? = null,
    email: String? = null,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "VCV — Veste Com Você",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Vcv.spacing.xs))
        Text(
            text = "Gaspar · Vale do Itajaí",
            style = MaterialTheme.typography.labelMedium,
            color = Vcv.colors.muted,
        )
        val hasLinks = instagramUrl != null || whatsappUrl != null || email != null
        if (hasLinks) {
            Spacer(Modifier.height(Vcv.spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.lg)) {
                instagramUrl?.let { FooterLink("Instagram") { uriHandler.openUri(it) } }
                whatsappUrl?.let { FooterLink("WhatsApp") { uriHandler.openUri(it) } }
                email?.let { FooterLink("E-mail") { uriHandler.openUri("mailto:$it") } }
            }
        }
    }
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(onClick = onClick).padding(Vcv.spacing.xs),
    )
}
