package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Contato destination wiring. Real `ContactScreen` (VCV-10) replaces the placeholder. */
fun EntryProviderScope<AppRoute>.contactEntry(navigator: Navigator) {
    entry<AppRoute.Contact> {
        RoutePlaceholder(
            title = "Contato",
            subtitle = "Fale com a VCV",
            navigator = navigator,
            actions = listOf(AppRoute.Home),
        )
    }
}
