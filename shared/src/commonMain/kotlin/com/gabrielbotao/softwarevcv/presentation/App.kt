package com.gabrielbotao.softwarevcv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.gabrielbotao.softwarevcv.core.image.configureImageLoader
import com.gabrielbotao.softwarevcv.core.ui.components.VcvFooter
import com.gabrielbotao.softwarevcv.core.ui.components.VcvNavItem
import com.gabrielbotao.softwarevcv.core.ui.components.VcvTopNav
import com.gabrielbotao.softwarevcv.core.ui.theme.VcvTheme
import com.gabrielbotao.softwarevcv.presentation.navigation.AppNavHost
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/**
 * Root composable: the responsive shell (top nav + footer) hosting the nav host, all inside [VcvTheme].
 * The [navigator] is created here by default; the web entrypoint passes its own instance so the
 * browser-history bridge shares it (deep-link on load + URL in sync). See [[MVVM-Multiplatform]].
 */
@Composable
@Preview
fun App(navigator: Navigator = remember { Navigator() }) {
    remember { configureImageLoader() } // once: Coil singleton loader (Ktor network fetcher)
    VcvTheme {
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            VcvTopNav(
                items = mainNavItems(navigator),
                onLogoClick = { navigator.navigate(AppRoute.Home) },
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AppNavHost(navigator)
            }
            VcvFooter(items = mainNavItems(navigator))
        }
    }
}

/** The main navigation destinations shown in the top nav and footer. */
private fun mainNavItems(navigator: Navigator): List<VcvNavItem> = listOf(
    VcvNavItem("Coleções") { navigator.navigate(AppRoute.Collections) },
    VcvNavItem("Catálogo") { navigator.navigate(AppRoute.Catalog) },
    VcvNavItem("Atelier") { navigator.navigate(AppRoute.Atelier) },
    VcvNavItem("Contato") { navigator.navigate(AppRoute.Contact) },
)
