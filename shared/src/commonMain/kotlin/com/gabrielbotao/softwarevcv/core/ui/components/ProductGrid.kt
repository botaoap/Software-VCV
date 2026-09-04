package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.responsive.productGridColumns
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

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
    BoxWithConstraints(modifier) {
        val columns = WindowWidthClass.of(maxWidth).productGridColumns()
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
            verticalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
            contentPadding = PaddingValues(Vcv.spacing.md),
        ) {
            if (header != null) {
                item(span = { GridItemSpan(maxLineSpan) }) { header() }
            }
            items(items) { item -> itemContent(item) }
            if (footer != null) {
                item(span = { GridItemSpan(maxLineSpan) }) { footer() }
            }
        }
    }
}
