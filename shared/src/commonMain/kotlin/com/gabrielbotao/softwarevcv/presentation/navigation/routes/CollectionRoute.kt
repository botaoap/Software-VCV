package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.catalog.view.CatalogScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Single-collection destination wiring (`/colecao/{slug}`) — reuses the catalog grid with the slug. */
fun EntryProviderScope<AppRoute>.collectionEntry(navigator: Navigator) {
    entry<AppRoute.Collection> { key ->
        CatalogScreen(
            slug = key.slug,
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
        )
    }
}
