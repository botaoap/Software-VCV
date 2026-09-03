package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Catalog (all products) destination wiring. Real `CatalogScreen` (VCV-7) replaces the placeholder. */
fun EntryProviderScope<AppRoute>.catalogEntry(navigator: Navigator) {
    entry<AppRoute.Catalog> {
        RoutePlaceholder(
            title = "Catálogo",
            subtitle = "Todas as peças",
            navigator = navigator,
            actions = listOf(AppRoute.Product("zebra"), AppRoute.Product("linho")),
        )
    }
}
