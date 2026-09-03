package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.product.view.ProductScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Product detail destination wiring (`/produto/{id}`). */
fun EntryProviderScope<AppRoute>.productEntry(navigator: Navigator) {
    entry<AppRoute.Product> { key ->
        ProductScreen(
            id = key.id,
            onCatalog = { navigator.navigate(AppRoute.Catalog) },
        )
    }
}
