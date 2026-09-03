package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.atelier.view.AtelierScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Atelier / Sobre destination wiring. */
fun EntryProviderScope<AppRoute>.atelierEntry(navigator: Navigator) {
    entry<AppRoute.Atelier> {
        AtelierScreen()
    }
}
