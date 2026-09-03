package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Collections list destination wiring. Real `CollectionsScreen` (VCV-7) replaces the placeholder. */
fun EntryProviderScope<AppRoute>.collectionsEntry(navigator: Navigator) {
    entry<AppRoute.Collections> {
        RoutePlaceholder(
            title = "Coleções",
            subtitle = "Nossas coleções",
            navigator = navigator,
            actions = listOf(AppRoute.Collection("verao"), AppRoute.Catalog),
        )
    }
}
