package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.contact.view.ContactScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Contato destination wiring. */
fun EntryProviderScope<AppRoute>.contactEntry(navigator: Navigator) {
    entry<AppRoute.Contact> {
        ContactScreen()
    }
}
