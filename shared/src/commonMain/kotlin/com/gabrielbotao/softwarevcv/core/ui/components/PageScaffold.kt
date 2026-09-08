package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

private val DefaultPageMaxWidth = 900.dp

/**
 * The standard scroll-column page: a vertical scroller whose [content] is a centered, max-width column,
 * with a **sticky, full-bleed footer**. Removes the repeated "scroll + centered maxWidth + footer"
 * boilerplate from the editorial/list pages (atelier, contato, sacola, checkout).
 *
 * **Sticky footer:** the scroll column is forced to at least the viewport height (`heightIn(min = …)`) and
 * a weighted spacer sits before the footer — so on a short page the footer pins to the bottom of the
 * screen, and on a tall page the spacer collapses to zero and the footer flows below the content and
 * scrolls into view (like home/atelier). See [[VCV-31]] / [[VCV-28]]. The [footer] is caller-supplied (a
 * page passes `AppFooter`) so `core` stays free of presentation wiring.
 */
@Composable
fun PageScaffold(
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    maxWidth: Dp = DefaultPageMaxWidth,
    spacing: Dp = Vcv.spacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(Vcv.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing),
                content = content,
            )
            // Always keep a gap between content and footer (the weighted spacer collapses to 0 on tall
            // pages), so the last item never butts against the footer. Matches home/atelier. See [[VCV-31]].
            Spacer(Modifier.height(Vcv.spacing.xl))
            Spacer(Modifier.weight(1f))
            footer()
        }
    }
}
