package com.gabrielbotao.softwarevcv.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.atelierEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.cartEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.catalogEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.collectionEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.collectionsEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.contactEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.homeEntry
import com.gabrielbotao.softwarevcv.presentation.navigation.routes.productEntry

/**
 * The Navigation 3 host. It only **composes** the per-route entries from `navigation/routes/` — each
 * route owns its own `entry<…>` wiring in its own file, so this stays a flat, scalable table (add a
 * route = add a file + one line here). No per-screen logic lives here. See the navigation standard /
 * [[MVVM-Multiplatform]] §Navigation.
 */
@Composable
fun AppNavHost(navigator: Navigator, modifier: Modifier = Modifier) {
    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier,
        onBack = { navigator.pop() },
        entryProvider = entryProvider {
            homeEntry(navigator)
            collectionsEntry(navigator)
            collectionEntry(navigator)
            catalogEntry(navigator)
            productEntry(navigator)
            atelierEntry(navigator)
            contactEntry(navigator)
            cartEntry(navigator)
        },
    )
}
