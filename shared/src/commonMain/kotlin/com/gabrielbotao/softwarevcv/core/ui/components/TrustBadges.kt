package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** One reassurance item in the [TrustBadges] strip. */
data class TrustBadge(val title: String, val subtitle: String)

/**
 * A reassurance strip (ateliê próprio · ficha técnica · envio · atendimento) — the trust-badge row the
 * reference storefronts use, adapted to the atelier. Icon-free (typographic). Responsive: a Row on
 * wide, stacked on compact. Global chrome — see [[VCV-19 — site structure & UX redesign]].
 */
@Composable
fun TrustBadges(items: List<TrustBadge>, modifier: Modifier = Modifier) {
    if (items.isEmpty()) return
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = Vcv.spacing.lg, horizontal = Vcv.spacing.md),
    ) {
        val stacked = WindowWidthClass.of(maxWidth) == WindowWidthClass.COMPACT
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
                items.forEach { BadgeItem(it, Modifier.fillMaxWidth()) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.lg)) {
                items.forEach { BadgeItem(it, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: TrustBadge, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.xs),
    ) {
        Text(
            text = badge.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = badge.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Vcv.colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}
