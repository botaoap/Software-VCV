package com.gabrielbotao.softwarevcv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gabrielbotao.softwarevcv.core.image.configureImageLoader
import com.gabrielbotao.softwarevcv.core.ui.components.FloatingWhatsApp
import com.gabrielbotao.softwarevcv.core.ui.components.PromoBar
import com.gabrielbotao.softwarevcv.core.ui.components.VcvNavItem
import com.gabrielbotao.softwarevcv.core.ui.components.VcvTopNav
import com.gabrielbotao.softwarevcv.core.ui.theme.Vcv
import com.gabrielbotao.softwarevcv.core.ui.theme.VcvTheme
import com.gabrielbotao.softwarevcv.presentation.chrome.ChromeViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppNavHost
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable: the responsive shell (top nav) hosting the nav host, all inside [VcvTheme].
 * The [navigator] is created here by default; the web entrypoint passes its own instance so the
 * browser-history bridge shares it (deep-link on load + URL in sync). See [[MVVM-Multiplatform]].
 *
 * The top bar is the single primary navigation. The footer is **not** in the shell (it used to be
 * pinned to the viewport bottom and duplicated the top nav — VCV-17); each page renders [VcvFooter] at
 * the end of its own scroll content, so it flows with the page and reclaims that vertical space.
 */
@Composable
@Preview
fun App(
    navigator: Navigator = remember { Navigator() },
    chromeViewModel: ChromeViewModel = koinViewModel(),
) {
    remember { configureImageLoader() } // once: Coil singleton loader (Ktor network fetcher)
    val whatsappUrl by chromeViewModel.whatsappUrl.collectAsStateWithLifecycle()
    VcvTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            PromoBar(promoMessages)
            VcvTopNav(
                items = mainNavItems(navigator),
                onLogoClick = { navigator.navigate(AppRoute.Home) },
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AppNavHost(navigator)
                // Persistent WhatsApp CTA, fixed over the content area (VCV-19 global chrome).
                whatsappUrl?.let { url ->
                    FloatingWhatsApp(
                        url = url,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(Vcv.spacing.lg),
                    )
                }
            }
        }
    }
}

/** Restrained promo-bar copy — true for the atelier, no commerce commitments (VCV-19). */
private val promoMessages = listOf(
    "Ateliê próprio em Gaspar · Vale do Itajaí",
    "Envio para todo o Brasil",
    "Atendimento direto no WhatsApp",
    "Poucas peças, bem feitas — feitas rolo a rolo",
)

/** The main navigation destinations shown in the top nav. */
private fun mainNavItems(navigator: Navigator): List<VcvNavItem> = listOf(
    VcvNavItem("Coleções") { navigator.navigate(AppRoute.Collections) },
    VcvNavItem("Catálogo") { navigator.navigate(AppRoute.Catalog) },
    VcvNavItem("Atelier") { navigator.navigate(AppRoute.Atelier) },
    VcvNavItem("Contato") { navigator.navigate(AppRoute.Contact) },
)
