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
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** Site footer: brand line, location, and optional links. See [[VCV Screens-and-UX]] §0. */
@Composable
fun VcvFooter(
    modifier: Modifier = Modifier,
    items: List<VcvNavItem> = emptyList(),
) {
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
        if (items.isNotEmpty()) {
            Spacer(Modifier.height(Vcv.spacing.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.lg)) {
                items.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Vcv.colors.muted,
                        modifier = Modifier.clickable(onClick = item.onClick),
                    )
                }
            }
        }
    }
}
