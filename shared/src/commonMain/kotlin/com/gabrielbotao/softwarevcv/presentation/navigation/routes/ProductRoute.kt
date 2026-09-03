package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Product detail destination wiring (`/produto/{id}`). Real `ProductScreen` (VCV-8) replaces this. */
fun EntryProviderScope<AppRoute>.productEntry(navigator: Navigator) {
    entry<AppRoute.Product> { key ->
        RoutePlaceholder(
            title = "Produto: ${key.id}",
            subtitle = "Detalhe do produto",
            navigator = navigator,
            actions = listOf(AppRoute.Catalog, AppRoute.Home),
        )
    }
}
