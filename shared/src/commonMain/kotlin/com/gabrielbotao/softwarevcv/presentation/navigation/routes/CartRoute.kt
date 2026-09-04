package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.cart.view.CartScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Cart / sacola destination wiring (`/carrinho`). */
fun EntryProviderScope<AppRoute>.cartEntry(navigator: Navigator) {
    entry<AppRoute.Cart> {
        CartScreen(
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
            onContinue = { navigator.navigate(AppRoute.Catalog) },
        )
    }
}
