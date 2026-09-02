package com.gabrielbotao.softwarevcv.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gabrielbotao.softwarevcv.core.ui.components.SectionHeader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvButton
import com.gabrielbotao.softwarevcv.core.ui.components.VcvOutlinedButton
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv

/**
 * Renders the screen for the navigator's current route. VCV-4 uses placeholder screens (title + a few
 * navigation actions) so the shell + routing + web-history are demonstrable end-to-end; the real feature
 * screens replace these per page card (VCV-6…VCV-10). See [[VCV Screens-and-UX]].
 */
@Composable
fun AppNavHost(navigator: Navigator, modifier: Modifier = Modifier) {
    val route by navigator.current.collectAsState()
    when (val current = route) {
        AppRoute.Home -> PlaceholderScreen(
            "Início", "VCV — Veste Com Você", navigator,
            listOf(AppRoute.Catalog, AppRoute.Collections, AppRoute.Atelier, AppRoute.Contact), modifier,
        )
        AppRoute.Collections -> PlaceholderScreen(
            "Coleções", "Nossas coleções", navigator,
            listOf(AppRoute.Collection("verao"), AppRoute.Catalog), modifier,
        )
        is AppRoute.Collection -> PlaceholderScreen(
            "Coleção: ${current.slug}", "Peças desta coleção", navigator,
            listOf(AppRoute.Product("zebra"), AppRoute.Catalog), modifier,
        )
        AppRoute.Catalog -> PlaceholderScreen(
            "Catálogo", "Todas as peças", navigator,
            listOf(AppRoute.Product("zebra"), AppRoute.Product("linho")), modifier,
        )
        is AppRoute.Product -> PlaceholderScreen(
            "Produto: ${current.id}", "Detalhe do produto", navigator,
            listOf(AppRoute.Catalog, AppRoute.Home), modifier,
        )
        AppRoute.Atelier -> PlaceholderScreen(
            "Atelier", "Gaspar · Vale do Itajaí", navigator, listOf(AppRoute.Home), modifier,
        )
        AppRoute.Contact -> PlaceholderScreen(
            "Contato", "Fale com a VCV", navigator, listOf(AppRoute.Home), modifier,
        )
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    navigator: Navigator,
    actions: List<AppRoute>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Vcv.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Vcv.spacing.md),
    ) {
        SectionHeader(title = title, subtitle = subtitle)
        Spacer(Modifier.height(Vcv.spacing.sm))
        actions.forEach { target ->
            VcvButton(text = "Ir para ${target.label()}", onClick = { navigator.navigate(target) })
        }
        VcvOutlinedButton(text = "Voltar", onClick = { navigator.pop() })
    }
}
