package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.checkout.view.CheckoutScreen
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator

/** Checkout destination wiring (`/checkout`). */
fun EntryProviderScope<AppRoute>.checkoutEntry(navigator: Navigator) {
    entry<AppRoute.Checkout> {
        CheckoutScreen(
            onDone = { navigator.navigate(AppRoute.Home) },
            onBackToCart = { navigator.navigate(AppRoute.Cart) },
        )
    }
}
