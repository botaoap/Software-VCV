package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/**
 * Home destination wiring. One file per route owns its `entry<…>` registration (the real `HomeScreen`
 * replaces the placeholder here in VCV-6) — `AppNavHost` just composes these. See the navigation standard.
 */
fun EntryProviderScope<AppRoute>.homeEntry(navigator: Navigator) {
    entry<AppRoute.Home> {
        RoutePlaceholder(
            title = "Início",
            subtitle = "VCV — Veste Com Você",
            navigator = navigator,
            actions = listOf(AppRoute.Catalog, AppRoute.Collections, AppRoute.Atelier, AppRoute.Contact),
        )
    }
}
