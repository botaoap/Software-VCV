package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.home.view.HomeScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/**
 * Home destination wiring — one file per route owns its `entry<…>`; `AppNavHost` just composes these.
 * See the navigation standard ([[nav3-per-route-navigation]]).
 */
fun EntryProviderScope<AppRoute>.homeEntry(navigator: Navigator) {
    entry<AppRoute.Home> {
        HomeScreen(
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
            onCatalog = { navigator.navigate(AppRoute.Catalog) },
            onAtelier = { navigator.navigate(AppRoute.Atelier) },
        )
    }
}
