package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Atelier / Sobre destination wiring. Real `AtelierScreen` (VCV-9) replaces the placeholder. */
fun EntryProviderScope<AppRoute>.atelierEntry(navigator: Navigator) {
    entry<AppRoute.Atelier> {
        RoutePlaceholder(
            title = "Atelier",
            subtitle = "Gaspar · Vale do Itajaí",
            navigator = navigator,
            actions = listOf(AppRoute.Home),
        )
    }
}
