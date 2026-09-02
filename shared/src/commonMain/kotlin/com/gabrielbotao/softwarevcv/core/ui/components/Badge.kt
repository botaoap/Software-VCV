package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** A small marketing badge (e.g. "BEST-SELLER") overlaid on a product cover. See [[VCV Design-System]] §9. */
@Composable
fun VcvBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Box(
        modifier = modifier
            .background(color, MaterialTheme.shapes.small)
            .padding(horizontal = Vcv.spacing.sm, vertical = Vcv.spacing.xs),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}
