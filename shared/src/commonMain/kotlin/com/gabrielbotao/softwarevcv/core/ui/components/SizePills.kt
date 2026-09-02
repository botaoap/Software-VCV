package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/** A wrapping row of size pills (the VCV grade 38–52). Presentational; selection is optional. §9. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SizePills(
    sizes: List<Int>,
    modifier: Modifier = Modifier,
    selected: Int? = null,
    onSelect: (Int) -> Unit = {},
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.sm),
    ) {
        sizes.forEach { size ->
            val isSelected = size == selected
            val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            val content = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                    .background(background, MaterialTheme.shapes.small)
                    .clickable { onSelect(size) }
                    .padding(horizontal = Vcv.spacing.md, vertical = Vcv.spacing.sm),
            ) {
                Text(size.toString(), style = MaterialTheme.typography.labelLarge, color = content)
            }
        }
    }
}
