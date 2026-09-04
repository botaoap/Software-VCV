package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.cart.view.CartScreen
import com.gabrielbotao.softwarevcv.presentation.features.cart.viewmodel.CartViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/** Cart / sacola destination wiring (`/carrinho`) — route owns the ViewModel ([[VCV-28]]). */
fun EntryProviderScope<AppRoute>.cartEntry(navigator: Navigator) {
    entry<AppRoute.Cart> {
        val viewModel: CartViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        CartScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onProduct = { id -> navigator.navigate(AppRoute.Product(id)) },
            onContinue = { navigator.navigate(AppRoute.Catalog) },
            onCheckout = { navigator.navigate(AppRoute.Checkout) },
        )
    }
}
