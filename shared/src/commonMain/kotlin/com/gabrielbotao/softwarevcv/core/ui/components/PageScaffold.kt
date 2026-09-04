package com.gabrielbotao.softwarevcv.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
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
 * with a full-bleed footer at the end. Removes the repeated "scroll + centered maxWidth + footer"
 * boilerplate from the editorial/list pages (atelier, contato, sacola, checkout). Grid/hero pages that
 * own their own scroller (home, catálogo) don't use this. The [footer] is supplied by the caller
 * (a page passes `AppFooter`) so `core` stays free of presentation wiring. See [[VCV-28]].
 */
@Composable
fun PageScaffold(
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    maxWidth: Dp = DefaultPageMaxWidth,
    spacing: Dp = Vcv.spacing.lg,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .align(Alignment.CenterHorizontally)
                .padding(Vcv.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = content,
        )
        footer()
    }
}
