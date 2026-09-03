package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.catalog.view.CatalogScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Catalog (all products) destination wiring. */
fun EntryProviderScope<AppRoute>.catalogEntry(navigator: Navigator) {
    entry<AppRoute.Catalog> {
        CatalogScreen(
            slug = null,
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
        )
    }
}
