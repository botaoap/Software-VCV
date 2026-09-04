package com.gabrielbotao.softwarevcv.presentation.navigation.routes

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import com.gabrielbotao.softwarevcv.presentation.features.checkout.state.CheckoutUiEvent
import com.gabrielbotao.softwarevcv.presentation.features.checkout.view.CheckoutScreen
import com.gabrielbotao.softwarevcv.presentation.features.checkout.viewmodel.CheckoutViewModel
import com.gabrielbotao.softwarevcv.presentation.navigation.AppRoute
import com.gabrielbotao.softwarevcv.presentation.navigation.Navigator
import org.koin.compose.viewmodel.koinViewModel

/**
 * Checkout destination wiring (`/checkout`) — the route owns the ViewModel, state, and the one-shot
 * openUrl side-effect (WhatsApp handoff / 3rd-party redirect); [CheckoutScreen] is stateless ([[VCV-28]]).
 */
fun EntryProviderScope<AppRoute>.checkoutEntry(navigator: Navigator) {
    entry<AppRoute.Checkout> {
        val viewModel: CheckoutViewModel = koinViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val uriHandler = LocalUriHandler.current
        LaunchedEffect(state.openUrl) {
            state.openUrl?.let { url ->
                uriHandler.openUri(url)
                viewModel.onEvent(CheckoutUiEvent.UrlOpened)
            }
        }
        CheckoutScreen(
            state = state,
            onEvent = viewModel::onEvent,
            onDone = { navigator.navigate(AppRoute.Home) },
            onBackToCart = { navigator.navigate(AppRoute.Cart) },
        )
    }
}
