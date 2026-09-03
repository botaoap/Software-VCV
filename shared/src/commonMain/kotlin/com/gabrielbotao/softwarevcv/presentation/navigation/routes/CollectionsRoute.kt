package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.collections.view.CollectionsScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Collections list destination wiring (`/colecoes`). */
fun EntryProviderScope<AppRoute>.collectionsEntry(navigator: Navigator) {
    entry<AppRoute.Collections> {
        CollectionsScreen(
            onCollection = { slug -> navigator.navigate(AppRoute.Collection(slug)) },
        )
    }
}
