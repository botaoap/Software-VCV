package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Single-collection destination wiring (`/colecao/{slug}`). Real `CollectionScreen` (VCV-7) later. */
fun EntryProviderScope<AppRoute>.collectionEntry(navigator: Navigator) {
    entry<AppRoute.Collection> { key ->
        RoutePlaceholder(
            title = "Coleção: ${key.slug}",
            subtitle = "Peças desta coleção",
            navigator = navigator,
            actions = listOf(AppRoute.Product("zebra"), AppRoute.Catalog),
        )
    }
}
