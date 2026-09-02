package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.gabrielbotao.softwarevcv.core.util.formatBrl

/** Renders a BRL price from integer cents (pt-BR, single line). See [[VCV Design-System]] §9. */
@Composable
fun PriceText(
    amountCents: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(text = formatBrl(amountCents), modifier = modifier, style = style, color = color, maxLines = 1)
}
