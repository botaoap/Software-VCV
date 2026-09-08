package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.responsive.WindowWidthClass
import com.gabrielbotao.softwarevcv.core.ui.responsive.productGridColumns
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Responsive product grid — columns follow the measured width (1 → 2 → 4). Generic over the item type;
 * pages supply the `itemContent` (typically a [ProductCard]), an optional full-width [header] (e.g. a
 * collection story), and an optional **sticky, full-bleed** [footer] (e.g. [VcvFooter]).
 *
 * The page scrolls as one column. The footer pins to the bottom of the viewport on a short catalog (few
 * items) via a weighted spacer, and flows below the content and scrolls on a long one — matching the rest
 * of the site (VCV-31). The catalog is small (an atelier's curated pieces), so a plain column of rows is
 * used rather than a lazy grid; that's what lets the whole page share one scroller + a sticky footer.
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
        val viewportHeight = maxHeight
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(Vcv.spacing.md),
                verticalArrangement = Arrangement.spacedBy(Vcv.spacing.lg),
            ) {
                header?.invoke()
                items.chunked(columns).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Vcv.spacing.md)) {
                        rowItems.forEach { item ->
                            Box(Modifier.weight(1f)) { itemContent(item) }
                        }
                        // Keep the last (partial) row's cells aligned to the grid columns.
                        repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            if (footer != null) {
                // Fixed gap so content never butts the footer on a tall page (weighted spacer → 0 there).
                Spacer(Modifier.height(Vcv.spacing.xl))
                Spacer(Modifier.weight(1f))
                footer()
            }
        }
    }
}
