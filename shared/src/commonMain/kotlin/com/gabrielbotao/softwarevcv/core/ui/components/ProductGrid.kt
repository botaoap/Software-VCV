package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.responsive.productGridColumns
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Lets a lazy item bleed past the grid's horizontal [contentPadding] to the screen edges — it grows its
 * width by `2 * padding` and shifts left by `padding`. Used for the full-width footer so its background
 * reaches both edges (matching the un-padded page footers), instead of sitting inset like a card.
 */
private fun Modifier.fullBleedHorizontal(padding: Dp) = layout { measurable, constraints ->
    val extra = (padding * 2).roundToPx()
    val widened = if (constraints.hasBoundedWidth) constraints.maxWidth + extra else constraints.maxWidth
    val placeable = measurable.measure(constraints.copy(minWidth = widened, maxWidth = widened))
    layout(placeable.width, placeable.height) { placeable.place(-padding.roundToPx(), 0) }
}

/**
 * Responsive product grid — columns follow the measured width (1 → 2 → 4). It is the page's scroller
 * (a `LazyVerticalGrid`), so give it the available height. Generic over the item type; pages supply the
 * `itemContent` (typically a [ProductCard]), an optional full-width [header] (e.g. a collection story),
 * and an optional full-width [footer] (e.g. [VcvFooter], so it scrolls with the page — VCV-17).
 * See [[VCV Design-System]] §8, [[VCV Screens-and-UX]] §3.
 */
@Composable
fun <T> ProductGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    val gridPadding = Vcv.spacing.md
    BoxWithConstraints(modifier) {
        val columns = WindowWidthClass.of(maxWidth).productGridColumns()
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
            contentPadding = PaddingValues(gridPadding),
        ) {
            if (header != null) {
                item(span = { GridItemSpan(maxLineSpan) }) { header() }
            }
            items(items) { item -> itemContent(item) }
            if (footer != null) {
                // Bleed the footer to the screen edges — its background shouldn't sit inset like a card.
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fullBleedHorizontal(gridPadding)) { footer() }
                }
            }
        }
    }
}
